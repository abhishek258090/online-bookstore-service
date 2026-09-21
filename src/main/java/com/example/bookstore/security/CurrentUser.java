package com.example.bookstore.security;

/**
 * Keeps framework-specific security context access out of web controllers.
 */
public interface CurrentUser {
    String email();
}
