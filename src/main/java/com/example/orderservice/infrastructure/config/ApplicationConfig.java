package com.example.orderservice.infrastructure.config;

import com.example.orderservice.application.port.out.OrderEventPublisher;
import com.example.orderservice.application.usecase.CancelOrderUseCase;
import com.example.orderservice.application.usecase.CreateOrderUseCase;
import com.example.orderservice.application.usecase.GetOrderUseCase;
import com.example.orderservice.application.usecase.ShipOrderUseCase;
import com.example.orderservice.application.usecase.impl.CancelOrderUseCaseImpl;
import com.example.orderservice.application.usecase.impl.CreateOrderUseCaseImpl;
import com.example.orderservice.application.usecase.impl.GetOrderUseCaseImpl;
import com.example.orderservice.application.usecase.impl.ShipOrderUseCaseImpl;
import com.example.orderservice.domain.repository.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring wiring for the application layer use cases.
 *
 * Use case implementations are intentionally plain Java classes (no @Service).
 * Spring is aware of them only here, in the infrastructure layer — maintaining
 * the Clean Architecture rule: the application layer has zero framework dependencies.
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public CreateOrderUseCase createOrderUseCase(OrderRepository orderRepository) {
        return new CreateOrderUseCaseImpl(orderRepository);
    }

    @Bean
    public ShipOrderUseCase shipOrderUseCase(OrderRepository orderRepository,
                                             OrderEventPublisher orderEventPublisher) {
        return new ShipOrderUseCaseImpl(orderRepository, orderEventPublisher);
    }

    @Bean
    public GetOrderUseCase getOrderUseCase(OrderRepository orderRepository) {
        return new GetOrderUseCaseImpl(orderRepository);
    }

    @Bean
    public CancelOrderUseCase cancelOrderUseCase(OrderRepository orderRepository) {
        return new CancelOrderUseCaseImpl(orderRepository);
    }
}
