package nl.codeclan.cvwiz.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginRateLimiterTest {

    @Test
    void createsNormalizedKeys() {
        LoginRateLimiter limiter = new LoginRateLimiter();

        assertThat(limiter.createKey("  USER@EXAMPLE.COM  ", "127.0.0.1")).isEqualTo("user@example.com|127.0.0.1");
        assertThat(limiter.createKey(null, null)).isEqualTo("|unknown");
    }

    @Test
    void allowsBeforeThresholdAndClearsFailuresOnSuccess() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-12T12:00:00Z"));
        LoginRateLimiter limiter = new LoginRateLimiter(clock);
        String key = limiter.createKey("user", "host");

        limiter.recordFailure(key);
        limiter.recordFailure(key);
        limiter.checkAllowed(key);
        limiter.recordSuccess(key);
        limiter.checkAllowed(key);
    }

    @Test
    void locksAfterFiveFailuresUntilLockExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-12T12:00:00Z"));
        LoginRateLimiter limiter = new LoginRateLimiter(clock);
        String key = "user|host";

        for (int i = 0; i < 5; i++) {
            limiter.recordFailure(key);
        }

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> limiter.checkAllowed(key));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        clock.instant = Instant.parse("2026-06-12T12:16:00Z");

        limiter.checkAllowed(key);
        limiter.checkAllowed(key);
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
