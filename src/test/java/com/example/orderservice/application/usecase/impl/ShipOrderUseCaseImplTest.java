package com.example.orderservice.application.usecase.impl;

import com.example.orderservice.application.port.out.OrderEventPublisher;
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
 * Unit tests for ShipOrderUseCaseImpl.
 *
 * NO Spring context — no @SpringBootTest, no database, no HTTP.
 * OrderRepository and OrderEventPublisher are mocked.
 *
 * Architecture insight: the use case delegates the state-transition guard to
 * the domain (Order.ship()). The use case itself only orchestrates: find → ship → save → publish.
 * These tests verify that orchestration, not the business rule (covered by OrderTest).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShipOrderUseCaseImpl — application layer unit tests")
class ShipOrderUseCaseImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private ShipOrderUseCaseImpl useCase;

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute() should ship the order and return it with SHIPPED status")
    void execute_shouldShipOrder_whenOrderExists() {
        Order order = Order.create("4K Monitor", new BigDecimal("800.00"), "Eve");
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = useCase.execute(order.getId());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("execute() should call repository.save() with the shipped order")
    void execute_shouldPersistUpdatedOrder() {
        Order order = Order.create("Webcam", new BigDecimal("90.00"), "Frank");
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        useCase.execute(order.getId());

        // Verify the adapter was asked to persist the same order object after ship()
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
    void execute_shouldThrowInvalidOrderStateException_whenOrderAlreadyShipped() {
        Order order = Order.create("Headset", new BigDecimal("60.00"), "Grace");
        order.ship(); // pre-transition: order is now SHIPPED
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // The domain's guard clause inside order.ship() triggers the exception
        assertThatThrownBy(() -> useCase.execute(order.getId()))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("already been shipped");
    }

    @Test
    @DisplayName("execute() should not save when the domain rejects the transition")
    void execute_shouldNotSave_whenDomainRejectsShip() {
        Order order = Order.create("Mouse", new BigDecimal("40.00"), "Hank");
        order.ship();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> useCase.execute(order.getId()))
                .isInstanceOf(InvalidOrderStateException.class);

        verify(orderRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Domain event publishing
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute() should publish OrderShippedEvent after a successful ship")
    void execute_shouldPublishEvent_whenOrderIsShippedSuccessfully() {
        Order order = Order.create("Smartwatch", new BigDecimal("200.00"), "Irene");
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        useCase.execute(order.getId());

        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("execute() should not publish event when order is not found")
    void execute_shouldNotPublishEvent_whenOrderNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(orderRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(unknownId))
                .isInstanceOf(OrderNotFoundException.class);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("execute() should not publish event when domain rejects the transition")
    void execute_shouldNotPublishEvent_whenDomainRejectsShip() {
        Order order = Order.create("Tablet", new BigDecimal("350.00"), "Jack");
        order.ship(); // already shipped
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> useCase.execute(order.getId()))
                .isInstanceOf(InvalidOrderStateException.class);

        verify(eventPublisher, never()).publish(any());
    }
}
