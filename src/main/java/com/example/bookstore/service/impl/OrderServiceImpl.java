package com.example.bookstore.service.impl;

import com.example.bookstore.constants.Constants;
import com.example.bookstore.dto.ApiDtos.CheckoutRequest;
import com.example.bookstore.dto.ApiDtos.OrderItemResponse;
import com.example.bookstore.dto.ApiDtos.OrderResponse;
import com.example.bookstore.exception.ApiException;
import com.example.bookstore.exception.InsufficientStockException;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Cart;
import com.example.bookstore.model.CartItem;
import com.example.bookstore.model.UserAccount;
import com.example.bookstore.model.PurchaseOrder;
import com.example.bookstore.payment.PaymentStrategyResolver;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final PaymentStrategyResolver paymentStrategyResolver;

    public OrderServiceImpl(UserRepository userRepository,
                            CartRepository cartRepository,
                            BookRepository bookRepository,
                            OrderRepository orderRepository,
                            PaymentStrategyResolver paymentStrategyResolver) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.paymentStrategyResolver = paymentStrategyResolver;
    }

    @Override
    @Transactional
    public OrderResponse checkout(String email, CheckoutRequest request) {
        UserAccount user = findUser(email);

        // Lock the cart BEFORE checking whether it is empty or validating stock.
        // A second checkout for the same cart therefore waits for the first
        // transaction to clear the cart and commit.
        Cart cart = cartRepository.findByCustomerForCheckout(user)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, Constants.NO_CART_FOUND_ERROR));

        if (cart.getItems().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    Constants.CAN_NOT_CHECKOUT_WITH_EMPTY_CART_ERROR);
        }

        Map<Long, Integer> quantities = cart.getItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getBook().getId(),
                        CartItem::getQuantity));

        List<Long> bookIds = quantities.keySet().stream().sorted().toList();

        // Book locks are acquired in deterministic ID order to reduce deadlock risk.
        Map<Long, Book> lockedBooks = bookRepository.findAllByIdInForCheckout(bookIds)
                .stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        if (lockedBooks.size() != bookIds.size()) {
            throw new ApiException(HttpStatus.NOT_FOUND, Constants.BOOK_NOT_FOUND_ERROR);
        }

        String str = validateQuantities(cart);
        if (!str.isEmpty()) {
            throw new InsufficientStockException(Constants.INSUFFICIENT_STOCK_FOR_BOOKS_ERROR +str);
        }

        PurchaseOrder order = PurchaseOrder.createFrom(
                user,
                bookIds.stream().map(lockedBooks::get).toList(),
                quantities);

        paymentStrategyResolver.resolve(request.paymentMethod()).pay(order);

        // Cart lock is held until transaction commit, so the clear is atomic
        // with stock reservation and order persistence.
        cart.clear();
        PurchaseOrder saved = orderRepository.saveAndFlush(order);

        log.info("order_placed orderId={} customer={} total={} paymentMethod={}",
                saved.getId(), email, saved.getTotal(), request.paymentMethod());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> orders(String email, Pageable pageable) {
        UserAccount user = findUser(email);
        return orderRepository
                .findByCustomer(user, pageable)
                .map(OrderServiceImpl::toResponse).getContent();
    }

    private UserAccount findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED, Constants.UNKNOWN_USER_ERROR));
    }

    static OrderResponse toResponse(PurchaseOrder purchaseOrder) {
        return new OrderResponse(
                purchaseOrder.getId(),
                purchaseOrder.getStatus().name(),
                purchaseOrder.getTotal(),
                purchaseOrder.getCreatedAt(),
                purchaseOrder.getItems().stream()
                        .map(i -> new OrderItemResponse(
                                i.getTitle(), i.getUnitPrice(), i.getQuantity()))
                        .toList());
    }

    private String validateQuantities(Cart cart){

        return cart.getItems().stream()
                .filter(e -> e.getBook().getStock() < e.getQuantity())
                .map(e -> e.getBook().getTitle())
                .collect(Collectors.joining(","));
    }
}
