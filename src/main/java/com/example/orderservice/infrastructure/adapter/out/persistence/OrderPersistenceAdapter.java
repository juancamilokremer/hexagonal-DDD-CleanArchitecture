package com.example.orderservice.infrastructure.adapter.out.persistence;

import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.repository.OrderRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Output Adapter — implements the domain's OrderRepository port using Spring Data JPA.
 *
 * Active for all profiles EXCEPT "inmemory".
 * To swap this adapter for the in-memory one, start with:
 *   mvn spring-boot:run -Dspring-boot.run.profiles=inmemory
 *
 * This is the only class in the persistence package that the rest of the
 * application is aware of. All JPA details stay encapsulated here.
 */
@Component
@Profile("!inmemory")
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderPersistenceAdapter(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = OrderMapper.toJpaEntity(order);
        OrderJpaEntity saved = jpaRepository.save(entity);
        return OrderMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(OrderMapper::toDomain);
    }
}
