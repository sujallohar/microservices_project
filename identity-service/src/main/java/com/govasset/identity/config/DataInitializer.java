package com.govasset.identity.config;

import com.govasset.identity.model.Role;
import com.govasset.identity.model.User;
import com.govasset.identity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            seedUserIfMissing("superadmin", "Admin@123", "Chief Government Administrator",
                    "admin@govasset.gujarat.gov.in", "GENERAL_ADMIN", Set.of(Role.ROLE_SUPER_ADMIN));

            seedUserIfMissing("finance_officer", "Finance@123", "Finance & Procurement Director",
                    "finance@govasset.gujarat.gov.in", "FINANCE_DEPT", Set.of(Role.ROLE_FINANCE_OFFICER));

            seedUserIfMissing("asset_officer", "Asset@123", "Asset Registrar Officer",
                    "asset@govasset.gujarat.gov.in", "PUBLIC_WORKS", Set.of(Role.ROLE_ASSET_OFFICER));

            seedUserIfMissing("inspector", "Inspector@123", "Chief Field Maintenance Inspector",
                    "inspector@govasset.gujarat.gov.in", "URBAN_INFRASTRUCTURE", Set.of(Role.ROLE_MAINTENANCE_INSPECTOR));

            seedUserIfMissing("auditor", "Auditor@123", "State Audit Officer",
                    "auditor@govasset.gujarat.gov.in", "CAG_AUDIT", Set.of(Role.ROLE_AUDITOR));

            seedUserIfMissing("citizen", "Citizen@123", "Aarav Patel (Citizen)",
                    "aarav.patel@gujarat.in", "PUBLIC", Set.of(Role.ROLE_CITIZEN));

            log.info("Government RBAC default identities verified in Redis.");
        } catch (Exception e) {
            log.warn("Notice: Could not seed Redis (Redis may not be running yet): {}", e.getMessage());
        }
    }

    private void seedUserIfMissing(String username, String rawPassword, String fullName, String email,
                                   String department, Set<Role> roles) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .id(UUID.randomUUID().toString())
                    .username(username)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .fullName(fullName)
                    .email(email)
                    .department(department)
                    .roles(roles)
                    .active(true)
                    .createdAt(System.currentTimeMillis())
                    .build();
            userRepository.save(user);
            log.info("Initialized default seed user: [{}] with roles: {}", username, roles);
        }
    }
}
