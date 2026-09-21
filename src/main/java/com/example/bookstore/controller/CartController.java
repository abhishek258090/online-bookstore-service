package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiDtos.AddCartItem;
import com.example.bookstore.dto.ApiDtos.CartResponse;
import com.example.bookstore.dto.ApiDtos.UpdateCartItem;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.CartService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private final CurrentUser currentUser;

    public CartController(CartService cartService, CurrentUser currentUser) {
        this.cartService = cartService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public CartResponse get() {
        return cartService.cart(currentUser.email());
    }

    @PostMapping("/items")
    public CartResponse add(@Valid @RequestBody AddCartItem addCartItem) {
        return cartService.add(currentUser.email(), addCartItem);
    }

    @PutMapping("/items/{bookId}")
    public CartResponse updateQuantity(
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateCartItem updateCartItem) {
        return cartService.updateQuantity(currentUser.email(), bookId, updateCartItem);
    }

    @DeleteMapping("/items/{bookId}")
    public CartResponse remove(@PathVariable Long bookId) {
        return cartService.remove(currentUser.email(), bookId);
    }
}
