package com.example.orderservice.application.usecase.impl;

import com.example.orderservice.application.usecase.GetOrderUseCase;
import com.example.orderservice.domain.exception.OrderNotFoundException;
import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.repository.OrderRepository;

import java.util.UUID;

/**
 * Implementation of the GetOrder use case.
 *
 * Orchestrates application logic:
 *   1. Retrieves the order by ID.
 *   2. Throws OrderNotFoundException if it does not exist.
 */
public class GetOrderUseCaseImpl implements GetOrderUseCase {

    private final OrderRepository orderRepository;

    public GetOrderUseCaseImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order execute(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
