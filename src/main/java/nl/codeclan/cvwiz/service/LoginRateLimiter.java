package nl.codeclan.cvwiz.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LoginRateLimiter {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final ConcurrentMap<String, LoginAttempt> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginRateLimiter() {
        this(Clock.systemUTC());
    }

    LoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public String createKey(String username, String remoteAddress) {
        String normalizedUsername = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        String normalizedAddress = remoteAddress == null ? "unknown" : remoteAddress;
        return normalizedUsername + "|" + normalizedAddress;
    }

    public void checkAllowed(String key) {
        LoginAttempt attempt = attempts.get(key);
        if (attempt == null || attempt.lockedUntil == null) {
            return;
        }
        if (Instant.now(clock).isBefore(attempt.lockedUntil)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Te veel mislukte loginpogingen. Probeer het later opnieuw.");
        }
        attempts.remove(key);
    }

    public void recordFailure(String key) {
        attempts.compute(key, (ignored, existingAttempt) -> {
            Instant now = Instant.now(clock);
            int failures = existingAttempt == null ? 1 : existingAttempt.failedAttempts + 1;
            Instant lockedUntil = failures >= MAX_FAILED_ATTEMPTS ? now.plus(LOCK_DURATION) : null;
            return new LoginAttempt(failures, lockedUntil);
        });
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    private record LoginAttempt(int failedAttempts, Instant lockedUntil) {
    }
}
