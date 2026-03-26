package com.example.orderservice.infrastructure.adapter.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests — start the full Spring context (H2) and exercise the HTTP layer.
 *
 * Contrast with the unit tests in the application layer:
 *   - These tests are slower (Spring context boot + JPA) but provide end-to-end confidence.
 *   - Unit tests are fast and test one class in isolation with Mockito.
 *
 * The @Transactional annotation rolls back every test method, keeping tests independent
 * without needing to truncate tables manually.
 *
 * @ActiveProfiles("dev") ensures H2 is always used, regardless of the default profile
 * set in application.properties.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── POST /orders ──────────────────────────────────────────────────────────

    @Test
    void createOrder_shouldReturn201WithLocationHeaderAndBody() throws Exception {
        String body = """
                {"description":"Gaming Laptop","amount":1500.00,"customer":"Alice"}
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.description").value("Gaming Laptop"))
                .andExpect(jsonPath("$.customer").value("Alice"))
                .andExpect(jsonPath("$.amount").value(1500.00));
    }

    @Test
    void createOrder_shouldReturn400WhenRequestBodyIsInvalid() throws Exception {
        // description is blank, amount is negative, customer is blank
        String body = """
                {"description":"","amount":-1,"customer":""}
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ── GET /orders/{id} ──────────────────────────────────────────────────────

    @Test
    void getOrder_shouldReturn200WithOrderBody() throws Exception {
        String orderId = createOrderAndGetId();

        mockMvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getOrder_shouldReturn404WhenOrderDoesNotExist() throws Exception {
        mockMvc.perform(get("/orders/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── POST /orders/{id}/ship ────────────────────────────────────────────────

    @Test
    void shipOrder_shouldReturn200WithStatusShipped() throws Exception {
        String orderId = createOrderAndGetId();

        mockMvc.perform(post("/orders/" + orderId + "/ship"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void shipOrder_shouldReturn404WhenOrderDoesNotExist() throws Exception {
        mockMvc.perform(post("/orders/" + UUID.randomUUID() + "/ship"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shipOrder_shouldReturn422WhenOrderIsAlreadyShipped() throws Exception {
        String orderId = createOrderAndGetId();

        // First ship succeeds
        mockMvc.perform(post("/orders/" + orderId + "/ship"))
                .andExpect(status().isOk());

        // Second ship is rejected — 422
        mockMvc.perform(post("/orders/" + orderId + "/ship"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    // ── POST /orders/{id}/cancel ──────────────────────────────────────────────

    @Test
    void cancelOrder_shouldReturn200WithStatusCancelled() throws Exception {
        String orderId = createOrderAndGetId();

        mockMvc.perform(post("/orders/" + orderId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelOrder_shouldReturn404WhenOrderDoesNotExist() throws Exception {
        mockMvc.perform(post("/orders/" + UUID.randomUUID() + "/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void cancelOrder_shouldReturn422WhenOrderIsAlreadyShipped() throws Exception {
        String orderId = createOrderAndGetId();

        // Ship the order first
        mockMvc.perform(post("/orders/" + orderId + "/ship"))
                .andExpect(status().isOk());

        // Cancelling a shipped order is rejected — 422
        mockMvc.perform(post("/orders/" + orderId + "/cancel"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void cancelOrder_shouldReturn422WhenOrderIsAlreadyCancelled() throws Exception {
        String orderId = createOrderAndGetId();

        // First cancel succeeds
        mockMvc.perform(post("/orders/" + orderId + "/cancel"))
                .andExpect(status().isOk());

        // Second cancel is rejected — 422
        mockMvc.perform(post("/orders/" + orderId + "/cancel"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Creates an order via the real HTTP endpoint and returns its UUID string.
     * Reused across tests that need an existing order as a precondition.
     */
    private String createOrderAndGetId() throws Exception {
        String body = """
                {"description":"Test Order","amount":100.00,"customer":"Test Customer"}
                """;

        MvcResult result = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asText();
    }
}
