package com.example.bookstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {
    }

    public record BookResponse(Long id, String title, String author, BigDecimal price, int stock) {
    }

    public record CreateBookRequest(
            @NotBlank String title,
            @NotBlank String author,
            @NotNull @Positive BigDecimal price,
            @Min(0) int stock) {

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private String title;
            private String author;
            private BigDecimal price;
            private int stock;

            public Builder title(String title) {
                this.title = title;
                return this;
            }

            public Builder author(String author) {
                this.author = author;
                return this;
            }

            public Builder price(BigDecimal price) {
                this.price = price;
                return this;
            }

            public Builder stock(int stock) {
                this.stock = stock;
                return this;
            }

            public CreateBookRequest build() {
                return new CreateBookRequest(title, author, price, stock);
            }
        }
    }

    public record UpdateBookStock(@Min(0) int stock) {
        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private int stock;

            public Builder stock(int stock) {
                this.stock = stock;
                return this;
            }

            public UpdateBookStock build() {
                return new UpdateBookStock(stock);
            }
        }
    }

    public record Credentials(@Email @NotBlank String email, @Size(min = 8, max = 72) String password) {
    }

    public record LoginResponse(String token) {
    }

    public record CartItemResponse(Long bookId, String title, BigDecimal unitPrice, int quantity) {
    }

    public record AddCartItem(long bookId, @Min(1) @Max(100) int quantity) {
    }

    public record UpdateCartItem(@Min(1) @Max(100) int quantity) {
    }

    public record CartResponse(List<CartItemResponse> items, BigDecimal total) {
    }

    public record CheckoutRequest(PaymentMethod paymentMethod) {
        public CheckoutRequest {
            if (paymentMethod == null) {
                paymentMethod = PaymentMethod.CARD;
            }
        }
    }

    public record OrderItemResponse(String title, BigDecimal unitPrice, int quantity) {
    }

    public record OrderResponse(Long id, String status, BigDecimal total, Instant createdAt,
                                List<OrderItemResponse> items) {
    }

    public enum PaymentMethod {
        CARD,
        CASH_ON_DELIVERY
    }
}
