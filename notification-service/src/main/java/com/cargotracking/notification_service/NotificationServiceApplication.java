package com.cargotracking.notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Notification Service Ana Uygulama Sınıfı
 * Kafka eventlerini dinler ve bildirim gönderir
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableAsync
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

} 