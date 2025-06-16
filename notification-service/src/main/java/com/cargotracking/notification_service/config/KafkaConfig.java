package com.cargotracking.notification_service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaConfig - Kafka consumer configuration
 * Event-driven notification processing için Kafka ayarları
 */
@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;
    
    /**
     * Kafka Consumer Factory
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Bootstrap servers
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Consumer group
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        
        // Auto offset reset strategy
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        
        // Deserializers
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // Performance tuning
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5 minutes
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30 seconds
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000); // 10 seconds
        
        // Reliability settings
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit
        configProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        
        // Retry settings
        configProps.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        configProps.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 5000);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    /**
     * Kafka Listener Container Factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Concurrent consumer settings
        factory.setConcurrency(3); // 3 consumer thread per partition
        
        // Acknowledgment mode - manual acknowledgment
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        
        // Error handling
        factory.setCommonErrorHandler(kafkaErrorHandler());
        
        // Batch processing settings
        factory.setBatchListener(false); // Single record processing
        
        // Auto startup
        factory.setAutoStartup(true);
        
        return factory;
    }
    
    /**
     * Kafka Error Handler
     */
    @Bean
    public org.springframework.kafka.listener.CommonErrorHandler kafkaErrorHandler() {
        org.springframework.util.backoff.FixedBackOff fixedBackOff = new org.springframework.util.backoff.FixedBackOff();
        fixedBackOff.setInterval(1000L); // 1 second retry interval
        fixedBackOff.setMaxAttempts(3L); // max 3 retry attempts
        
        return new org.springframework.kafka.listener.DefaultErrorHandler(
            (consumerRecord, exception) -> {
                // Dead Letter Queue logic can be implemented here
                System.err.printf("Error processing record: topic=%s, partition=%d, offset=%d, error=%s%n",
                    consumerRecord.topic(),
                    consumerRecord.partition(),
                    consumerRecord.offset(),
                    exception.getMessage());
            },
            fixedBackOff
        );
    }
    
    /**
     * High priority consumer factory for critical notifications
     */
    @Bean
    public ConsumerFactory<String, String> highPriorityConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-priority");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // Higher priority settings
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); // Process fewer records at once
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1); // Don't wait for more data
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500); // Max 500ms wait
        
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    /**
     * High priority listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> highPriorityKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(highPriorityConsumerFactory());
        factory.setConcurrency(5); // More threads for priority processing
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(kafkaErrorHandler());
        
        return factory;
    }
} 