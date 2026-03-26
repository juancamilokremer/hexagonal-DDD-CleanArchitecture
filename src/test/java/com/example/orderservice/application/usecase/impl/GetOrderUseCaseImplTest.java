package com.example.orderservice.application.usecase.impl;

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
import static org.mockito.Mockito.when;

/**
 * Unit tests for GetOrderUseCaseImpl.
 *
 * NO Spring context — no @SpringBootTest, no database, no HTTP.
 * OrderRepository is mocked.
 *
 * Architecture insight: this use case is intentionally simple — just a query
 * that delegates to the repository port. Its test demonstrates that even the
 * simplest use case is isolated and verifiable without any framework.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetOrderUseCaseImpl — application layer unit tests")
class GetOrderUseCaseImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private GetOrderUseCaseImpl useCase;

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute() should return the order when it exists")
    void execute_shouldReturnOrder_whenOrderExists() {
        Order order = Order.create("Laptop Stand", new BigDecimal("55.00"), "Iris");
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        Order result = useCase.execute(order.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(order.getId());
        assertThat(result.getDescription()).isEqualTo("Laptop Stand");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("55.00"));
        assertThat(result.getCustomer()).isEqualTo("Iris");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("execute() should return a SHIPPED order unchanged")
    void execute_shouldReturnShippedOrder_whenOrderIsShipped() {
        Order order = Order.create("USB Hub", new BigDecimal("25.00"), "Jack");
        order.ship();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        Order result = useCase.execute(order.getId());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
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
}
