package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.AddCartItem;
import com.example.bookstore.dto.ApiDtos.CartItemResponse;
import com.example.bookstore.dto.ApiDtos.CartResponse;
import com.example.bookstore.dto.ApiDtos.UpdateCartItem;
import com.example.bookstore.exception.ApiException;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Cart;
import com.example.bookstore.model.CartItem;
import com.example.bookstore.model.UserAccount;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
class CartServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImplTest.class);
    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CartRepository cartRepository;

    private CartServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CartServiceImpl(
                userRepository,
                bookRepository,
                cartRepository
        );
    }

    @Test
    void cart_shouldReturnExistingCart() {
        String email = "john@example.com";
        UserAccount user = mock(UserAccount.class);
        Cart cart = Mockito.spy(new Cart(user));
        Book book1 = new Book("Clean Code", "Robert Martin", BigDecimal.valueOf(10.0),10);
        CartItem cartItem = new CartItem(cart, book1, 3);
        cart.getItems().add(cartItem);
        CartResponse cartResponse = new CartResponse(List.of(new CartItemResponse(10L,"Clean Code",BigDecimal.TEN,3)),new BigDecimal(30));
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByCustomer(user))
                .thenReturn(Optional.of(cart));

        when(cart.getItems())
                .thenReturn(List.of(cartItem));

        CartResponse result = service.cart(email);

        assertNotNull(result);
        assertEquals(cartResponse.items().get(0).title(), result.items().get(0).title());
        assertEquals(cartResponse.total().intValue(), result.total().intValue());

        verify(cartRepository).findByCustomer(user);
    }

    @Test
    void cart_shouldCreateCartWhenCartDoesNotExist() {
        String email = "john@example.com";

        UserAccount user = mock(UserAccount.class);
        Cart newCart = mock(Cart.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByCustomer(user))
                .thenReturn(Optional.empty());

        when(cartRepository.save(any(Cart.class)))
                .thenReturn(newCart);

        when(newCart.getItems())
                .thenReturn(List.of());

        CartResponse result = service.cart(email);

        assertNotNull(result);
        assertTrue(result.items().isEmpty());
        assertEquals(BigDecimal.ZERO, result.total());

        verify(cartRepository).save(any(Cart.class));
    }


    @Test
    void add_shouldThrowNotFoundWhenBookDoesNotExist() {
        String email = "john@example.com";
        AddCartItem request = new AddCartItem(100L, 2);

        when(bookRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ApiException.class,
                () -> service.add(email, request)
        );

        verify(bookRepository).findById(100L);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void add_shouldThrowBadRequestWhenStockIsInsufficient() {
        String email = "john@example.com";

        AddCartItem request = new AddCartItem(100L, 5);

        Book book = mock(Book.class);

        when(bookRepository.findById(100L))
                .thenReturn(Optional.of(book));

        when(book.getStock())
                .thenReturn(2);

        assertThrows(
                ApiException.class,
                () -> service.add(email, request)
        );

        verify(book).getStock();
        verify(userRepository, never()).findByEmail(anyString());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void add_shouldAddBookToExistingCart() {
        String email = "john@example.com";

        AddCartItem request = new AddCartItem(10L, 3);

        UserAccount user = mock(UserAccount.class);
        Cart cart = Mockito.spy(new Cart(user));
        Book book1 = new Book("Clean Code", "Robert Martin", BigDecimal.valueOf(10.0),10);
        CartItem cartItem = new CartItem(cart, book1, 3);
        cart.getItems().add(cartItem);
        CartResponse cartResponse = new CartResponse(List.of(new CartItemResponse(10L,"Clean Code",BigDecimal.TEN,3)),new BigDecimal(30));

        when(bookRepository.findById(10L))
                .thenReturn(Optional.of(book1));

//        when(book.getStock())
//                .thenReturn(10);
        doNothing().when(cart).add(book1, 3);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByCustomer(user))
                .thenReturn(Optional.of(cart));

        when(cartRepository.save(cart))
                .thenReturn(cart);

        when(cart.getItems())
                .thenReturn(List.of(cartItem));

        CartResponse result = service.add(email, request);

        assertNotNull(result);
        assertEquals(cartResponse.items().get(0).title(), result.items().get(0).title());
        assertEquals(cartResponse.total().intValue(), result.total().intValue());

        verify(cart).add(book1, 3);
        verify(cartRepository).save(cart);
    }

    @Test
    void updateQuantity_shouldThrowWhenCartDoesNotExist() {
        String email = "john@example.com";

        UpdateCartItem request = new UpdateCartItem(2);

        UserAccount user = mock(UserAccount.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByCustomer(user))
                .thenReturn(Optional.empty());

        assertThrows(
                ApiException.class,
                () -> service.updateQuantity(email, 100L, request)
        );

        verify(cartRepository, never()).save(any());
    }

    @Test
    void updateQuantity_shouldThrowWhenCartItemDoesNotExist() {
        String email = "john@example.com";

        UpdateCartItem request = new UpdateCartItem(2);

        UserAccount user = mock(UserAccount.class);
        Cart cart = mock(Cart.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByCustomer(user))
                .thenReturn(Optional.of(cart));

        when(cart.updateQuantity(100L, 2))
                .thenReturn(false);

        assertThrows(
                ApiException.class,
                () -> service.updateQuantity(email, 100L, request)
        );

        verify(cart, never()).clear();
        verify(cartRepository, never()).save(any());
    }

    @Test
    void updateQuantity_shouldUpdateExistingCartItem() {
        String email = "john@example.com";

        UpdateCartItem request = new UpdateCartItem(3);

        UserAccount user = mock(UserAccount.class);
        Cart cart = Mockito.spy(new Cart(user));
        Book book1 = new Book("Clean Code", "Robert Martin", BigDecimal.valueOf(10.0),10);
        CartItem cartItem = new CartItem(cart, book1, 3);
        cart.getItems().add(cartItem);
        CartResponse cartResponse = new CartResponse(List.of(new CartItemResponse(10L,"Clean Code",BigDecimal.TEN,3)),new BigDecimal(30));


        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByCustomer(user))
                .thenReturn(Optional.of(cart));

        when(cart.updateQuantity(10L, 3))
                .thenReturn(true);

        when(cartRepository.save(cart))
                .thenReturn(cart);

        when(cart.getItems())
                .thenReturn(List.of(cartItem));

        CartResponse result =
                service.updateQuantity(email, 10L, request);

        assertNotNull(result);
        assertEquals(cartResponse.items().get(0).title(), result.items().get(0).title());
        assertEquals(cartResponse.total().intValue(), result.total().intValue());

        verify(cart).updateQuantity(10L, 3);
        verify(cartRepository).save(cart);
    }

    @Test
    void remove_shouldThrowWhenCartItemDoesNotExist() {
        String email = "john@example.com";

        UserAccount user = mock(UserAccount.class);
        Cart cart = mock(Cart.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByCustomer(user))
                .thenReturn(Optional.of(cart));

        when(cart.remove(100L))
                .thenReturn(false);

        assertThrows(
                ApiException.class,
                () -> service.remove(email, 100L)
        );

        verify(cartRepository, never()).save(any());
    }

    @Test
    void remove_shouldRemoveExistingItem() {
        String email = "john@example.com";

        UserAccount user = mock(UserAccount.class);
        Cart cart = mock(Cart.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByCustomer(user))
                .thenReturn(Optional.of(cart));

        when(cart.remove(100L))
                .thenReturn(true);

        when(cartRepository.save(cart))
                .thenReturn(cart);

        when(cart.getItems())
                .thenReturn(List.of());

        CartResponse result =
                service.remove(email, 100L);

        assertNotNull(result);

        verify(cart).remove(100L);
        verify(cartRepository).save(cart);
    }
}