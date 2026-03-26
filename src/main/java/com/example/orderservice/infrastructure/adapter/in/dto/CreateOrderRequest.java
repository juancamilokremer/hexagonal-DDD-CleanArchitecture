package com.example.orderservice.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Input DTO for the POST /orders endpoint.
 * Validated at the controller boundary before entering the application layer.
 */
@Schema(description = "Request body for creating a new order")
public class CreateOrderRequest {

    @Schema(description = "Short description of the ordered item", example = "Gaming Laptop")
    @NotBlank(message = "description must not be blank")
    private String description;

    @Schema(description = "Order total amount (must be > 0)", example = "1500.00")
    @NotNull(message = "amount must not be null")
    @Positive(message = "amount must be greater than zero")
    private BigDecimal amount;

    @Schema(description = "Full name of the customer placing the order", example = "Alice Smith")
    @NotBlank(message = "customer must not be blank")
    private String customer;

    // ── Required by Jackson ───────────────────────────────────────────────────
    public CreateOrderRequest() {}

    public CreateOrderRequest(String description, BigDecimal amount, String customer) {
        this.description = description;
        this.amount = amount;
        this.customer = customer;
    }

    public String getDescription() { return description; }
    public BigDecimal getAmount()  { return amount; }
    public String getCustomer()    { return customer; }
}
