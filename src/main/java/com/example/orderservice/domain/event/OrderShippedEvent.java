package com.example.orderservice.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event — raised when an order transitions to SHIPPED status.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * ARCHITECTURAL PRINCIPLE:
 *
 *   This is a plain Java record — zero imports from Spring or any library.
 *   The domain communicates THAT something happened, without knowing:
 *     - who will react to it
 *     - how it will be transported (Spring Events, Kafka, SQS...)
 *     - what side effects will follow (email, audit log, notification...)
 *
 *   All of that lives in the infrastructure layer.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * @param orderId     the ID of the order that was shipped
 * @param occurredAt  the moment the event occurred
 */
public record OrderShippedEvent(UUID orderId, LocalDateTime occurredAt) {

    /**
     * Factory method — captures the current timestamp automatically.
     */
    public static OrderShippedEvent of(UUID orderId) {
        return new OrderShippedEvent(orderId, LocalDateTime.now());
    }
}
