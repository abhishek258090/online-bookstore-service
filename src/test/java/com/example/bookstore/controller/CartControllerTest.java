package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiDtos.AddCartItem;
import com.example.bookstore.dto.ApiDtos.CartItemResponse;
import com.example.bookstore.dto.ApiDtos.CartResponse;
import com.example.bookstore.dto.ApiDtos.UpdateCartItem;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

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
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private CurrentUser currentUser;

    private CartController controller;

    @BeforeEach
    void setUp() {
        controller = new CartController(cartService, currentUser);

        when(currentUser.email()).thenReturn("john@example.com");
    }

    @Test
    void get_shouldReturnCart() {
        CartItemResponse cartItemResponse1 = new CartItemResponse(10L, "Clean Code", BigDecimal.valueOf(10.0), 2);
        CartItemResponse cartItemResponse2 = new CartItemResponse(11L, "Effective Java", BigDecimal.valueOf(20.0), 3);
        CartResponse response =
                new CartResponse(List.of(cartItemResponse1, cartItemResponse2),new BigDecimal(80));

        when(cartService.cart("john@example.com"))
                .thenReturn(response);

        CartResponse result = controller.get();

        assertSame(response, result);

        verify(currentUser).email();
        verify(cartService).cart("john@example.com");
    }

    @Test
    void get_shouldPropagateServiceException() {
        when(cartService.cart("john@example.com"))
                .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class,
                () -> controller.get());

        verify(cartService).cart("john@example.com");
    }

    @Test
    void add_shouldAddItemToCart() {
        CartItemResponse cartItemResponse1 = new CartItemResponse(10L, "Clean Code", BigDecimal.valueOf(10.0), 2);
        AddCartItem request = new AddCartItem(10L, 2);

        CartResponse response =
                new CartResponse(List.of(cartItemResponse1), new BigDecimal(20));

        when(cartService.add("john@example.com", request))
                .thenReturn(response);

        CartResponse result = controller.add(request);

        assertSame(response, result);

        verify(cartService).add("john@example.com", request);
    }

    @Test
    void add_shouldPropagateException() {
        AddCartItem request = new AddCartItem(10L, 2);

        when(cartService.add("john@example.com", request))
                .thenThrow(new RuntimeException("Book not found"));

        assertThrows(RuntimeException.class,
                () -> controller.add(request));

        verify(cartService).add("john@example.com", request);
    }

    @Test
    void updateQuantity_shouldUpdateCartItem() {
        Long bookId = 10L;
        UpdateCartItem request = new UpdateCartItem(3);
        CartItemResponse cartItemResponse1 = new CartItemResponse(10L, "Clean Code", BigDecimal.valueOf(10.0), 3);
        CartResponse response =
                new CartResponse(List.of(cartItemResponse1), new BigDecimal(30));

        when(cartService.updateQuantity(
                "john@example.com", bookId, request))
                .thenReturn(response);

        CartResponse result =
                controller.updateQuantity(bookId, request);

        assertSame(response, result);

        verify(cartService)
                .updateQuantity("john@example.com", bookId, request);
    }

    @Test
    void updateQuantity_shouldPropagateException() {
        Long bookId = 10L;
        UpdateCartItem request = new UpdateCartItem(3);

        when(cartService.updateQuantity(
                "john@example.com", bookId, request))
                .thenThrow(new RuntimeException("Cart item not found"));

        assertThrows(RuntimeException.class,
                () -> controller.updateQuantity(bookId, request));

        verify(cartService)
                .updateQuantity("john@example.com", bookId, request);
    }

    @Test
    void remove_shouldRemoveItem() {
        Long bookId = 10L;

        CartResponse response =
                new CartResponse(List.of(), BigDecimal.ZERO);

        when(cartService.remove("john@example.com", bookId))
                .thenReturn(response);

        CartResponse result = controller.remove(bookId);

        assertSame(response, result);

        verify(cartService)
                .remove("john@example.com", bookId);
    }

    @Test
    void remove_shouldPropagateException() {
        Long bookId = 10L;

        when(cartService.remove("john@example.com", bookId))
                .thenThrow(new RuntimeException("Cart item not found"));

        assertThrows(RuntimeException.class,
                () -> controller.remove(bookId));

        verify(cartService)
                .remove("john@example.com", bookId);
    }
}