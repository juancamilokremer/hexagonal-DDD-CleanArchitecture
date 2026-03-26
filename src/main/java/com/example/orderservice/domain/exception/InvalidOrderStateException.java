package com.example.orderservice.domain.exception;

/**
 * Thrown when an invalid state transition is attempted on an Order.
 * Example: shipping an order that has already been shipped.
 */
public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}
