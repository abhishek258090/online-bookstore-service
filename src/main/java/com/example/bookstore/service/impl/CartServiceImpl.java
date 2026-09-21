package com.example.bookstore.service.impl;

import com.example.bookstore.constants.Constants;
import com.example.bookstore.dto.ApiDtos.AddCartItem;
import com.example.bookstore.dto.ApiDtos.CartItemResponse;
import com.example.bookstore.dto.ApiDtos.CartResponse;
import com.example.bookstore.dto.ApiDtos.UpdateCartItem;
import com.example.bookstore.exception.ApiException;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Cart;
import com.example.bookstore.model.UserAccount;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class CartServiceImpl implements CartService {
    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CartRepository cartRepository;

    public CartServiceImpl(UserRepository userRepository,
                           BookRepository bookRepository,
                           CartRepository cartRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.cartRepository = cartRepository;
    }

    public CartResponse cart(String email) {
        return toResponse(findOrCreate(email));
    }

    public CartResponse add(String email, AddCartItem addCartItem) {
        Book book = bookRepository.findById(addCartItem.bookId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, Constants.BOOK_NOT_FOUND_ERROR));

        if (book.getStock() < addCartItem.quantity()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, Constants.NOT_ENOUGH_STOCK_ERROR);
        }

        Cart cart = findOrCreate(email);
        cart.add(book, addCartItem.quantity());
        CartResponse response = toResponse(cartRepository.save(cart));
        log.info("cart_item_added customer={} bookId={} quantity={}",
                email, addCartItem.bookId(), addCartItem.quantity());
        return response;
    }

    public CartResponse updateQuantity(String email, Long bookId, UpdateCartItem updateCartItem) {
        Cart cart = findExisting(email);
        if (!cart.updateQuantity(bookId, updateCartItem.quantity())) {
            throw new ApiException(HttpStatus.NOT_FOUND, Constants.CART_ITEM_NOT_FOUND_ERROR);
        }

        CartResponse response = toResponse(cartRepository.save(cart));
        log.info("cart_item_updated customer={} bookId={} quantity={}",
                email, bookId, updateCartItem.quantity());
        return response;
    }

    public CartResponse remove(String email, Long bookId) {
        Cart cart = findExisting(email);
        if (!cart.remove(bookId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, Constants.CART_ITEM_NOT_FOUND_ERROR);
        }

        CartResponse response = toResponse(cartRepository.save(cart));
        log.info("cart_item_removed customer={} bookId={}", email, bookId);
        return response;
    }

    private Cart findOrCreate(String email) {
        UserAccount userAccount = user(email);
        return cartRepository.findByCustomer(userAccount)
                .orElseGet(() -> cartRepository.save(new Cart(userAccount)));
    }

    private Cart findExisting(String email) {
        return cartRepository.findByCustomer(user(email))
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, Constants.CART_NOT_FOUND_ERROR));
    }

    private UserAccount user(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED, Constants.UNKNOWN_USER_ERROR));
    }

    static CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream()
                .map(i -> new CartItemResponse(
                        i.getBook().getId(),
                        i.getBook().getTitle(),
                        i.getBook().getPrice(),
                        i.getQuantity()))
                .toList();

        BigDecimal total = items.stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(items, total);
    }
}
