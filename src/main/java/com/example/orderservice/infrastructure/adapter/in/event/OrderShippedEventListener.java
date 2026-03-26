package com.example.orderservice.infrastructure.adapter.in.event;

import com.example.orderservice.domain.event.OrderShippedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Input Adapter — reacts to OrderShippedEvent published via Spring's event bus.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * ARCHITECTURAL LESSON:
 *
 *   This listener is completely invisible to the domain and application layers.
 *   Adding it requires zero changes to Order, ShipOrderUseCaseImpl, or any port.
 *
 *   To add a new reaction (send email, call external API, update analytics):
 *     → Add a new @EventListener method here, or create another listener class.
 *     → Nothing else changes.
 *
 *   This is the Open/Closed Principle: the use case is CLOSED for modification
 *   but the system is OPEN for new side effects via listeners.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Component
public class OrderShippedEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderShippedEventListener.class);

    @EventListener
    public void handle(OrderShippedEvent event) {
        log.info("[DOMAIN EVENT] OrderShippedEvent received — orderId={}, occurredAt={}",
                event.orderId(), event.occurredAt());

        // In a real application, this is where you would:
        //   - Send a shipping confirmation email
        //   - Notify a warehouse system
        //   - Publish to an external message broker (Kafka, RabbitMQ)
        //   - Update an analytics service
        // All without touching Order.java or ShipOrderUseCaseImpl.
    }
}
