package com.example.bookstore.service.impl;

import com.example.bookstore.constants.Constants;
import com.example.bookstore.dto.ApiDtos.Credentials;
import com.example.bookstore.dto.ApiDtos.LoginResponse;
import com.example.bookstore.exception.ApiException;
import com.example.bookstore.model.UserAccount;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.JwtService;
import com.example.bookstore.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public void register(Credentials credentials) {
        String email = credentials.email().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, Constants.EMAIL_ALREADY_REGISTERED_ERROR);
        }

        userRepository.save(UserAccount.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(credentials.password()))
                .build());

        log.info("user_registered email={}", email);
    }

    @Override
    public LoginResponse login(String email) {
        return new LoginResponse(jwtService.create(email));
    }
}
