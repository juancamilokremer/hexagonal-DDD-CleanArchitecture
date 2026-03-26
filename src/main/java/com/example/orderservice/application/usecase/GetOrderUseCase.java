package com.example.orderservice.application.usecase;

import com.example.orderservice.domain.model.Order;

import java.util.UUID;

/**
 * Input Port for retrieving an order by its ID.
 * Throws OrderNotFoundException if the ID does not exist.
 */
public interface GetOrderUseCase {

    /**
     * Retrieves the order corresponding to the given UUID.
     *
     * @param orderId the unique identifier of the order.
     * @return the found order.
     */
    Order execute(UUID orderId);
}
