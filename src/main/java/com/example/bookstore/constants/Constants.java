package com.example.bookstore.constants;

public interface Constants {
    String EMAIL_ALREADY_REGISTERED_ERROR = "Email already registered";
    String BOOK_NOT_FOUND_ERROR = "Book not found";
    String NOT_ENOUGH_STOCK_ERROR = "Not enough stock";
    String CART_ITEM_NOT_FOUND_ERROR = "Cart item not found";
    String CART_NOT_FOUND_ERROR = "Cart not found";
    String UNKNOWN_USER_ERROR = "Unknown user";
    String CAN_NOT_CHECKOUT_WITH_EMPTY_CART_ERROR = "Cannot checkout with an empty cart";
    String INSUFFICIENT_STOCK_FOR_BOOKS_ERROR = "Insufficient stock for books:";
    String NO_CART_FOUND_ERROR = "No cart found";
    String INVALID_JWT_ERROR = "Invalid JWT";
    String AUTHENTICATED_USER_IS_REQUIRED_ERROR = "Authenticated user is required";
    String RESERVATION_QUANTITY_MUST_BE_POSITIVE_ERROR = "Reservation quantity must be positive";
    String INSUFFICIENT_STOCK_FOR_BOOK_ERROR = "Insufficient stock for book: ";
    String LOCAL_ADMIN_ERROR = "Local admin credentials are missing. " +
            "Set BOOKSTORE_ADMIN_EMAIL and BOOKSTORE_ADMIN_PASSWORD.";
    String MISING_QUANTITY_FOR_BOOK = "Missing quantity for book ";
    String UNSUPPORTED_PAYMENT_METHOD = "Unsupported payment method: ";


    String AUTHORIZATION = "Authorization";
    String BEARER = "Bearer ";
}
