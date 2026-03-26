package com.example.orderservice.application.usecase.impl;

import com.example.orderservice.application.usecase.CreateOrderUseCase;
import com.example.orderservice.application.usecase.command.CreateOrderCommand;
import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.repository.OrderRepository;

/**
 * Implementation of the CreateOrder use case.
 *
 * Orchestrates application logic:
 *   1. Delegates creation to the domain factory method.
 *   2. Persists the order through the repository port.
 *
 * Contains no business logic — that responsibility belongs to Order.
 * No framework annotations: Spring registers this bean from ApplicationConfig.
 */
public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private final OrderRepository orderRepository;

    public CreateOrderUseCaseImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order execute(CreateOrderCommand command) {
        Order order = Order.create(
                command.getDescription(),
                command.getAmount(),
                command.getCustomer()
        );
        return orderRepository.save(order);
    }
}
