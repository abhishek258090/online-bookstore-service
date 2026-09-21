package com.example.bookstore.security;

import com.example.bookstore.constants.Constants;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextCurrentUser implements CurrentUser {
    @Override
    public String email() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            throw new IllegalStateException(Constants.AUTHENTICATED_USER_IS_REQUIRED_ERROR);
        }
        return authentication.getName();
    }
}
