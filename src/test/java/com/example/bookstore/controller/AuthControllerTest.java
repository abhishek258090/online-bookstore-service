package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiDtos.Credentials;
import com.example.bookstore.dto.ApiDtos.LoginResponse;
import com.example.bookstore.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private Authentication authentication;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService);
    }

    @Test
    void register_shouldReturnSuccessMessage() {
        Credentials credentials =
                new Credentials("john@example.com", "password");

        doNothing().when(authService).register(credentials);

        String result = controller.register(credentials);

        assertEquals("Registration successful", result);
        verify(authService).register(credentials);
    }

    @Test
    void register_shouldPropagateExceptionWhenServiceFails() {
        Credentials credentials =
                new Credentials("john@example.com", "password");

        RuntimeException exception = new RuntimeException("Registration failed");

        doThrow(exception).when(authService).register(credentials);

        RuntimeException result =
                assertThrows(RuntimeException.class,
                        () -> controller.register(credentials));

        assertSame(exception, result);
        verify(authService).register(credentials);
    }

    @Test
    void login_shouldReturnLoginResponse() {
        String email = "john@example.com";
        LoginResponse response = new LoginResponse("jwt-token");

        when(authentication.getName()).thenReturn(email);
        when(authService.login(email)).thenReturn(response);

        LoginResponse result = controller.login(authentication);

        assertSame(response, result);
        verify(authentication).getName();
        verify(authService).login(email);
    }

    @Test
    void login_shouldPropagateServiceException() {
        String email = "john@example.com";

        when(authentication.getName()).thenReturn(email);
        when(authService.login(email))
                .thenThrow(new RuntimeException("Login failed"));

        assertThrows(RuntimeException.class,
                () -> controller.login(authentication));

        verify(authService).login(email);
    }
}