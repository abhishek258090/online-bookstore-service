package com.example.bookstore.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus httpStatus, String message) {
        super(message);
        status = httpStatus;
    }

    public HttpStatus status() {
        return status;
    }
}
