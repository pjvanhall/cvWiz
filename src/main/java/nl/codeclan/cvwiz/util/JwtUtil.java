package nl.codeclan.cvwiz.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public final class JwtUtil {

    private static final String ISSUER = "cvWiz-api";
    private static final String AUDIENCE = "cvWiz-client";
    private static final long TOKEN_VALIDITY_MILLIS = 1000L * 60 * 60 * 24 * 10;

    private final SecretKey key;

    public JwtUtil(@Value("${SECRET}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
    }

    private Date extractExpiration(String jwt) {
        return extractClaim(jwt, Claims::getExpiration);
    }

    private <T> T extractClaim(String jwt, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jwt);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String jwt) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(ISSUER)
                .requireAudience(AUDIENCE)
                .clockSkewSeconds(30)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    private Boolean isTokenExpired(String jwt) {
        return extractExpiration(jwt).before(new Date());
    }

    public String generateToken(UserDetails userDetails, String username, String email, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        if (name != null) {
            claims.put("name", name);
        }
        List<String> authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        claims.put("authorities", authorities);

        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date issuedAt = new Date(System.currentTimeMillis());
        return Jwts.builder()
                .claims(claims)
                .issuer(ISSUER)
                .subject(subject)
                .audience().add(AUDIENCE).and()
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiration(new Date(issuedAt.getTime() + TOKEN_VALIDITY_MILLIS))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Boolean validateToken(String jwt, UserDetails userDetails) {
        try {
            final String username = extractUsername(jwt);
            return username.equals(userDetails.getUsername()) && userDetails.isEnabled() && !isTokenExpired(jwt);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
