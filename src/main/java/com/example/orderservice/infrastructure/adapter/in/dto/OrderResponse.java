package com.example.orderservice.infrastructure.adapter.in.dto;

import com.example.orderservice.domain.model.Order;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Output DTO returned by every order endpoint.
 * Domain entities are never exposed directly to the outside world.
 */
@Schema(description = "Order details returned by every endpoint")
public class OrderResponse {

    @Schema(description = "Unique order identifier (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private final UUID id;
    @Schema(description = "Short description of the ordered item", example = "Gaming Laptop")
    private final String description;
    @Schema(description = "Order total amount", example = "1500.00")
    private final BigDecimal amount;
    @Schema(description = "Full name of the customer", example = "Alice Smith")
    private final String customer;
    @Schema(description = "Current order status", allowableValues = {"CREATED", "SHIPPED", "CANCELLED"}, example = "CREATED")
    private final String status;
    @Schema(description = "Timestamp when the order was created (ISO-8601)")
    private final LocalDateTime createdAt;

    private OrderResponse(UUID id, String description, BigDecimal amount,
                          String customer, String status, LocalDateTime createdAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.customer = customer;
        this.status = status;
        this.createdAt = createdAt;
    }

    /** Factory method — translates a domain Order into this response DTO. */
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getDescription(),
                order.getAmount(),
                order.getCustomer(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }

    public UUID getId()                { return id; }
    public String getDescription()     { return description; }
    public BigDecimal getAmount()      { return amount; }
    public String getCustomer()        { return customer; }
    public String getStatus()          { return status; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
}
