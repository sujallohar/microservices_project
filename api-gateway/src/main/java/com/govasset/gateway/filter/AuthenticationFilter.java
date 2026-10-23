package com.govasset.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {
        // Configuration parameters if required
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Allow public auth endpoints and actuator
            if (isSecured(path)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    log.warn("Missing Authorization Header for path: {}", path);
                    return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.warn("Invalid Authorization Header format for path: {}", path);
                    return onError(exchange, "Invalid Authorization Header format. Expected Bearer token.", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7);
                try {
                    Claims claims = Jwts.parser()
                            .verifyWith(getSigningKey())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

                    String userId = claims.getSubject();
                    String username = claims.get("username", String.class);
                    String department = claims.get("department", String.class);
                    List<?> roles = claims.get("roles", List.class);
                    String rolesStr = roles != null ? String.join(",", roles.stream().map(Object::toString).toList()) : "";

                    // Decorate downstream request with verified authentication context
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Id", userId != null ? userId : "")
                            .header("X-Username", username != null ? username : "")
                            .header("X-User-Roles", rolesStr)
                            .header("X-Department", department != null ? department : "CIVIL")
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());

                } catch (JwtException | IllegalArgumentException e) {
                    log.error("JWT verification failed at Gateway: {}", e.getMessage());
                    return onError(exchange, "Unauthorized: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                }
            }

            return chain.filter(exchange);
        };
    }

    private boolean isSecured(String path) {
        return !path.startsWith("/api/v1/auth/login") &&
                !path.startsWith("/api/v1/auth/register") &&
                !path.startsWith("/fallback/") &&
                !path.startsWith("/actuator/");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format("{\"success\":false,\"error\":\"%s\",\"statusCode\":%d}", err, httpStatus.value());
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
