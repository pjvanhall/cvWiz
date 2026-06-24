package nl.codeclan.cvwiz.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import nl.codeclan.cvwiz.dto.LoginRequestDto;
import nl.codeclan.cvwiz.dto.LoginResponseDto;
import nl.codeclan.cvwiz.model.CustomUser;
import nl.codeclan.cvwiz.service.CustomUserDetailService;
import nl.codeclan.cvwiz.service.CustomUserService;
import nl.codeclan.cvwiz.service.LoginRateLimiter;
import nl.codeclan.cvwiz.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import nl.codeclan.cvwiz.dto.GoogleLoginRequestDto;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Collections;


@RestController
@RequestMapping("/gebruikers")
@Validated
public class CustomUserController {
    private final CustomUserService customUserService;
    private final CustomUserDetailService customUserDetailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LoginRateLimiter loginRateLimiter;

    @Value("${google.client.id}")
    private String googleClientId;

    public CustomUserController(
            CustomUserService customUserService,
            CustomUserDetailService customUserDetailService,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            LoginRateLimiter loginRateLimiter
    ) {
        this.customUserService = customUserService;
        this.customUserDetailService = customUserDetailService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto loginRequest, HttpServletRequest request) {
        String rateLimitKey = loginRateLimiter.createKey(loginRequest.username(), request.getRemoteAddr());
        loginRateLimiter.checkAllowed(rateLimitKey);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
            loginRateLimiter.recordSuccess(rateLimitKey);
        } catch (AuthenticationException ex) {
            loginRateLimiter.recordFailure(rateLimitKey);
            throw ex;
        }

        CustomUser user = customUserService.getUserById(loginRequest.username());
        UserDetails userDetails = customUserDetailService.loadUserByUsername(loginRequest.username());
        String token = jwtUtil.generateToken(userDetails, user.getUsername(), user.getEmail());

        return new LoginResponseDto(token);
    }

    @PostMapping("/google-login")
    public LoginResponseDto googleLogin(@Valid @RequestBody GoogleLoginRequestDto loginRequest, HttpServletRequest request) {
        String rateLimitKey = loginRateLimiter.createKey("google-" + request.getRemoteAddr(), request.getRemoteAddr());
        loginRateLimiter.checkAllowed(rateLimitKey);

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(loginRequest.idToken());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                
                CustomUser user = customUserService.getUserByEmail(email);
                UserDetails userDetails = customUserDetailService.loadUserByUsername(user.getUsername());
                String token = jwtUtil.generateToken(userDetails, user.getUsername(), user.getEmail());
                
                loginRateLimiter.recordSuccess(rateLimitKey);
                return new LoginResponseDto(token);
            } else {
                loginRateLimiter.recordFailure(rateLimitKey);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID token.");
            }
        } catch (Exception e) {
            loginRateLimiter.recordFailure(rateLimitKey);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google login failed: " + e.getMessage());
        }
    }

}
