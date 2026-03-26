package com.example.orderservice.domain.exception;

import java.util.UUID;

/**
 * Thrown when an Order cannot be found by its identifier.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID id) {
        super(String.format("Order with id '%s' not found.", id));
    }
}
