package com.cargotracking.user_management_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * User Management Service Application
 * Requirements: FR-UM-001 to FR-UM-007, NFR-SEC-002, NFR-OBSV-003
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UserManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserManagementServiceApplication.class, args);
	}

}
