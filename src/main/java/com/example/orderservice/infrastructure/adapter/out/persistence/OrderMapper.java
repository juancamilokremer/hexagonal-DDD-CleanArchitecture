package com.example.orderservice.infrastructure.adapter.out.persistence;

import com.example.orderservice.domain.model.Order;

/**
 * Stateless mapper between the domain model and the JPA entity.
 * Keeps the translation logic in a single, focused class.
 */
final class OrderMapper {

    private OrderMapper() {}

    /**
     * Converts a domain Order to a JPA entity for persistence.
     */
    static OrderJpaEntity toJpaEntity(Order order) {
        return new OrderJpaEntity(
                order.getId(),
                order.getDescription(),
                order.getAmount(),
                order.getCustomer(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    /**
     * Reconstitutes a domain Order from a JPA entity.
     */
    static Order toDomain(OrderJpaEntity entity) {
        return Order.reconstitute(
                entity.getId(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getCustomer(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
