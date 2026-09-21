package com.example.bookstore.config;

import com.example.bookstore.constants.Constants;
import com.example.bookstore.model.Role;
import com.example.bookstore.model.UserAccount;
import com.example.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("local")
public class DataInitializer {

    @Bean
    CommandLineRunner localAdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword) {

        return args -> {
            if (adminEmail.isBlank() || adminPassword.isBlank()) {
                throw new IllegalStateException(
                        Constants.LOCAL_ADMIN_ERROR);
            }

            if (userRepository.existsByEmail(adminEmail.toLowerCase())) {
                return;
            }

            UserAccount admin = UserAccount.builder()
                    .email(adminEmail.toLowerCase())
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
        };
    }
}
