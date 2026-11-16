package com.example.customerservice.config;

import com.example.customerservice.entity.User;
import com.example.customerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures demo user passwords always match the demo login credentials on application start:
 * - admin / admin123
 * - agent_sarah / agent123
 * - john_doe / customer123
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        updatePasswordIfPresent("admin", "admin123");
        updatePasswordIfPresent("agent_sarah", "agent123");
        updatePasswordIfPresent("agent_michael", "agent123");
        updatePasswordIfPresent("john_doe", "customer123");
        updatePasswordIfPresent("alice_smith", "customer123");
    }

    private void updatePasswordIfPresent(String username, String rawPassword) {
        userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .ifPresent(user -> {
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    userRepository.save(user);
                    log.info("Initialized demo credentials for '{}': {}", username, rawPassword);
                });
    }
}
