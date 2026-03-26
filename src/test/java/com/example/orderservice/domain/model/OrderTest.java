package com.example.orderservice.domain.model;

import com.example.orderservice.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order — domain business logic")
class OrderTest {

    // -------------------------------------------------------------------------
    // create()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create() should initialize order with CREATED status and correct data")
    void create_shouldInitializeWithCreatedStatus() {
        Order order = Order.create("Gaming Laptop", BigDecimal.valueOf(1500.00), "John");

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getDescription()).isEqualTo("Gaming Laptop");
        assertThat(order.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1500.00));
        assertThat(order.getCustomer()).isEqualTo("John");
        assertThat(order.getCreatedAt()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // ship()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("ship() should change status to SHIPPED when order is in CREATED status")
    void ship_shouldChangeStatusToShipped_whenOrderIsCreated() {
        Order order = Order.create("Standing Desk", BigDecimal.valueOf(300.00), "Mary");

        order.ship();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("ship() should throw InvalidOrderStateException when order is already SHIPPED")
    void ship_shouldThrowInvalidOrderStateException_whenOrderIsAlreadyShipped() {
        Order order = Order.create("Ergonomic Chair", BigDecimal.valueOf(150.00), "Charles");
        order.ship(); // legitimate transition: CREATED → SHIPPED

        assertThatThrownBy(order::ship)
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("already been shipped");
    }

    // -------------------------------------------------------------------------
    // reconstitute()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("reconstitute() should preserve all given values without modification")
    void reconstitute_shouldPreserveAllValues() {
        Order original = Order.create("4K Monitor", BigDecimal.valueOf(800.00), "Anne");
        original.ship();

        Order reconstituted = Order.reconstitute(
                original.getId(),
                original.getDescription(),
                original.getAmount(),
                original.getCustomer(),
                original.getStatus(),
                original.getCreatedAt()
        );

        assertThat(reconstituted.getId()).isEqualTo(original.getId());
        assertThat(reconstituted.getDescription()).isEqualTo(original.getDescription());
        assertThat(reconstituted.getAmount()).isEqualByComparingTo(original.getAmount());
        assertThat(reconstituted.getCustomer()).isEqualTo(original.getCustomer());
        assertThat(reconstituted.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(reconstituted.getCreatedAt()).isEqualTo(original.getCreatedAt());
    }

    // -------------------------------------------------------------------------
    // cancel()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("cancel() should change status to CANCELLED when order is in CREATED status")
    void cancel_shouldChangeStatusToCancelled_whenOrderIsCreated() {
        Order order = Order.create("Smartwatch", BigDecimal.valueOf(200.00), "Leo");

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancel() should throw InvalidOrderStateException when order is already SHIPPED")
    void cancel_shouldThrowInvalidOrderStateException_whenOrderIsShipped() {
        Order order = Order.create("Tablet", BigDecimal.valueOf(350.00), "Mia");
        order.ship();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("shipped")
                .hasMessageContaining("cannot be cancelled");
    }

    @Test
    @DisplayName("cancel() should throw InvalidOrderStateException when order is already CANCELLED")
    void cancel_shouldThrowInvalidOrderStateException_whenOrderIsAlreadyCancelled() {
        Order order = Order.create("Charger", BigDecimal.valueOf(30.00), "Noah");
        order.cancel();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("already been cancelled");
    }
}
