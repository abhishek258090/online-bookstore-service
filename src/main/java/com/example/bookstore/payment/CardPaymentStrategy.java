package com.example.bookstore.payment;

import com.example.bookstore.dto.ApiDtos.PaymentMethod;
import com.example.bookstore.model.PurchaseOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentStrategy implements PaymentStrategy {
    private static final Logger log = LoggerFactory.getLogger(CardPaymentStrategy.class);

    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return PaymentMethod.CARD == paymentMethod;
    }

    @Override
    public void pay(PurchaseOrder order) {
        // Replace with the real card gateway integration.
        log.info("payment_processed method=CARD orderTotal={}", order.getTotal());
    }
}
