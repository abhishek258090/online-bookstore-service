package com.example.bookstore.payment;

import com.example.bookstore.dto.ApiDtos.PaymentMethod;
import com.example.bookstore.model.PurchaseOrder;

public interface PaymentStrategy {
    boolean supports(PaymentMethod paymentMethod);
    void pay(PurchaseOrder order);
}
