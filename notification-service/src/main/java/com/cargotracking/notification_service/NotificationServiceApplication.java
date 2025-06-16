package com.cargotracking.notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * NotificationServiceApplication - Ana uygulama sınıfı
 * 
 * Notification service'i için gerekli özellikleri etkinleştirir:
 * - MongoDB audit desteği
 * - Kafka consumer desteği  
 * - Service discovery (Eureka)
 * - Asenkron işlem desteği
 * - Scheduled task desteği
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableMongoAuditing
@EnableAsync
@EnableScheduling
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

}
