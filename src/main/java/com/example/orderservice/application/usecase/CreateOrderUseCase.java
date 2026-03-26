package com.example.orderservice.application.usecase;

import com.example.orderservice.application.usecase.command.CreateOrderCommand;
import com.example.orderservice.domain.model.Order;

/**
 * Input Port for creating a new order.
 * Defines the contract that the infrastructure (controller) invokes.
 * The implementation lives in impl/ and has no framework dependency.
 */
public interface CreateOrderUseCase {

    /**
     * Creates a new order from the given command data.
     *
     * @param command the data required to create the order.
     * @return the newly created order with its generated ID and CREATED status.
     */
    Order execute(CreateOrderCommand command);
}
