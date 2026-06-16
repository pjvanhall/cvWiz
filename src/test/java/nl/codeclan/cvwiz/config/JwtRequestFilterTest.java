package nl.codeclan.cvwiz.config;

import nl.codeclan.cvwiz.model.CustomUser;
import nl.codeclan.cvwiz.repository.CustomUserRepository;
import nl.codeclan.cvwiz.service.CustomUserDetailService;
import nl.codeclan.cvwiz.service.CustomUserService;
import nl.codeclan.cvwiz.support.RepositoryDoubles;
import nl.codeclan.cvwiz.support.TestData;
import nl.codeclan.cvwiz.support.TestPasswordEncoder;
import nl.codeclan.cvwiz.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

class JwtRequestFilterTest {

    private static final String SECRET = "01234567890123456789012345678901";

    private RepositoryDoubles.TestRepository<CustomUserRepository, CustomUser, String> userRepository;
    private JwtUtil jwtUtil;
    private JwtRequestFilter filter;

    @BeforeEach
    void setUp() {
        userRepository = RepositoryDoubles.users();
        CustomUserService customUserService = new CustomUserService(userRepository.repository(), new TestPasswordEncoder());
        jwtUtil = new JwtUtil(SECRET);
        filter = new JwtRequestFilter(new CustomUserDetailService(customUserService), jwtUtil);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void continuesWithoutAuthorizationHeader() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest(), response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void rejectsMalformedTokensBeforeContinuingChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer not-a-token");
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void rejectsTokensForUnknownUsers() throws Exception {
        String token = tokenFor("missing@example.com");
        MockHttpServletRequest request = bearerRequest(token);
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void rejectsDisabledUsers() throws Exception {
        CustomUser user = TestData.user("jane@example.com", "ROLE_CONSULTANT");
        user.setEnabled(false);
        userRepository.put(user);
        MockHttpServletRequest request = bearerRequest(tokenFor("jane@example.com"));
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void authenticatesValidBearerToken() throws Exception {
        userRepository.put(TestData.user("jane@example.com", "ROLE_CONSULTANT"));
        MockHttpServletRequest request = bearerRequest(tokenFor("jane@example.com"));
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).extracting(Object::toString).containsExactly("ROLE_CONSULTANT");
    }

    @Test
    void skipsAuthenticationWhenSecurityContextAlreadyHasAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("existing", null));
        MockHttpServletRequest request = bearerRequest(tokenFor("missing@example.com"));
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("existing");
    }

    private MockHttpServletRequest bearerRequest(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private String tokenFor(String username) {
        UserDetails details = User.withUsername(username)
                .password("encoded")
                .authorities("ROLE_CONSULTANT")
                .build();
        return jwtUtil.generateToken(details, username, username);
    }
}
