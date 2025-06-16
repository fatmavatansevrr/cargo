package com.cargotracking.analytics_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI analyticsServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cargo Tracking Analytics Service API")
                        .description("API documentation for the Analytics Service of the Cargo Tracking System")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Cargo Tracking Team")
                                .email("support@cargotracking.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
} 