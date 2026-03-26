package com.example.orderservice.infrastructure.adapter.out.persistence;

import com.example.orderservice.domain.model.OrderStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity — infrastructure concern only.
 * Never exposed beyond the persistence adapter.
 */
@Entity
@Table(name = "orders")  // "order" is a reserved SQL keyword
public class OrderJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Required by JPA ──────────────────────────────────────────────────────
    protected OrderJpaEntity() {}

    public OrderJpaEntity(UUID id, String description, BigDecimal amount,
                          String customer, OrderStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.customer = customer;
        this.status = status;
        this.createdAt = createdAt;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public UUID getId()                { return id; }
    public String getDescription()     { return description; }
    public BigDecimal getAmount()      { return amount; }
    public String getCustomer()        { return customer; }
    public OrderStatus getStatus()     { return status; }
    public LocalDateTime getCreatedAt(){ return createdAt; }

    // ── Setter for mutable state (status changes on ship) ────────────────────

    public void setStatus(OrderStatus status) { this.status = status; }
}
