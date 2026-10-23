package com.govasset.identity.service;

import com.govasset.identity.dto.AuthDtos.*;
import com.govasset.identity.model.Role;
import com.govasset.identity.model.User;
import com.govasset.identity.repository.UserRepository;
import com.govasset.identity.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + req.getUsername());
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(req.getUsername())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .email(req.getEmail())
                .department(req.getDepartment() != null ? req.getDepartment() : "ADMINISTRATION")
                .roles(req.getRoles() != null && !req.getRoles().isEmpty() ? req.getRoles() : Set.of(Role.ROLE_CITIZEN))
                .active(true)
                .createdAt(System.currentTimeMillis())
                .build();

        userRepository.save(user);
        log.info("Registered new user: {} with roles: {}", user.getUsername(), user.getRoles());

        String token = jwtTokenProvider.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(jwtTokenProvider.getExpirationMs())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .department(user.getDepartment())
                .roles(user.getRoles())
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!user.isActive()) {
            throw new IllegalStateException("Account is deactivated");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = jwtTokenProvider.generateToken(user);
        log.info("User logged in successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(jwtTokenProvider.getExpirationMs())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .department(user.getDepartment())
                .roles(user.getRoles())
                .build();
    }

    public void logout(String token, String reason) {
        jwtTokenProvider.blacklistToken(token, reason != null ? reason : "USER_LOGOUT");
        log.info("Token successfully blacklisted in Redis");
    }

    public TokenValidationResponse validateToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token is invalid, expired, or blacklisted")
                    .build();
        }

        Claims claims = jwtTokenProvider.getClaims(token);
        List<?> rolesList = claims.get("roles", List.class);
        Set<String> roles = rolesList != null ? rolesList.stream().map(Object::toString).collect(Collectors.toSet()) : Collections.emptySet();

        return TokenValidationResponse.builder()
                .valid(true)
                .userId(claims.getSubject())
                .username(claims.get("username", String.class))
                .department(claims.get("department", String.class))
                .roles(roles)
                .message("Token is valid")
                .build();
    }

    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }

    public List<Map<String, Object>> getAllRoles() {
        return Arrays.stream(Role.values())
                .map(r -> Map.of(
                        "role", (Object) r.name(),
                        "description", switch (r) {
                            case ROLE_SUPER_ADMIN -> "Full access to all services, deployments, and audit registers";
                            case ROLE_ASSET_OFFICER -> "Manage asset catalog, transfers, QR scanning, and retirement";
                            case ROLE_FINANCE_OFFICER -> "Approve procurement, manage budget allocations, view TCO";
                            case ROLE_MAINTENANCE_INSPECTOR -> "Conduct physical inspections, log condition scores, execute work orders";
                            case ROLE_AUDITOR -> "Read-only access to immutable audit records and transactions";
                            case ROLE_CITIZEN -> "Submit public grievance reports and track status";
                        }
                ))
                .collect(Collectors.toList());
    }
}
