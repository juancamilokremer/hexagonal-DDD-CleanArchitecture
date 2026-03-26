package com.example.orderservice.application.port.out;

import com.example.orderservice.domain.event.OrderShippedEvent;

/**
 * Output Port — contract for publishing domain events.
 *
 * Defined in the application layer so the use cases can publish events
 * without any dependency on Spring or any messaging infrastructure.
 *
 * The infrastructure layer provides the concrete implementation
 * (e.g., SpringOrderEventPublisher using ApplicationEventPublisher).
 */
public interface OrderEventPublisher {

    void publish(OrderShippedEvent event);
}
