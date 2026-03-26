package com.example.orderservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global OpenAPI / Swagger metadata.
 *
 * The interactive UI is available at:
 *   http://localhost:8080/swagger-ui.html
 *
 * The raw OpenAPI JSON spec is at:
 *   http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("""
                                REST API for the Order Service — built with Hexagonal Architecture, \
                                Domain-Driven Design, and Clean Architecture.
                                
                                Demonstrates how the infrastructure layer (this API) is a mere \
                                adapter: all business rules live in the domain and application layers \
                                with zero framework dependencies.
                                """)
                        .version("0.0.1-SNAPSHOT")
                        .contact(new Contact()
                                .name("Order Service Team")
                                .email("team@example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
