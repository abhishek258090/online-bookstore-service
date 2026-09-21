package com.example.bookstore.exception;

/**
 * A checkout cannot reserve the requested quantity from the current inventory.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
