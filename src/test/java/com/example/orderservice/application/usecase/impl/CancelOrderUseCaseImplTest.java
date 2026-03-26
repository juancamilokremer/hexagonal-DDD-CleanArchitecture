package com.example.orderservice.application.usecase.impl;

import com.example.orderservice.domain.exception.InvalidOrderStateException;
import com.example.orderservice.domain.exception.OrderNotFoundException;
import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.model.OrderStatus;
import com.example.orderservice.domain.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CancelOrderUseCaseImpl.
 *
 * NO Spring context — no @SpringBootTest, no database, no HTTP.
 * OrderRepository is mocked.
 *
 * Architecture insight: the guard clause for cancellation lives in Order.cancel()
 * (a domain rule). The use case only orchestrates: find → cancel → save.
 * These tests verify that orchestration and the domain's guard are respected.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CancelOrderUseCaseImpl — application layer unit tests")
class CancelOrderUseCaseImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CancelOrderUseCaseImpl useCase;

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute() should cancel the order and return it with CANCELLED status")
    void execute_shouldCancelOrder_whenOrderIsCreated() {
        Order order = Order.create("Laptop Stand", new BigDecimal("55.00"), "Alice");
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = useCase.execute(order.getId());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("execute() should call repository.save() with the cancelled order")
    void execute_shouldPersistCancelledOrder() {
        Order order = Order.create("Webcam", new BigDecimal("70.00"), "Bob");
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        useCase.execute(order.getId());

        verify(orderRepository).save(order);
    }

    // -------------------------------------------------------------------------
    // Order not found
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute() should throw OrderNotFoundException when order does not exist")
    void execute_shouldThrowOrderNotFoundException_whenOrderDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(orderRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(unknownId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }

    @Test
    @DisplayName("execute() should not call repository.save() when order is not found")
    void execute_shouldNotSave_whenOrderDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(orderRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(unknownId))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Domain invariant guard
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute() should throw InvalidOrderStateException when order is already SHIPPED")
    void execute_shouldThrowInvalidOrderStateException_whenOrderIsShipped() {
        Order order = Order.create("Keyboard", new BigDecimal("90.00"), "Carol");
        order.ship();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> useCase.execute(order.getId()))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("cannot be cancelled");
    }

    @Test
    @DisplayName("execute() should throw InvalidOrderStateException when order is already CANCELLED")
    void execute_shouldThrowInvalidOrderStateException_whenOrderIsAlreadyCancelled() {
        Order order = Order.create("Monitor", new BigDecimal("300.00"), "Dave");
        order.cancel();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> useCase.execute(order.getId()))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("already been cancelled");
    }

    @Test
    @DisplayName("execute() should not save when the domain rejects the cancellation")
    void execute_shouldNotSave_whenDomainRejectsCancel() {
        Order order = Order.create("Headset", new BigDecimal("60.00"), "Eve");
        order.ship();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> useCase.execute(order.getId()))
                .isInstanceOf(InvalidOrderStateException.class);

        verify(orderRepository, never()).save(any());
    }
}
