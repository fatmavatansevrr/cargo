package com.cargotracking.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Application
 * Requirements: FR-GW-001 to FR-GW-004
 * - FR-GW-001: Tüm harici istemci istekleri API Gateway üzerinden yönlendirilir
 * - FR-GW-002: Merkezi kimlik doğrulama (authentication)
 * - FR-GW-003: Rate limiting (istek sınırlama)
 * - FR-GW-004: Response aggregation (gerekirse)
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
}