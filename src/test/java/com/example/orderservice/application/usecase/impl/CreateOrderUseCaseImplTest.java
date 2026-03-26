package com.example.orderservice.application.usecase.impl;

import com.example.orderservice.application.usecase.command.CreateOrderCommand;
import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.model.OrderStatus;
import com.example.orderservice.domain.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CreateOrderUseCaseImpl.
 *
 * NO Spring context — no @SpringBootTest, no database, no HTTP.
 * OrderRepository is mocked: this proves the use case is independently testable.
 *
 * Architecture insight: if these tests were hard to write, it would signal that
 * Spring or JPA concerns had leaked into the application layer.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateOrderUseCaseImpl — application layer unit tests")
class CreateOrderUseCaseImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CreateOrderUseCaseImpl useCase;

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute() should return the order returned by the repository")
    void execute_shouldReturnSavedOrder() {
        var command = new CreateOrderCommand("Gaming Laptop", new BigDecimal("1500.00"), "Alice");
        // Simulate the repository echoing back whatever it receives
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = useCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Gaming Laptop");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(result.getCustomer()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("execute() should create order with CREATED status")
    void execute_shouldInitializeStatusAsCreated() {
        var command = new CreateOrderCommand("Ergonomic Chair", new BigDecimal("300.00"), "Bob");
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = useCase.execute(command);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("execute() should generate a non-null UUID for the new order")
    void execute_shouldGenerateId() {
        var command = new CreateOrderCommand("Mechanical Keyboard", new BigDecimal("120.00"), "Carol");
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = useCase.execute(command);

        assertThat(result.getId()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // Interaction with the repository port
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute() should call repository.save() exactly once with the created order")
    void execute_shouldPersistTheOrderOnce() {
        var command = new CreateOrderCommand("Standing Desk", new BigDecimal("450.00"), "Dave");
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(command);

        // Capture what was passed to save() and verify the data matches the command
        var captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        Order persisted = captor.getValue();
        assertThat(persisted.getDescription()).isEqualTo("Standing Desk");
        assertThat(persisted.getAmount()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(persisted.getCustomer()).isEqualTo("Dave");
        assertThat(persisted.getStatus()).isEqualTo(OrderStatus.CREATED);
    }
}
