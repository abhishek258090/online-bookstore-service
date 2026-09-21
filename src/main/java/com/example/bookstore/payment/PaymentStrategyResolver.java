package com.example.bookstore.payment;

import com.example.bookstore.constants.Constants;
import com.example.bookstore.dto.ApiDtos.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentStrategyResolver {
    private final List<PaymentStrategy> strategies;

    public PaymentStrategyResolver(List<PaymentStrategy> strategies) {
        this.strategies = strategies;
    }

    public PaymentStrategy resolve(PaymentMethod paymentMethod) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(paymentMethod))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        Constants.UNSUPPORTED_PAYMENT_METHOD + paymentMethod));
    }
}
