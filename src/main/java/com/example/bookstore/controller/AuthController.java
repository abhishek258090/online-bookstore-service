package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiDtos.Credentials;
import com.example.bookstore.dto.ApiDtos.LoginResponse;
import com.example.bookstore.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public String register(@Valid @RequestBody Credentials credentials) {
        authService.register(credentials);
        return "Registration successful";
    }

    @PostMapping("/login")
    public LoginResponse login(Authentication authentication) {
        return authService.login(authentication.getName());
    }
}
