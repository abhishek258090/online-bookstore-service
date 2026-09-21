package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiDtos.CheckoutRequest;
import com.example.bookstore.dto.ApiDtos.OrderItemResponse;
import com.example.bookstore.dto.ApiDtos.OrderResponse;
import com.example.bookstore.dto.ApiDtos.PaymentMethod;
import com.example.bookstore.model.OrderStatus;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
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
class OrderControllerTest {
    @Mock private OrderService orderService;
    @Mock private CurrentUser currentUser;
    private OrderController controller;

    @BeforeEach
    void setUp() {
        controller = new OrderController(orderService, currentUser);
        when(currentUser.email()).thenReturn("john@example.com");
    }

    @Test
    void all_shouldReturnOrders() {
        PageRequest pageable = PageRequest.of(0, 20);
        OrderResponse response = new OrderResponse(
                10L, OrderStatus.PLACED.toString(), new BigDecimal(20),
                Instant.now(), List.of(new OrderItemResponse("Clean Architecture", BigDecimal.TEN, 2)));
        List<OrderResponse> orders = List.of(response);

        when(orderService.orders("john@example.com", pageable)).thenReturn(orders);

        assertSame(orders, controller.all(pageable));
        verify(orderService).orders("john@example.com", pageable);
    }

    @Test
    void all_shouldPropagateException() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(orderService.orders("john@example.com", pageable))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> controller.all(pageable));
    }

    @Test
    void checkout_shouldReturnCreatedOrder() {
        OrderResponse response = new OrderResponse(
                10L, OrderStatus.PLACED.toString(), new BigDecimal(20),
                Instant.now(), List.of());
        CheckoutRequest request = new CheckoutRequest(PaymentMethod.CARD);

        when(orderService.checkout("john@example.com", request)).thenReturn(response);

        assertSame(response, controller.checkout(request));
        verify(orderService).checkout("john@example.com", request);
    }

    @Test
    void checkout_withoutRequest_shouldUseDefaultPaymentMethod() {
        OrderResponse response = new OrderResponse(
                10L, OrderStatus.PLACED.toString(), new BigDecimal(20),
                Instant.now(), List.of());

        when(orderService.checkout(eq("john@example.com"), any(CheckoutRequest.class)))
                .thenReturn(response);

        assertSame(response, controller.checkout(null));
        verify(orderService).checkout(eq("john@example.com"), any(CheckoutRequest.class));
    }
}
