package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.AddCartItem;
import com.example.bookstore.dto.ApiDtos.CartResponse;
import com.example.bookstore.dto.ApiDtos.UpdateCartItem;

public interface CartService {
    CartResponse cart(String email);
    CartResponse add(String email, AddCartItem addCartItem);
    CartResponse updateQuantity(String email, Long bookId, UpdateCartItem updateCartItem);
    CartResponse remove(String email, Long bookId);
}
