package com.example.orderservice.infrastructure.adapter.out.inmemory;

import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.repository.OrderRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Output Adapter — implements the domain's OrderRepository port using an in-memory store.
 *
 * Only active when the "inmemory" Spring profile is set:
 *   mvn spring-boot:run -Dspring-boot.run.profiles=inmemory
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * ARCHITECTURAL LESSON — Dependency Inversion Principle in action:
 *
 *   The domain defines this contract:
 *       OrderRepository (interface, lives in domain/)
 *
 *   Two completely different adapters implement it:
 *       OrderPersistenceAdapter  → JPA + H2/PostgreSQL  (profile: default)
 *       InMemoryOrderRepository  → ConcurrentHashMap    (profile: inmemory)
 *
 *   ZERO lines in domain/ or application/ change when switching between them.
 *   Spring wires the correct bean through @Profile — the rest of the app is unaware.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Component
@Profile("inmemory")
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<UUID, Order> store = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        store.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }
}
