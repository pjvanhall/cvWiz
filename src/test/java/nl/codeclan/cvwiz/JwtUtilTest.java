package nl.codeclan.cvwiz;

import nl.codeclan.cvwiz.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "01234567890123456789012345678901";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET);

    @Test
    void generatesTokenAndExtractsUsername() {
        UserDetails details = User.withUsername("jane")
                .password("encoded")
                .authorities("ROLE_CONSULTANT")
                .build();

        String token = jwtUtil.generateToken(details, "ignored", "jane@example.com");

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("jane");
        assertThat(jwtUtil.validateToken(token, details)).isTrue();
    }

    @Test
    void validationReturnsFalseForDisabledUsersAndInvalidTokens() {
        UserDetails enabled = User.withUsername("jane")
                .password("encoded")
                .authorities("ROLE_CONSULTANT")
                .build();
        UserDetails disabled = User.withUsername("jane")
                .password("encoded")
                .authorities("ROLE_CONSULTANT")
                .disabled(true)
                .build();
        String token = jwtUtil.generateToken(enabled, "ignored", "jane@example.com");

        assertThat(jwtUtil.validateToken(token, disabled)).isFalse();
        assertThat(jwtUtil.validateToken("not-a-token", enabled)).isFalse();
    }
}
