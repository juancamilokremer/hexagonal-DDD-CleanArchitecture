package com.example.orderservice.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for the JPA entity.
 * Internal to the persistence adapter — never referenced outside infrastructure.
 */
interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {
}
