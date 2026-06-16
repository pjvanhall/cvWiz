package nl.codeclan.cvwiz.config;

import nl.codeclan.cvwiz.model.CustomUser;
import nl.codeclan.cvwiz.repository.CustomUserRepository;
import nl.codeclan.cvwiz.service.CustomUserDetailService;
import nl.codeclan.cvwiz.service.CustomUserService;
import nl.codeclan.cvwiz.support.RepositoryDoubles;
import nl.codeclan.cvwiz.support.TestData;
import nl.codeclan.cvwiz.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitWebConfig(classes = {SecConfig.class, SecConfigTest.TestBeans.class})
class SecConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void passwordEncoderUsesBCrypt() {
        String encoded = passwordEncoder.encode("secret-password");

        assertThat(encoded).startsWith("$2");
        assertThat(passwordEncoder.matches("secret-password", encoded)).isTrue();
    }

    @Test
    void authenticationManagerUsesConfiguredUserDetailsServiceAndPasswordEncoder() {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("jane@example.com", "password"));

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getName()).isEqualTo("jane@example.com");
    }

    @Test
    void securityFilterChainMatchesRequests() {
        assertThat(securityFilterChain.matches(new MockHttpServletRequest("GET", "/anything"))).isTrue();
    }

    @Configuration
    static class TestBeans {

        @Bean
        RepositoryDoubles.TestRepository<CustomUserRepository, CustomUser, String> userRepositoryDouble(PasswordEncoder passwordEncoder) {
            RepositoryDoubles.TestRepository<CustomUserRepository, CustomUser, String> repository = RepositoryDoubles.users();
            CustomUser user = TestData.user("jane@example.com", "ROLE_CONSULTANT");
            user.setPassword(passwordEncoder.encode("password"));
            repository.put(user);
            return repository;
        }

        @Bean
        CustomUserRepository customUserRepository(RepositoryDoubles.TestRepository<CustomUserRepository, CustomUser, String> repository) {
            return repository.repository();
        }

        @Bean
        CustomUserService customUserService(CustomUserRepository repository, PasswordEncoder passwordEncoder) {
            return new CustomUserService(repository, passwordEncoder);
        }

        @Bean
        CustomUserDetailService customUserDetailService(CustomUserService customUserService) {
            return new CustomUserDetailService(customUserService);
        }

        @Bean
        JwtUtil jwtUtil() {
            return new JwtUtil("01234567890123456789012345678901");
        }

        @Bean
        JwtRequestFilter jwtRequestFilter(CustomUserDetailService customUserDetailService, JwtUtil jwtUtil) {
            return new JwtRequestFilter(customUserDetailService, jwtUtil);
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.addAllowedOrigin("http://localhost:3000");
            configuration.addAllowedMethod("GET");
            configuration.addAllowedHeader("Authorization");
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }
    }
}
