package com.govasset.identity.security;

import com.govasset.identity.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    public JwtTokenProvider(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        List<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(user.getId())
                .claim("username", user.getUsername())
                .claim("fullName", user.getFullName())
                .claim("department", user.getDepartment() != null ? user.getDepartment() : "CIVIL")
                .claim("roles", roleNames)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            // Check Redis blacklist
            if (isTokenBlacklisted(token)) {
                log.warn("Token is blacklisted in Redis");
                return false;
            }

            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public void blacklistToken(String token, String reason) {
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            long ttlMs = expiration.getTime() - System.currentTimeMillis();
            if (ttlMs > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + token,
                        reason != null ? reason : "LOGOUT",
                        ttlMs,
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception e) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "REVOKED", 1, TimeUnit.HOURS);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        Boolean hasKey = redisTemplate.hasKey(BLACKLIST_PREFIX + token);
        return Boolean.TRUE.equals(hasKey);
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}
