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


@RestController
@RequestMapping("/gebruikers")
@Validated
public class CustomUserController {
    private final CustomUserService customUserService;
    private final CustomUserDetailService customUserDetailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LoginRateLimiter loginRateLimiter;

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

}
