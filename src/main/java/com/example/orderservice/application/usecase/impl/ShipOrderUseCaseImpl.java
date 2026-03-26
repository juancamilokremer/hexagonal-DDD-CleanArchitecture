package com.example.orderservice.application.usecase.impl;

import com.example.orderservice.application.port.out.OrderEventPublisher;
import com.example.orderservice.application.usecase.ShipOrderUseCase;
import com.example.orderservice.domain.event.OrderShippedEvent;
import com.example.orderservice.domain.exception.OrderNotFoundException;
import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.repository.OrderRepository;

import java.util.UUID;

/**
 * Implementation of the ShipOrder use case.
 *
 * Orchestrates application logic:
 *   1. Retrieves the order — throws OrderNotFoundException if it does not exist.
 *   2. Invokes order.ship() — business logic lives in the domain.
 *   3. Persists the updated state.
 *   4. Publishes OrderShippedEvent through the event output port.
 *
 * Contains no business logic — that responsibility belongs to Order.
 * Contains no Spring dependency — OrderEventPublisher is a plain interface.
 */
public class ShipOrderUseCaseImpl implements ShipOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public ShipOrderUseCaseImpl(OrderRepository orderRepository, OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Order execute(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.ship(); // throws InvalidOrderStateException if already shipped

        Order saved = orderRepository.save(order);

        // Publish event AFTER successful persistence — the infrastructure decides what to do with it
        eventPublisher.publish(OrderShippedEvent.of(saved.getId()));

        return saved;
    }
}

