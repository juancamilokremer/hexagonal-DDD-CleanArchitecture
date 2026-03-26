# Order Service — Hexagonal Architecture + DDD + Clean Architecture

A production-ready Java 17 / Spring Boot 3.2 microservice built as a **learning reference** for three complementary architectural patterns:

- **Domain-Driven Design (DDD)** — model business rules in code
- **Hexagonal Architecture** (Ports & Adapters) — isolate the domain from external systems
- **Clean Architecture** — enforce strict dependency direction rules

---

## Table of Contents

1. [Why These Patterns?](#1-why-these-patterns)
2. [Core Concepts](#2-core-concepts)
3. [Project Structure](#3-project-structure)
4. [Layer-by-Layer Explanation](#4-layer-by-layer-explanation)
   - [Domain Layer](#41-domain-layer)
   - [Application Layer](#42-application-layer)
   - [Infrastructure Layer](#43-infrastructure-layer)
5. [Dependency Rule (The Golden Rule)](#5-dependency-rule-the-golden-rule)
6. [Hexagonal Architecture — Ports & Adapters](#6-hexagonal-architecture--ports--adapters)
7. [Data Flow — Creating an Order (End-to-End)](#7-data-flow--creating-an-order-end-to-end)
8. [API Reference](#8-api-reference)
9. [Running the Project](#9-running-the-project)
10. [Running the Tests](#10-running-the-tests)
11. [Key Design Decisions](#11-key-design-decisions)
12. [Common Mistakes This Architecture Prevents](#12-common-mistakes-this-architecture-prevents)
13. [Learning Roadmap — Upcoming Improvements](#13-learning-roadmap--upcoming-improvements)

---

## 1. Why These Patterns?

Traditional layered architectures (Controller → Service → Repository) have a critical flaw: **everything depends on the database**. The repository interface lives in the infrastructure, so the business logic is forced to know about JPA, Spring Data, or whatever persistence technology is chosen.

These three patterns solve that problem by **inverting the dependency**:

```
Traditional:  Business Logic → depends on → Database
Hexagonal:    Database       → depends on → Business Logic
```

The result is a codebase where:
- **Business rules can be tested without starting Spring** — no database, no HTTP, just plain Java.
- **The persistence technology can be swapped** (H2 → PostgreSQL → MongoDB) without touching business logic.
- **The delivery mechanism can be swapped** (REST → gRPC → CLI) without touching business logic.

---

## 2. Core Concepts

### Domain-Driven Design (DDD)

DDD is about putting the **business language and rules at the center** of the code.

| DDD Concept | This Project |
|---|---|
| **Ubiquitous Language** | The code uses domain words: `Order`, `ship()`, `OrderStatus.CREATED` — not technical jargon like `updateRecord()` |
| **Aggregate Root** | `Order` — the only entry point to modify order state |
| **Value Object** | `OrderStatus` enum — immutable, compared by value |
| **Factory Method** | `Order.create()` — enforces invariants at construction time |
| **Domain Exception** | `InvalidOrderStateException`, `OrderNotFoundException` — business rule violations |
| **Repository (interface)** | `OrderRepository` — the domain defines *what* it needs, not *how* it's stored |

### Hexagonal Architecture (Ports & Adapters)

Coined by Alistair Cockburn. The application is a **hexagon** surrounded by adapters:

```
         [ REST Client ]         [ Test ]
               |                    |
          [ Input Adapter ]  [ Input Adapter ]
               |                    |
        ┌──────────────────────────────────┐
        │          APPLICATION CORE        │
        │   (Domain + Use Cases)           │
        └──────────────────────────────────┘
               |
         [ Output Adapter ]
               |
          [ Database ]
```

- **Ports** = interfaces (contracts the core defines)
- **Adapters** = implementations of those contracts (REST controller, JPA adapter)

### Clean Architecture

Robert C. Martin's rule: **dependencies always point inward**.

```
┌─────────────────────────────────────────┐
│  Infrastructure                         │
│  ┌───────────────────────────────────┐  │
│  │  Application                      │  │
│  │  ┌─────────────────────────────┐  │  │
│  │  │  Domain                     │  │  │
│  │  │  (no external dependencies) │  │  │
│  │  └─────────────────────────────┘  │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘

Arrows: Infrastructure → Application → Domain
Never: Domain → Application, Domain → Infrastructure
```

---

## 3. Project Structure

```
src/
├── main/java/com/example/orderservice/
│   │
│   ├── OrderServiceApplication.java          ← Spring Boot entry point
│   │
│   ├── domain/                               ← Pure Java, zero framework imports
│   │   ├── model/
│   │   │   ├── Order.java                    ← Aggregate Root
│   │   │   └── OrderStatus.java              ← Enum: CREATED, SHIPPED
│   │   ├── exception/
│   │   │   ├── OrderNotFoundException.java
│   │   │   └── InvalidOrderStateException.java
│   │   └── repository/
│   │       └── OrderRepository.java          ← Output Port (interface)
│   │
│   ├── application/                          ← Orchestration, no business logic
│   │   └── usecase/
│   │       ├── CreateOrderUseCase.java        ← Input Port (interface)
│   │       ├── ShipOrderUseCase.java          ← Input Port (interface)
│   │       ├── GetOrderUseCase.java           ← Input Port (interface)
│   │       ├── command/
│   │       │   └── CreateOrderCommand.java    ← Command Object (POJO)
│   │       └── impl/
│   │           ├── CreateOrderUseCaseImpl.java
│   │           ├── ShipOrderUseCaseImpl.java
│   │           └── GetOrderUseCaseImpl.java
│   │
│   └── infrastructure/                       ← All framework/technology code
│       ├── adapter/
│       │   ├── in/                           ← Input Adapters (drive the app)
│       │   │   ├── OrderController.java       ← REST Controller
│       │   │   ├── dto/
│       │   │   │   ├── CreateOrderRequest.java
│       │   │   │   ├── OrderResponse.java
│       │   │   │   └── ApiErrorResponse.java
│       │   │   └── exception/
│       │   │       └── GlobalExceptionHandler.java
│       │   └── out/                          ← Output Adapters (driven by the app)
│       │       └── persistence/
│       │           ├── OrderJpaEntity.java    ← JPA Entity (not a domain object)
│       │           ├── OrderJpaRepository.java← Spring Data interface
│       │           ├── OrderMapper.java       ← JPA Entity ↔ Domain Object
│       │           └── OrderPersistenceAdapter.java ← Implements OrderRepository
│       └── config/
│           └── ApplicationConfig.java        ← @Bean wiring for use cases
│
├── resources/
│   └── application.properties               ← H2, JPA, logging config
│
└── test/java/com/example/orderservice/
    └── domain/model/
        └── OrderTest.java                   ← 4 unit tests, no Spring context
```

---

## 4. Layer-by-Layer Explanation

### 4.1 Domain Layer

**Location:** `domain/`  
**Rule:** Zero imports from Spring, JPA, or any library. Only `java.*`.

This is the heart of the application. It represents the business problem in pure code.

#### `Order` — Aggregate Root

```java
// ✅ Factory method enforces invariants at construction
Order order = Order.create("Gaming Laptop", new BigDecimal("1500.00"), "John");

// ✅ Business logic lives here, not in a Service
order.ship(); // CREATED → SHIPPED

// ✅ Guard clause protects the domain invariant
order.ship(); // throws InvalidOrderStateException — cannot ship twice
```

Key decisions:
- **Private constructor** — only `create()` and `reconstitute()` can build an `Order`.
- **No setters for business state** — `status` only changes via `ship()`.
- **`reconstitute()`** — a separate factory method for rebuilding from persisted data. This prevents the persistence layer from abusing `create()` (which would generate a new UUID every time).

#### `OrderRepository` — Output Port

```java
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
}
```

This interface is defined **inside the domain**, not the infrastructure. The domain says *"I need something that can save and find orders"* — it has no idea whether that's H2, PostgreSQL, or an in-memory map.

---

### 4.2 Application Layer

**Location:** `application/`  
**Rule:** Only imports `domain/` classes. No Spring, no JPA.

This layer **orchestrates** the use cases. It coordinates domain objects and the repository port to fulfill a business scenario, but it contains no business rules itself.

#### Use Case Pattern: Interface + Implementation

Each use case has two parts:

```java
// 1. The Input Port (interface) — the contract
public interface CreateOrderUseCase {
    Order execute(CreateOrderCommand command);
}

// 2. The Implementation — the orchestration
public class CreateOrderUseCaseImpl implements CreateOrderUseCase {
    private final OrderRepository orderRepository; // domain port, not JPA

    public Order execute(CreateOrderCommand command) {
        Order order = Order.create(                // delegate to domain
            command.getDescription(),
            command.getAmount(),
            command.getCustomer()
        );
        return orderRepository.save(order);        // use the port
    }
}
```

Why split interface and implementation?
- The `OrderController` depends on `CreateOrderUseCase` (the interface), not on `CreateOrderUseCaseImpl`.
- This makes the controller testable: you can inject a mock use case without Spring.

#### `CreateOrderCommand` — Command Object

```java
// The controller translates HTTP request → Command
// The command crosses the boundary into the application layer
CreateOrderCommand command = new CreateOrderCommand(
    request.getDescription(),
    request.getAmount(),
    request.getCustomer()
);
```

Why not pass the `CreateOrderRequest` (DTO) directly to the use case?  
Because the DTO has `@NotBlank` annotations (a Bean Validation / Jakarta concern) — the application layer must not know about Jakarta. The command is a plain Java object.

---

### 4.3 Infrastructure Layer

**Location:** `infrastructure/`  
**Rule:** The only layer that knows about Spring, JPA, HTTP.

#### Input Adapter — `OrderController`

Translates HTTP → Application:

```
HTTP Request (JSON)
      ↓
CreateOrderRequest (DTO with @NotBlank validation)
      ↓
CreateOrderCommand (plain object)
      ↓
CreateOrderUseCase.execute(command)
      ↓
Order (domain object)
      ↓
OrderResponse (DTO — never expose the domain entity directly)
      ↓
HTTP Response (JSON, 201 Created)
```

#### Output Adapter — `OrderPersistenceAdapter`

Translates Domain ↔ JPA:

```
Order (domain)
      ↓  OrderMapper.toJpaEntity()
OrderJpaEntity (@Entity, @Id, @Enumerated)
      ↓  JpaRepository.save()
Database (H2)
```

And in reverse for reads:

```
Database (H2)
      ↓  JpaRepository.findById()
OrderJpaEntity
      ↓  OrderMapper.toDomain()  →  Order.reconstitute(...)
Order (domain)
```

Why two separate entity classes (`Order` and `OrderJpaEntity`)?  
If `Order` had `@Entity` and `@Id`, the domain would depend on JPA. When you later add `@Column`, `@Table`, or change the column name, you'd be modifying a domain class for infrastructure reasons — a violation of the Single Responsibility Principle.

#### `ApplicationConfig` — Spring Wiring Without Polluting Application Layer

```java
@Configuration
public class ApplicationConfig {
    @Bean
    public CreateOrderUseCase createOrderUseCase(OrderRepository orderRepository) {
        return new CreateOrderUseCaseImpl(orderRepository);
    }
}
```

The use case implementations have **no `@Service` annotation**. Spring knows about them only through this config class, which lives in `infrastructure/`. The `application/` layer stays completely Spring-free.

---

## 5. Dependency Rule (The Golden Rule)

```
infrastructure  →  application  →  domain
     ↑                 ↑
  Spring, JPA       (none)       Pure Java
```

**What this means in practice:**

| Layer | Can import FROM | Cannot import FROM |
|---|---|---|
| `domain` | `java.*` only | `application`, `infrastructure`, Spring, JPA |
| `application` | `domain` | `infrastructure`, Spring, JPA |
| `infrastructure` | `domain`, `application`, Spring, JPA | — |

A quick way to verify: grep for `import org.springframework` or `import jakarta.persistence` in `domain/` or `application/`. There should be **zero results**.

---

## 6. Hexagonal Architecture — Ports & Adapters

```
                      ┌─────────────────────────────────────────┐
  [ HTTP Client ]     │                                         │
        │             │   ┌──────────────────────────────────┐  │
        ↓             │   │   Application Core               │  │
  OrderController     │   │                                  │  │
  (Input Adapter)  ───┼──▶│  CreateOrderUseCase (Input Port) │  │
                      │   │  ShipOrderUseCase   (Input Port) │  │
                      │   │  GetOrderUseCase    (Input Port) │  │
                      │   │                                  │  │
                      │   │  OrderRepository   (Output Port)─┼──┼──▶ OrderPersistenceAdapter
                      │   └──────────────────────────────────┘  │         (Output Adapter)
                      │                                         │               │
                      └─────────────────────────────────────────┘               ↓
                                                                          H2 Database
```

| Port Type | Interface | Direction |
|---|---|---|
| **Input Port** | `CreateOrderUseCase`, `ShipOrderUseCase`, `GetOrderUseCase` | External world drives the app |
| **Output Port** | `OrderRepository` | App drives external systems |

| Adapter Type | Class | Implements |
|---|---|---|
| **Input Adapter** | `OrderController` | Calls the Input Ports |
| **Output Adapter** | `OrderPersistenceAdapter` | Implements the Output Port |

---

## 7. Data Flow — Creating an Order (End-to-End)

```
POST /orders
Body: { "description": "Gaming Laptop", "amount": 1500.00, "customer": "John" }

1. OrderController.createOrder()
   - Spring deserializes JSON → CreateOrderRequest
   - Bean Validation validates @NotBlank, @Positive
   - Builds CreateOrderCommand (plain object, no annotations)
   - Calls createOrderUseCase.execute(command)

2. CreateOrderUseCaseImpl.execute()
   - Calls Order.create(description, amount, customer)
   - Returns Order with UUID generated, status=CREATED, createdAt=now()
   - Calls orderRepository.save(order)

3. OrderPersistenceAdapter.save()
   - OrderMapper.toJpaEntity(order) → OrderJpaEntity
   - jpaRepository.save(entity) → persists to H2
   - OrderMapper.toDomain(saved) → reconstitutes Order

4. Back in OrderController
   - OrderResponse.from(order) → DTO with all fields
   - Returns 201 Created + Location: /orders/{uuid} + body

HTTP Response:
{
  "id": "89626d21-0761-4a8c-ac64-1729746747e7",
  "description": "Gaming Laptop",
  "amount": 1500.00,
  "customer": "John",
  "status": "CREATED",
  "createdAt": "2026-03-24T15:00:00"
}
```

---

## 8. API Reference

### Create Order

```http
POST /orders
Content-Type: application/json

{
  "description": "Gaming Laptop",
  "amount": 1500.00,
  "customer": "John Doe"
}
```

**Response `201 Created`:**
```json
{
  "id": "89626d21-0761-4a8c-ac64-1729746747e7",
  "description": "Gaming Laptop",
  "amount": 1500.00,
  "customer": "John Doe",
  "status": "CREATED",
  "createdAt": "2026-03-24T15:00:00"
}
```

---

### Ship Order

```http
POST /orders/{id}/ship
```

**Response `200 OK`:**
```json
{
  "id": "89626d21-0761-4a8c-ac64-1729746747e7",
  "description": "Gaming Laptop",
  "amount": 1500.00,
  "customer": "John Doe",
  "status": "SHIPPED",
  "createdAt": "2026-03-24T15:00:00"
}
```

**Error `422 Unprocessable Entity`** (already shipped):
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Order '89626d21-...' has already been shipped and cannot be shipped again.",
  "timestamp": "2026-03-24T15:01:00"
}
```

---

### Get Order

```http
GET /orders/{id}
```

**Response `200 OK`:** Same shape as above.

**Error `404 Not Found`:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Order with id '00000000-...' not found.",
  "timestamp": "2026-03-24T15:01:00"
}
```

---

### Validation Errors

**Error `400 Bad Request`** (missing or invalid fields):
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "description must not be blank; amount must be greater than zero",
  "timestamp": "2026-03-24T15:01:00"
}
```

---

## 9. Running the Project

**Prerequisites:** Java 17+, Maven 3.8+

```bash
# Clone and run
cd order-service
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

**H2 Console** (in-memory database browser):
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:orderdb`
- Username: `sa` / Password: *(empty)*

**Quick test with curl:**
```bash
# 1. Create an order
curl -s -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"description":"Gaming Laptop","amount":1500.00,"customer":"John Doe"}' | jq .

# 2. Copy the "id" from the response, then ship it
curl -s -X POST http://localhost:8080/orders/{id}/ship | jq .

# 3. Try to ship again → 422
curl -s -X POST http://localhost:8080/orders/{id}/ship | jq .

# 4. Get the order
curl -s http://localhost:8080/orders/{id} | jq .
```

---

## 10. Running the Tests

```bash
mvn clean test
```

The 4 domain tests run **without a Spring context** — no database, no HTTP, just pure logic:

| Test | Verifies |
|---|---|
| `create_shouldInitializeWithCreatedStatus` | Factory method sets correct initial state |
| `ship_shouldChangeStatusToShipped_whenOrderIsCreated` | Happy path transition `CREATED → SHIPPED` |
| `ship_shouldThrowInvalidOrderStateException_whenOrderIsAlreadyShipped` | Domain invariant: cannot ship twice |
| `reconstitute_shouldPreserveAllValues` | Mapper can rebuild domain object without side effects |

These tests are the fastest possible — no Spring Boot startup, no database connection. They prove the business rules work independently of any framework.

---

## 11. Key Design Decisions

| Decision | Alternatives Considered | Why This One |
|---|---|---|
| `Order` has no `@Entity` | Single class with both annotations | JPA annotations in domain violate the Dependency Rule |
| Use case impls have no `@Service` | `@Service` on every impl | `@Service` imports Spring into the application layer |
| `OrderJpaRepository` is package-private | `public` interface | Only `OrderPersistenceAdapter` should use it; hides JPA from the rest |
| `OrderMapper` is a final class with static methods | Separate bean / MapStruct | Simple enough to not need a framework; static makes the intent clear |
| `reconstitute()` factory method | Use `create()` for all cases | `create()` generates a new UUID — wrong for DB reads |
| `CreateOrderCommand` as a POJO | Passing `CreateOrderRequest` directly | DTOs may have Jakarta annotations; commands must be framework-free |
| `POST /orders/{id}/ship` instead of `PATCH /orders/{id}` | `PATCH` with `{"status":"SHIPPED"}` | Explicit intent; follows CQRS command naming; prevents arbitrary state changes |

---

## 12. Common Mistakes This Architecture Prevents

| Anti-pattern | How this project avoids it |
|---|---|
| **Anemic Domain Model** — entities with only getters/setters, logic in Services | `Order.ship()` owns the state transition; use cases just orchestrate |
| **Fat Service** — one class doing validation, business logic, persistence, and HTTP | Three clear layers with single responsibility each |
| **Leaky Abstraction** — JPA entities returned directly from controllers | `OrderResponse` DTO always sits between the domain and the HTTP boundary |
| **Framework Lock-in** — business logic only works when Spring is running | Domain and application layers have zero Spring dependencies; testable in isolation |
| **Bi-directional dependencies** — infrastructure and domain knowing about each other | Dependency Rule enforced by package structure; impossible to import infrastructure from domain |

---

## 13. Learning Roadmap — Upcoming Improvements

The following enhancements are planned to make specific architectural benefits **tangible and testable**. Each one is designed to answer a concrete question about the architecture — not just explain it, but *prove* it in running code.

---

### Improvement 1 — Application Layer Unit Tests with Mockito

**Status:** ✅ Completed — 13 tests added, `mvn test` BUILD SUCCESS (17 total at the time)

**What:** Unit tests for `CreateOrderUseCaseImpl`, `ShipOrderUseCaseImpl`, and `GetOrderUseCaseImpl` using Mockito to mock `OrderRepository`.

**Why it matters:** The biggest claim of this architecture is *"business logic is testable without Spring, without a database, without HTTP"*. These tests prove it. If writing them were difficult, it would be evidence that the architecture is leaking framework concerns into the application layer.

**Files created:**
- `src/test/java/com/example/orderservice/application/usecase/impl/CreateOrderUseCaseImplTest.java` (4 tests)
- `src/test/java/com/example/orderservice/application/usecase/impl/ShipOrderUseCaseImplTest.java` (6 tests)
- `src/test/java/com/example/orderservice/application/usecase/impl/GetOrderUseCaseImplTest.java` (3 tests)

**Key technique — `ArgumentCaptor`:** Verifies the exact `Order` object passed to `save()`, confirming the use case correctly translates the command into a domain object before persisting it.

```java
@ExtendWith(MockitoExtension.class)  // No Spring — Mockito extension only
class CreateOrderUseCaseImplTest {

    @Mock  OrderRepository orderRepository;  // Mocked — no DB needed
    @InjectMocks  CreateOrderUseCaseImpl useCase;

    @Test
    void execute_shouldPersistTheOrderOnce() {
        var command = new CreateOrderCommand("Laptop", new BigDecimal("1500"), "Alice");
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(command);

        var captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());  // proves save() called once
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.CREATED);
    }
}
```

---

### Improvement 2 — Second Output Adapter: In-Memory Repository

**Status:** ✅ Completed — 6 tests added, `mvn test` BUILD SUCCESS (23 total)

**What:** `InMemoryOrderRepository` implementing the domain's `OrderRepository` port using a `ConcurrentHashMap<UUID, Order>`. Active only under the `inmemory` Spring profile.

**Why it matters:** This is the most concrete demonstration of the **Dependency Inversion Principle**. The same domain and application code runs with a completely different storage mechanism — zero lines change in `domain/` or `application/`. This is the architectural payoff.

**Files created/modified:**
- `src/main/java/com/example/orderservice/infrastructure/adapter/out/inmemory/InMemoryOrderRepository.java`
- `src/test/java/com/example/orderservice/infrastructure/adapter/out/inmemory/InMemoryOrderRepositoryTest.java` (6 tests)
- `OrderPersistenceAdapter.java` — added `@Profile("!inmemory")` (active by default)

**How to swap adapters — only the launch command changes:**
```bash
# Default: JPA + H2 (OrderPersistenceAdapter active)
mvn spring-boot:run

# In-memory adapter (InMemoryOrderRepository active)
mvn spring-boot:run -Dspring-boot.run.profiles=inmemory
```

```java
@Component
@Profile("inmemory")                        // active only with --profiles=inmemory
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<UUID, Order> store = new ConcurrentHashMap<>();

    @Override public Order save(Order order) { store.put(order.getId(), order); return order; }
    @Override public Optional<Order> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
}
```

The domain's `OrderRepository` interface does not change. The application's use cases do not change. Only the Spring profile selects which adapter is injected.

---

### Improvement 3 — Domain Events: `OrderShippedEvent`

**Status:** ✅ Completed — 3 new event-related tests added, `mvn test` BUILD SUCCESS (26 total)

**What:** After `order.ship()` succeeds, the use case publishes an `OrderShippedEvent` (plain Java record) through an output port. The infrastructure picks it up and logs it via Spring's `ApplicationEventPublisher`.

**Why it matters:** The domain communicates *that something happened* without knowing *who reacts or how*. A log, an email, a notification — all can be added as listeners without modifying `Order.java` or any use case. This is the **Open/Closed Principle** in action: the system is open for new reactions, closed for modification of the shipping logic.

**Files created/modified:**

| File | Layer | Responsibility |
|---|---|---|
| `domain/event/OrderShippedEvent.java` | `domain/` | Plain Java record — what happened, no Spring |
| `application/port/out/OrderEventPublisher.java` | `application/` | Output port interface — use case depends on this, not on Spring |
| `application/usecase/impl/ShipOrderUseCaseImpl.java` | `application/` | Publishes after `save()` succeeds — still zero Spring imports |
| `infrastructure/adapter/out/event/SpringOrderEventPublisher.java` | `infrastructure/` | Only class that knows `ApplicationEventPublisher` |
| `infrastructure/adapter/in/event/OrderShippedEventListener.java` | `infrastructure/` | Reacts to the event with `@EventListener` |
| `infrastructure/config/ApplicationConfig.java` | `infrastructure/` | Wires `OrderEventPublisher` into the `ShipOrderUseCase` bean |

**Key architectural flow:**
```
ShipOrderUseCaseImpl              (application — no Spring)
    └─ orderEventPublisher.publish(event)
           │
           ▼ [interface boundary]
SpringOrderEventPublisher         (infrastructure — has Spring)
    └─ applicationEventPublisher.publishEvent(event)
           │
           ▼ [Spring event bus]
OrderShippedEventListener         (infrastructure — reacts)
    └─ log.info("[DOMAIN EVENT] OrderShippedEvent received...")
```

**Adding a new reaction requires zero changes to existing code** — just add a new `@EventListener` method or class.

---

### Improvement 4 — `CancelOrder` Use Case

**Status:** ✅ Completed — 10 new tests added, `mvn test` BUILD SUCCESS (36 total)

**What:** New `CANCELLED` status in `OrderStatus`. `order.cancel()` with guard clauses in the aggregate — only `CREATED` orders can be cancelled. New `CancelOrderUseCase` port, implementation, and `POST /orders/{id}/cancel` endpoint.

**Why it matters:** Shows how to **extend the domain correctly**. The guard clause lives in `Order.cancel()` (a domain rule), not in a service or controller. The existing `GlobalExceptionHandler` already handles `InvalidOrderStateException` → 422 with zero changes. Adding this feature did not touch any other use case.

**Files created/modified:**
- `domain/model/OrderStatus.java` — `CANCELLED` added as third state
- `domain/model/Order.java` — `cancel()` with two guards; `ship()` also hardened to reject `CANCELLED` orders
- `application/usecase/CancelOrderUseCase.java` — new input port interface
- `application/usecase/impl/CancelOrderUseCaseImpl.java` — orchestrates find → cancel → save
- `infrastructure/adapter/in/OrderController.java` — `POST /orders/{id}/cancel` endpoint added
- `infrastructure/config/ApplicationConfig.java` — `cancelOrderUseCase` bean wired

**New API endpoint:**
```
POST /orders/{id}/cancel
  200 OK  → { ..., "status": "CANCELLED" }
  404     → order not found
  422     → order already SHIPPED or already CANCELLED
```

**Domain invariants in `Order.cancel()`:**
```java
public void cancel() {
    if (this.status == OrderStatus.SHIPPED) {
        throw new InvalidOrderStateException("...has already been shipped and cannot be cancelled.");
    }
    if (this.status == OrderStatus.CANCELLED) {
        throw new InvalidOrderStateException("...has already been cancelled.");
    }
    this.status = OrderStatus.CANCELLED;  // guard passed — state changes here
}
```

**Tests added (+10):**
- `OrderTest` — 3 new: `cancel()` happy path, reject if SHIPPED, reject if already CANCELLED
- `CancelOrderUseCaseImplTest` — 7 tests covering all orchestration paths

---

### Improvement 5 — Spring Profiles: `dev` (H2) vs `prod` (PostgreSQL)

**Status:** ✅ Completed

**What:** Split `application.properties` into profile-specific files. `application-dev.properties` retains the H2 config. `application-prod.properties` contains PostgreSQL connection settings. Switching environments requires only `-Dspring.profiles.active=prod`.

**Why it matters:** The persistence technology changes entirely — the domain and application layers have **zero changes**. Only infrastructure configuration changes. This makes the architectural claim visible in the build/deploy pipeline, not just in code diagrams.

**Files modified/created:**

| File | Role |
|---|---|
| `src/main/resources/application.properties` | Common settings only (`server.port`, logging, `spring.profiles.active=dev`) |
| `src/main/resources/application-dev.properties` | H2 in-memory datasource, H2 console, `ddl-auto=create-drop` |
| `src/main/resources/application-prod.properties` | PostgreSQL datasource via env vars, `ddl-auto=validate` |

**How to switch profiles:**

```bash
# Run with dev profile (default — H2)
mvn spring-boot:run

# Run with prod profile (PostgreSQL — requires a running PG instance)
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Or with a JAR
java -Dspring.profiles.active=prod -jar target/order-service.jar
```

**Key design in `application-prod.properties`:**
```properties
# Credentials are read from environment variables — never hardcoded
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/orderdb}
spring.datasource.username=${DB_USERNAME:orderuser}
spring.datasource.password=${DB_PASSWORD:changeme}

# validate = Hibernate never modifies the schema; use Flyway/Liquibase for migrations
spring.jpa.hibernate.ddl-auto=validate
```

**Tests:** 36 total, 0 failures — profile split has no impact on unit tests (they use no Spring context).

---

### Improvement 6 — Integration Tests with `@SpringBootTest`

**Status:** ✅ Completed

**What:** End-to-end tests that start the full Spring context with H2, then make real HTTP calls via `MockMvc`. Every endpoint is exercised — happy paths, 404s, and 422 state-conflict errors.

**Why it matters:** These tests contrast sharply with the domain and application unit tests — they are slower (Spring context boot + JPA) but provide end-to-end confidence that every layer wires together correctly. The contrast reinforces *why* you want most tests to be framework-free: the test pyramid (many fast unit tests, fewer slow integration tests). Both types are necessary; the architecture makes it easy to have both.

**File created:**

| File | Role |
|---|---|
| `src/test/java/com/example/orderservice/infrastructure/adapter/in/OrderControllerIntegrationTest.java` | 11 end-to-end tests via MockMvc |

**Key annotations used:**

```java
@SpringBootTest          // loads full Spring context (H2 datasource, JPA, all beans)
@AutoConfigureMockMvc    // injects MockMvc wired to the real DispatcherServlet
@ActiveProfiles("dev")   // guarantees H2 is always used, even if prod profile is default
@Transactional           // rolls back after each test — no manual table cleanup needed
```

**Tests added (11):**

| Scenario | Endpoint | Expected |
|---|---|---|
| Create with valid body | `POST /orders` | 201 + Location header + CREATED status |
| Create with invalid body | `POST /orders` | 400 Bad Request |
| Get existing order | `GET /orders/{id}` | 200 OK |
| Get non-existent order | `GET /orders/{id}` | 404 Not Found |
| Ship CREATED order | `POST /orders/{id}/ship` | 200 + SHIPPED |
| Ship non-existent order | `POST /orders/{id}/ship` | 404 Not Found |
| Ship already-SHIPPED order | `POST /orders/{id}/ship` | 422 Unprocessable Entity |
| Cancel CREATED order | `POST /orders/{id}/cancel` | 200 + CANCELLED |
| Cancel non-existent order | `POST /orders/{id}/cancel` | 404 Not Found |
| Cancel SHIPPED order | `POST /orders/{id}/cancel` | 422 Unprocessable Entity |
| Cancel already-CANCELLED order | `POST /orders/{id}/cancel` | 422 Unprocessable Entity |

**Total tests: 47 (36 unit + 11 integration), 0 failures.**

---

### Improvement 7 — Swagger / OpenAPI Documentation

**Status:** ✅ Completed

**What:** Added `springdoc-openapi-starter-webmvc-ui 2.3.0` to `pom.xml`. The full interactive API documentation is auto-generated and browsable at runtime. Endpoints, request/response schemas, status codes, and descriptions are all documented.

**Why it matters:** Closes the project as a production-like reference. Documentation lives alongside the code — it can never go out of sync because it is generated from the actual controller signatures and annotations. Importantly, this is *infrastructure*: the domain and application layers are completely unaware of Swagger.

**Files modified/created:**

| File | Change |
|---|---|
| `pom.xml` | Added `springdoc-openapi-starter-webmvc-ui:2.3.0` |
| `src/main/java/.../infrastructure/config/OpenApiConfig.java` | `@Bean OpenAPI` with title, description, version, contact, license |
| `src/main/java/.../adapter/in/OrderController.java` | `@Tag`, `@Operation`, `@ApiResponses` / `@ApiResponse` on each endpoint |
| `src/main/java/.../adapter/in/dto/CreateOrderRequest.java` | `@Schema` on class and each field (description + example) |
| `src/main/java/.../adapter/in/dto/OrderResponse.java` | `@Schema` on class and each field (description + example) |

**URLs available after `mvn spring-boot:run`:**

| URL | Content |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Interactive Swagger UI |
| `http://localhost:8080/v3/api-docs` | Raw OpenAPI JSON spec |

**Key annotations used:**

```java
// Controller-level grouping
@Tag(name = "Orders", description = "Order lifecycle management")

// Per-endpoint documentation
@Operation(summary = "Create a new order", description = "...")
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Order created",
        content = @Content(schema = @Schema(implementation = OrderResponse.class))),
    @ApiResponse(responseCode = "400", description = "Validation failure",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
})

// DTO field documentation
@Schema(description = "Order total amount (must be > 0)", example = "1500.00")
private BigDecimal amount;
```

**Tests:** 47 total, 0 failures — existing tests unchanged; Swagger auto-configuration is transparent to MockMvc tests.

---

### Summary Table

| # | Improvement | Architectural Concept Demonstrated | Status |
|---|---|---|---|
| 1 | Application layer Mockito tests | Testability without Spring | ✅ Done |
| 2 | `InMemoryOrderRepository` | Dependency Inversion, swappable adapters | ✅ Done |
| 3 | Domain Events (`OrderShippedEvent`) | Domain decoupled from side effects | ✅ Done |
| 4 | `CancelOrder` use case | Correct domain extension, guard clauses in aggregate | ✅ Done |
| 5 | Spring Profiles (H2 vs PostgreSQL) | Technology swap without touching domain/application | ✅ Done |
| 6 | `@SpringBootTest` integration tests | Test pyramid, integration vs unit trade-offs | ✅ Done |
| 7 | Swagger / OpenAPI | Production readiness, documentation as infrastructure | ✅ Done |
 