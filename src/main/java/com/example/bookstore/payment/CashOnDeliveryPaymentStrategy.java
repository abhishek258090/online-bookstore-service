package com.example.bookstore.payment;

import com.example.bookstore.dto.ApiDtos.PaymentMethod;
import com.example.bookstore.model.PurchaseOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CashOnDeliveryPaymentStrategy implements PaymentStrategy {
    private static final Logger log = LoggerFactory.getLogger(CashOnDeliveryPaymentStrategy.class);

    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return PaymentMethod.CASH_ON_DELIVERY == paymentMethod;
    }

    @Override
    public void pay(PurchaseOrder order) {
        log.info("payment_authorized method=CASH_ON_DELIVERY orderTotal={}", order.getTotal());
    }
}
