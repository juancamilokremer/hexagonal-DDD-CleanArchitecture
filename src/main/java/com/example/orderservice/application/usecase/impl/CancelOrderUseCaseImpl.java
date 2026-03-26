package com.example.orderservice.application.usecase.impl;

import com.example.orderservice.application.usecase.CancelOrderUseCase;
import com.example.orderservice.domain.exception.OrderNotFoundException;
import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.repository.OrderRepository;

import java.util.UUID;

/**
 * Implementation of the CancelOrder use case.
 *
 * Orchestrates application logic:
 *   1. Retrieves the order — throws OrderNotFoundException if it does not exist.
 *   2. Invokes order.cancel() — business logic and guard clause live in the domain.
 *   3. Persists the updated state.
 *
 * Contains no business logic — that responsibility belongs to Order.
 * No framework annotations: Spring registers this bean from ApplicationConfig.
 */
public class CancelOrderUseCaseImpl implements CancelOrderUseCase {

    private final OrderRepository orderRepository;

    public CancelOrderUseCaseImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order execute(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.cancel(); // throws InvalidOrderStateException if already shipped or cancelled

        return orderRepository.save(order);
    }
}
