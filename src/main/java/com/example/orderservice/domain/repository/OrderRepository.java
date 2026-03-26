package com.example.orderservice.domain.repository;

import com.example.orderservice.domain.model.Order;

import java.util.Optional;
import java.util.UUID;

/**
 * Output Port — defined by the domain.
 *
 * The domain declares the persistence contract it needs.
 * The infrastructure provides the concrete implementation (output adapter).
 * This dependency inversion ensures the domain remains independent
 * of any persistence technology.
 */
public interface OrderRepository {

    /**
     * Persists an order (create or update).
     *
     * @param order the domain entity to save.
     * @return the saved order.
     */
    Order save(Order order);

    /**
     * Finds an order by its unique identifier.
     *
     * @param id the UUID of the order.
     * @return an Optional containing the order if found, empty otherwise.
     */
    Optional<Order> findById(UUID id);
}
