package com.example.orderservice.infrastructure.adapter.out.inmemory;

import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for InMemoryOrderRepository.
 *
 * NO Spring context — no @SpringBootTest, no database, no HTTP.
 * Instantiated directly as a plain Java object.
 *
 * Architecture insight: this adapter can be tested without Spring because it
 * implements a pure domain interface. The same is true of OrderPersistenceAdapter,
 * but testing JPA requires a datasource. The in-memory adapter removes even that
 * constraint — making it ideal as a test double in higher-level tests.
 */
@DisplayName("InMemoryOrderRepository — output adapter unit tests")
class InMemoryOrderRepositoryTest {

    private InMemoryOrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
    }

    // -------------------------------------------------------------------------
    // save()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("save() should return the same order instance that was saved")
    void save_shouldReturnSavedOrder() {
        Order order = Order.create("Wireless Mouse", new BigDecimal("35.00"), "Alice");

        Order result = repository.save(order);

        assertThat(result).isSameAs(order);
    }

    @Test
    @DisplayName("save() should make the order retrievable by ID")
    void save_shouldMakeOrderFindableById() {
        Order order = Order.create("Mechanical Keyboard", new BigDecimal("120.00"), "Bob");

        repository.save(order);

        assertThat(repository.findById(order.getId())).isPresent();
    }

    @Test
    @DisplayName("save() called twice with the same ID should overwrite the previous state")
    void save_shouldOverwriteOrderWithSameId() {
        Order order = Order.create("Headset", new BigDecimal("80.00"), "Carol");
        repository.save(order); // CREATED

        order.ship();
        repository.save(order); // SHIPPED — same UUID, updated state

        Optional<Order> found = repository.findById(order.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    // -------------------------------------------------------------------------
    // findById()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById() should return the order with all its original data")
    void findById_shouldReturnOrderWithCorrectData() {
        Order order = Order.create("USB-C Hub", new BigDecimal("45.00"), "Dave");
        repository.save(order);

        Optional<Order> result = repository.findById(order.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(order.getId());
        assertThat(result.get().getDescription()).isEqualTo("USB-C Hub");
        assertThat(result.get().getAmount()).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(result.get().getCustomer()).isEqualTo("Dave");
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("findById() should return empty Optional when order does not exist")
    void findById_shouldReturnEmpty_whenOrderNotFound() {
        Optional<Order> result = repository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Isolation between orders
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("multiple orders can be saved and retrieved independently")
    void multipleOrders_shouldBeStoredAndRetrievedIndependently() {
        Order order1 = Order.create("Monitor", new BigDecimal("500.00"), "Eve");
        Order order2 = Order.create("Webcam", new BigDecimal("70.00"), "Frank");

        repository.save(order1);
        repository.save(order2);

        assertThat(repository.findById(order1.getId())).isPresent();
        assertThat(repository.findById(order2.getId())).isPresent();
        assertThat(repository.findById(order1.getId()).get().getDescription()).isEqualTo("Monitor");
        assertThat(repository.findById(order2.getId()).get().getDescription()).isEqualTo("Webcam");
    }
}
