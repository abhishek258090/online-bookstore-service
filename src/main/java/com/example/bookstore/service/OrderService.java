package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.CheckoutRequest;
import com.example.bookstore.dto.ApiDtos.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderResponse checkout(String email, CheckoutRequest request);
    List<OrderResponse> orders(String email, Pageable pageable);
}
