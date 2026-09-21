package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiDtos.CheckoutRequest;
import com.example.bookstore.dto.ApiDtos.OrderResponse;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final CurrentUser currentUser;

    public OrderController(OrderService orderService, CurrentUser currentUser) {
        this.orderService = orderService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<OrderResponse> all(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return orderService.orders(currentUser.email(), pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(
            @RequestBody(required = false) CheckoutRequest request) {
        CheckoutRequest effectiveRequest = request == null
                ? new CheckoutRequest(null)
                : request;
        return orderService.checkout(currentUser.email(), effectiveRequest);
    }
}
