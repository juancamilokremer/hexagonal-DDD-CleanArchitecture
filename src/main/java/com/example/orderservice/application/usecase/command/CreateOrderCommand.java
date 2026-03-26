package com.example.orderservice.application.usecase.command;

import java.math.BigDecimal;

/**
 * Command object (application-layer input DTO).
 *
 * Encapsulates the data required to create an order.
 * Immutable: constructed once and never modified.
 * Plain Java — no framework annotations.
 */
public class CreateOrderCommand {

    private final String description;
    private final BigDecimal amount;
    private final String customer;

    public CreateOrderCommand(String description, BigDecimal amount, String customer) {
        this.description = description;
        this.amount = amount;
        this.customer = customer;
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
}
