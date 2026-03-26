package com.example.orderservice.application.usecase;

import com.example.orderservice.domain.model.Order;

import java.util.UUID;

/**
 * Input Port for shipping an existing order.
 * Throws OrderNotFoundException if the ID does not exist.
 * Throws InvalidOrderStateException if the order has already been shipped.
 */
public interface ShipOrderUseCase {

    /**
     * Ships the order identified by the given UUID.
     *
     * @param orderId the unique identifier of the order to ship.
     * @return the updated order with SHIPPED status.
     */
    Order execute(UUID orderId);
}
