package com.example.orderservice.infrastructure.adapter.in;

import com.example.orderservice.application.usecase.CancelOrderUseCase;
import com.example.orderservice.application.usecase.CreateOrderUseCase;
import com.example.orderservice.application.usecase.GetOrderUseCase;
import com.example.orderservice.application.usecase.ShipOrderUseCase;
import com.example.orderservice.application.usecase.command.CreateOrderCommand;
import com.example.orderservice.domain.model.Order;
import com.example.orderservice.infrastructure.adapter.in.dto.ApiErrorResponse;
import com.example.orderservice.infrastructure.adapter.in.dto.CreateOrderRequest;
import com.example.orderservice.infrastructure.adapter.in.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * Input Adapter — REST controller exposing the Order domain via HTTP.
 *
 * Responsibilities:
 *   - Deserialize and validate incoming requests.
 *   - Translate HTTP input to application commands.
 *   - Delegate to use case ports (never to implementations directly).
 *   - Serialize domain results to response DTOs.
 *
 * Does NOT contain business logic.
 */
@Tag(name = "Orders", description = "Order lifecycle management: create, retrieve, ship, and cancel orders")
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final ShipOrderUseCase shipOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase,
                           ShipOrderUseCase shipOrderUseCase,
                           GetOrderUseCase getOrderUseCase,
                           CancelOrderUseCase cancelOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.shipOrderUseCase = shipOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
    }

    // ── POST /orders ──────────────────────────────────────────────────────────

    /**
     * Creates a new order.
     *
     * Request body example:
     * {
     *   "description": "Gaming Laptop",
     *   "amount": 1500.00,
     *   "customer": "John Doe"
     * }
     *
     * Response: 201 Created + Location header + OrderResponse body.
     */
    @Operation(summary = "Create a new order", description = "Creates an order in CREATED status and returns its URI in the Location header.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order created",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body (validation failure)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand(
                request.getDescription(),
                request.getAmount(),
                request.getCustomer()
        );

        Order order = createOrderUseCase.execute(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(order.getId())
                .toUri();

        return ResponseEntity.created(location).body(OrderResponse.from(order));
    }

    // ── POST /orders/{id}/ship ────────────────────────────────────────────────

    /**
     * Ships an existing order.
     *
     * Response: 200 OK + OrderResponse body with status SHIPPED.
     * Errors:
     *   - 404 if the order does not exist.
     *   - 422 if the order is already shipped.
     */
    @Operation(summary = "Ship an order", description = "Transitions a CREATED order to SHIPPED status. Rejects orders already SHIPPED or CANCELLED.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order shipped",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "Invalid state transition",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{id}/ship")
    public ResponseEntity<OrderResponse> shipOrder(@PathVariable UUID id) {
        Order order = shipOrderUseCase.execute(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    // ── GET /orders/{id} ──────────────────────────────────────────────────────

    /**
     * Retrieves an order by its UUID.
     *
     * Response: 200 OK + OrderResponse body.
     * Errors:
     *   - 404 if the order does not exist.
     */
    @Operation(summary = "Get an order by ID", description = "Retrieves the full order details for the given UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order found",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        Order order = getOrderUseCase.execute(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    // ── POST /orders/{id}/cancel ─────────────────────────────────────────────────

    /**
     * Cancels an existing order.
     *
     * Response: 200 OK + OrderResponse body with status CANCELLED.
     * Errors:
     *   - 404 if the order does not exist.
     *   - 422 if the order has already been shipped or cancelled.
     */
    @Operation(summary = "Cancel an order", description = "Transitions a CREATED order to CANCELLED status. Rejects orders already SHIPPED or CANCELLED.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order cancelled",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "Invalid state transition",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID id) {
        Order order = cancelOrderUseCase.execute(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }
}
