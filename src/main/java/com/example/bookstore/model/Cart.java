package com.example.bookstore.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Cart extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private UserAccount customer;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {
    }

    public Cart(UserAccount customer) {
        this.customer = customer;
    }

    private Cart(Builder builder) {
        this(builder.customer);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UserAccount customer;

        public Builder customer(UserAccount customer) {
            this.customer = customer;
            return this;
        }

        public Cart build() {
            return new Cart(this);
        }
    }

    public void add(Book book, int quantity) {
        items.stream()
                .filter(i -> Objects.equals(i.getBook().getId(), book.getId()))
                .findFirst()
                .ifPresentOrElse(i -> i.increase(quantity),
                        () -> items.add(new CartItem(this, book, quantity)));
    }

    public boolean updateQuantity(Long bookId, int quantity) {
        return items.stream()
                .filter(i -> Objects.equals(i.getBook().getId(), bookId))
                .findFirst()
                .map(i -> {
                    i.changeQuantityTo(quantity);
                    return true;
                })
                .orElse(false);
    }

    public boolean remove(Long bookId) {
        return items.removeIf(i -> Objects.equals(i.getBook().getId(), bookId));
    }

    public void clear() {
        items.clear();
    }

    public Long getId() {
        return id;
    }

    public List<CartItem> getItems() {
        return items;
    }
}
