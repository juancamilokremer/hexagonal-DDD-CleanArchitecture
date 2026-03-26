package com.example.orderservice.infrastructure.adapter.out.event;

import com.example.orderservice.application.port.out.OrderEventPublisher;
import com.example.orderservice.domain.event.OrderShippedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Output Adapter — implements the OrderEventPublisher port using Spring's event bus.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * ARCHITECTURAL LESSON:
 *
 *   The application layer defines the port (OrderEventPublisher interface).
 *   This adapter is the ONLY class that knows about Spring's ApplicationEventPublisher.
 *
 *   If we later want to publish to Kafka or RabbitMQ instead, we would:
 *     1. Create KafkaOrderEventPublisher implementing OrderEventPublisher
 *     2. Remove @Component here, add @Component there
 *     Zero changes in domain/ or application/.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Component
public class SpringOrderEventPublisher implements OrderEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringOrderEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(OrderShippedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
