package com.example.bookstore.model;

import com.example.bookstore.constants.Constants;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "orders")
public class PurchaseOrder extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private UserAccount customer;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    protected PurchaseOrder() {
    }

    public PurchaseOrder(UserAccount customer) {
        this.customer = customer;
    }

    private PurchaseOrder(Builder builder) {
        this(builder.customer);
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UserAccount customer;
        private OrderStatus status = OrderStatus.PLACED;

        public Builder customer(UserAccount customer) {
            this.customer = customer;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public PurchaseOrder build() {
            return new PurchaseOrder(this);
        }
    }

    /**
     * Factory method keeps order assembly out of the checkout orchestration.
     * Prices and titles are copied into immutable order-line snapshots.
     */
    public static PurchaseOrder createFrom(UserAccount customer,
                                            List<Book> books,
                                            Map<Long, Integer> quantities) {
        PurchaseOrder order = PurchaseOrder.builder().customer(customer).build();
        for (Book book : books) {
            Integer quantity = quantities.get(book.getId());
            if (quantity == null) {
                throw new IllegalArgumentException(Constants.MISING_QUANTITY_FOR_BOOK + book.getId());
            }
            book.reserve(quantity);
            order.addItem(book, quantity);
        }
        return order;
    }

    private void addItem(Book book, int quantity) {
        items.add(OrderItem.builder()
                .order(this)
                .title(book.getTitle())
                .unitPrice(book.getPrice())
                .quantity(quantity)
                .build());
        total = total.add(book.getPrice().multiply(BigDecimal.valueOf(quantity)));
    }

    public Long getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }
}
