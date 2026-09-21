package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.CheckoutRequest;
import com.example.bookstore.dto.ApiDtos.OrderResponse;
import com.example.bookstore.dto.ApiDtos.PaymentMethod;
import com.example.bookstore.exception.ApiException;
import com.example.bookstore.exception.InsufficientStockException;
import com.example.bookstore.model.*;
import com.example.bookstore.payment.PaymentStrategy;
import com.example.bookstore.payment.PaymentStrategyResolver;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private CartRepository cartRepository;
    @Mock private BookRepository bookRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private PaymentStrategyResolver paymentStrategyResolver;
    @Mock private PaymentStrategy paymentStrategy;

    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(
                userRepository, cartRepository, bookRepository,
                orderRepository, paymentStrategyResolver);
        //when(paymentStrategyResolver.resolve(any())).thenReturn(paymentStrategy);
    }

    @Test
    void checkout_shouldThrowBadRequestWhenCartDoesNotExist() {
        UserAccount user = mock(UserAccount.class);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomerForCheckout(user)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> service.checkout("john@example.com", new CheckoutRequest(null)));

        assertEquals("No cart found", ex.getMessage());
        verify(bookRepository, never()).findAllByIdInForCheckout(any());
    }

    @Test
    void checkout_shouldLockCartBeforeEmptyCartValidation() {
        UserAccount user = mock(UserAccount.class);
        Cart cart = mock(Cart.class);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomerForCheckout(user)).thenReturn(Optional.of(cart));
        when(cart.getItems()).thenReturn(List.of());

        assertThrows(ApiException.class,
                () -> service.checkout("john@example.com", new CheckoutRequest(null)));

        verify(cartRepository).findByCustomerForCheckout(user);
        verify(bookRepository, never()).findAllByIdInForCheckout(any());
    }

    @Test
    void checkout_shouldSuccessfullyCreateOrderAndProcessPayment() {
        Book book = spy(new Book("Domain-Driven Design", "Author",
                new BigDecimal("50.00"), 3));
        UserAccount user = mock(UserAccount.class);
        Cart cart = new Cart(user);
        cart.add(book, 2);

        when(book.getId()).thenReturn(1L);
        when(userRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomerForCheckout(user)).thenReturn(Optional.of(cart));
        when(bookRepository.findAllByIdInForCheckout(List.of(1L))).thenReturn(List.of(book));
        when(paymentStrategyResolver.resolve(any())).thenReturn(paymentStrategy);
        when(orderRepository.saveAndFlush(any(PurchaseOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse result = service.checkout(
                "reader@example.com", new CheckoutRequest(PaymentMethod.CARD));

        assertEquals(new BigDecimal("100.00"), result.total());
        assertEquals(1, book.getStock());
        assertTrue(cart.getItems().isEmpty());

        verify(paymentStrategyResolver).resolve(PaymentMethod.CARD);
        verify(paymentStrategy).pay(any(PurchaseOrder.class));
        verify(orderRepository).saveAndFlush(any(PurchaseOrder.class));
    }

    @Test
    void checkout_shouldValidateStockAfterBookLock() {
        Book book = spy(new Book("Domain-Driven Design", "Author",
                new BigDecimal("50.00"), 1));
        UserAccount user = mock(UserAccount.class);
        Cart cart = new Cart(user);
        cart.add(book, 2);

        when(book.getId()).thenReturn(1L);
        when(userRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomerForCheckout(user)).thenReturn(Optional.of(cart));
        when(bookRepository.findAllByIdInForCheckout(List.of(1L))).thenReturn(List.of(book));
        //when(paymentStrategyResolver.resolve(any())).thenReturn(paymentStrategy);

        InsufficientStockException ex = assertThrows(
                InsufficientStockException.class,
                () -> service.checkout("reader@example.com", new CheckoutRequest(null)));

        assertEquals("Insufficient stock for books:Domain-Driven Design", ex.getMessage());
        verify(orderRepository, never()).saveAndFlush(any(PurchaseOrder.class));
    }

    @Test
    void orders_shouldReturnPaginatedOrders() {
        String email = "john@example.com";
        UserAccount user = mock(UserAccount.class);
        PurchaseOrder po = mock(PurchaseOrder.class);
        Instant created = Instant.now();

        when(po.getId()).thenReturn(1L);
        when(po.getStatus()).thenReturn(OrderStatus.PLACED);
        when(po.getTotal()).thenReturn(new BigDecimal("20"));
        when(po.getCreatedAt()).thenReturn(created);
        when(po.getItems()).thenReturn(List.of(
                new OrderItem(po, "New book", BigDecimal.TEN, 2)));

        PageRequest pageable = PageRequest.of(0, 10);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(orderRepository.findByCustomer(user, pageable))
                .thenReturn(new PageImpl<>(List.of(po), pageable, 1));

        List<OrderResponse> result = service.orders(email, pageable);

        assertEquals(1, result.size());
        OrderResponse response = result.get(0);
        assertEquals("New book", response.items().get(0).title());
        verify(orderRepository).findByCustomer(user, pageable);
    }
}
