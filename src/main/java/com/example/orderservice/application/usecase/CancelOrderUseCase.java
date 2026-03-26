package com.example.orderservice.application.usecase;

import com.example.orderservice.domain.model.Order;

import java.util.UUID;

/**
 * Input Port — use case contract for cancelling an order.
 */
public interface CancelOrderUseCase {

    Order execute(UUID orderId);
}
