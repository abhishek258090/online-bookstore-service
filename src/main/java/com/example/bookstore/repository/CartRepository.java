package com.example.bookstore.repository;

import com.example.bookstore.model.Cart;
import com.example.bookstore.model.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByCustomer(UserAccount customer);

    /**
     * Locks the cart before any checkout validation. This serializes concurrent
     * checkout attempts for the same customer/cart.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.customer = :customer")
    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByCustomerForCheckout(@Param("customer") UserAccount customer);
}
