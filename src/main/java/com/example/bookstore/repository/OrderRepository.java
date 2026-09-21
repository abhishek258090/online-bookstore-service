package com.example.bookstore.repository;

import com.example.bookstore.model.PurchaseOrder;
import com.example.bookstore.model.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Page<PurchaseOrder> findByCustomer(UserAccount customer, Pageable pageable);
}
