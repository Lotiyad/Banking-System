package com.example.banking.config;

import com.example.banking.repository.UserRepository;
import com.example.banking.status.Role;
import com.example.banking.entity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner initializeAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultAdminUsername = "admin";
            String defaultAdminPassword = "";

            Optional<User> adminOptional = userRepository.findByUsername(defaultAdminUsername);
            if (adminOptional.isEmpty()) {
                User admin = new User();
                admin.setUsername(defaultAdminUsername);
                admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
                admin.setRole(Role.ADMIN);
                admin.setAccountNonLocked(true);
                admin.setFailedAttempt(0);
                admin.setLockTime(null);
                admin.setApproved(true);
                userRepository.save(admin);
                System.out.println("✅  Admin account created: username = admin, password = admin123");
            }
        };
    }
}
