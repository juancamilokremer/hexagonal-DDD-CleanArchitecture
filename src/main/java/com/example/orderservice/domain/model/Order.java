package com.example.orderservice.domain.model;

import com.example.orderservice.domain.exception.InvalidOrderStateException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Aggregate Root.
 * Encapsulates the state and business logic of an order.
 * Has no dependencies on any framework or infrastructure library.
 */
public class Order {

    private final UUID id;
    private final String description;
    private final BigDecimal amount;
    private final String customer;
    private OrderStatus status;
    private final LocalDateTime createdAt;

    // Private constructor — instantiation goes through factory methods only
    private Order(UUID id, String description, BigDecimal amount,
                  String customer, OrderStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.customer = customer;
        this.status = status;
        this.createdAt = createdAt;
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    /**
     * Creates a new order with CREATED status.
     * ID and creation timestamp are generated automatically.
     */
    public static Order create(String description, BigDecimal amount, String customer) {
        return new Order(
                UUID.randomUUID(),
                description,
                amount,
                customer,
                OrderStatus.CREATED,
                LocalDateTime.now()
        );
    }

    /**
     * Reconstitutes an order from persisted data.
     * Should only be used by infrastructure adapters (mappers).
     */
    public static Order reconstitute(UUID id, String description, BigDecimal amount,
                                     String customer, OrderStatus status, LocalDateTime createdAt) {
        return new Order(id, description, amount, customer, status, createdAt);
    }

    // -------------------------------------------------------------------------
    // Business logic (domain invariants)
    // -------------------------------------------------------------------------

    /**
     * Ships the order.
     * Invariant: an order can only be shipped if it is in CREATED status.
     *
     * @throws InvalidOrderStateException if the order has already been shipped.
     */
    public void ship() {
        if (this.status == OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException(
                    String.format("Order '%s' has already been shipped and cannot be shipped again.", this.id)
            );
        }
        if (this.status == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                    String.format("Order '%s' has been cancelled and cannot be shipped.", this.id)
            );
        }
        this.status = OrderStatus.SHIPPED;
    }

    /**
     * Cancels the order.
     * Invariant: an order can only be cancelled if it is in CREATED status.
     *
     * @throws InvalidOrderStateException if the order has already been shipped or cancelled.
     */
    public void cancel() {
        if (this.status == OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException(
                    String.format("Order '%s' has already been shipped and cannot be cancelled.", this.id)
            );
        }
        if (this.status == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                    String.format("Order '%s' has already been cancelled.", this.id)
            );
        }
        this.status = OrderStatus.CANCELLED;
    }

    // -------------------------------------------------------------------------
    // Getters (no setters — state mutates only through business methods)
    // -------------------------------------------------------------------------

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
