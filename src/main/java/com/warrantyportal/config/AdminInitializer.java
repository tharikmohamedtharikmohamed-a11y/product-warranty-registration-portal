package com.warrantyportal.config;

import com.warrantyportal.entity.User;
import com.warrantyportal.entity.enums.UserRole;
import com.warrantyportal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.admin.seed.name:System Admin}")
    private String adminName;

    @Value("${app.admin.seed.email:admin@warrantyportal.com}")
    private String adminEmail;

    @Value("${app.admin.seed.password:AdminPassword123}")
    private String adminPassword;

    @Value("${app.admin.seed.phone:9999999999}")
    private String adminPhone;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!seedEnabled) {
            logger.info("Admin seed initialization is disabled.");
            return;
        }

        boolean adminExists = userRepository.findAll()
                .stream()
                .anyMatch(u -> u.getRole() == UserRole.ADMIN);

        if (!adminExists) {
            logger.info("No ADMIN user found in database. Initializing default ADMIN user...");

            User admin = new User();
            admin.setName(adminName);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setPhone(adminPhone);
            admin.setRole(UserRole.ADMIN);

            userRepository.save(admin);
            logger.info("Default ADMIN user created successfully with email: {}", adminEmail);
        } else {
            logger.info("ADMIN user already exists in database. Skipping seed.");
        }
    }
}
