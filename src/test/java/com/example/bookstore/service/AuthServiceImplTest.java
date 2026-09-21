package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.Credentials;
import com.example.bookstore.dto.ApiDtos.LoginResponse;
import com.example.bookstore.exception.ApiException;
import com.example.bookstore.model.UserAccount;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.JwtService;
import com.example.bookstore.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void register_shouldCreateUserSuccessfully() {
        Credentials credentials =
                new Credentials("john@example.com", "password");

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password"))
                .thenReturn("encoded-password");
//        when(userRepository.save(UserAccount.builder()
//                        .passwordHash("encoded-password").email("john@example.com").build()))
//                .thenReturn(UserAccount.builder()
//                        .passwordHash("encoded-password").email("john@example.com").build());

        service.register(credentials);

        ArgumentCaptor<UserAccount> captor =
                ArgumentCaptor.forClass(UserAccount.class);

        verify(userRepository).save(captor.capture());

        UserAccount savedUser = captor.getValue();

        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPasswordHash());

        verify(passwordEncoder).encode("password");
    }

    @Test
    void register_shouldThrowExceptionWhenEmailAlreadyExists() {
        Credentials credentials =
                new Credentials("john@example.com", "password");

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(true);

        assertThrows(
                ApiException.class,
                () -> service.register(credentials)
        );

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void register_shouldNotSaveUserWhenPasswordEncodingFails() {
        Credentials credentials =
                new Credentials("john@example.com", "password");

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password"))
                .thenThrow(new RuntimeException("Encoding failed"));

        assertThrows(
                RuntimeException.class,
                () -> service.register(credentials)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldCreateLoginResponse() {
        String email = "john@example.com";
        String token = "jwt-token";

        when(jwtService.create(email))
                .thenReturn(token);

        LoginResponse result = service.login(email);

        assertNotNull(result);
        assertEquals(token, result.token());

        verify(jwtService).create(email);
    }

    @Test
    void login_shouldPropagateJwtException() {
        String email = "john@example.com";

        when(jwtService.create(email))
                .thenThrow(new RuntimeException("JWT generation failed"));

        assertThrows(
                RuntimeException.class,
                () -> service.login(email)
        );

        verify(jwtService).create(email);
    }
}