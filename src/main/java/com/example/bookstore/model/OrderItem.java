package com.example.bookstore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

@Entity
public class OrderItem extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private PurchaseOrder order;

    private String title;
    private BigDecimal unitPrice;
    private int quantity;

    protected OrderItem() {
    }

    public OrderItem(PurchaseOrder order, String title, BigDecimal unitPrice, int quantity) {
        this.order = order;
        this.title = title;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    private OrderItem(Builder builder) {
        this(builder.order, builder.title, builder.unitPrice, builder.quantity);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private PurchaseOrder order;
        private String title;
        private BigDecimal unitPrice;
        private int quantity;

        public Builder order(PurchaseOrder order) {
            this.order = order;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder unitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public OrderItem build() {
            return new OrderItem(this);
        }
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}
