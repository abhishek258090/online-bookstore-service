package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.Credentials;
import com.example.bookstore.dto.ApiDtos.LoginResponse;

public interface AuthService {
    void register(Credentials credentials);
    LoginResponse login(String email);
}
