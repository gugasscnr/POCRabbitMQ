package com.example.rabbitmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application for RabbitMQ API.
 * This application provides a REST API for:
 * - Sending messages to RabbitMQ with custom configuration
 * - Setting up exchanges and queues
 * - Configuring routing keys and binding patterns
 */
@SpringBootApplication
public class RabbitMQApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RabbitMQApplication.class, args);
    }
}
