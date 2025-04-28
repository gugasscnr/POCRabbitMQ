package com.example.rabbitmq;

import com.example.rabbitmq.config.RabbitMQConfig;
import com.example.rabbitmq.consumer.MessageConsumer;
import com.example.rabbitmq.producer.MessageProducer;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 * Main application to demonstrate RabbitMQ POC.
 * This class orchestrates the RabbitMQ demonstration by:
 * - Establishing connection to RabbitMQ
 * - Setting up exchanges and queues
 * - Starting consumers for each queue
 * - Providing an interactive console for sending messages
 */
public class RabbitMQApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQApplication.class);
    
    public static void main(String[] args) {
        logger.info("Starting RabbitMQ POC application");
        
        try (Connection connection = RabbitMQConfig.createConnection()) {
            logger.info("Connected to RabbitMQ successfully");
            System.out.println("Connected to RabbitMQ successfully");
            
            // Execute the application components with proper resource management
            try (MessageProducer producer = new MessageProducer(connection);
                 MessageConsumer consumer = new MessageConsumer(connection)) {
                
                // Setup exchanges and queues
                setupExchangesAndQueues(producer);
                
                // Start consumers
                startConsumers(consumer);
                
                // Interactive message sending
                sendMessagesInteractively(producer);
                
                logger.info("Application terminating normally");
                System.out.println("Application terminated");
            }
        } catch (Exception e) {
            logger.error("Error in RabbitMQ application: {}", e.getMessage(), e);
            System.err.println("Error in RabbitMQ application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sets up exchanges and queues for the POC.
     * Creates different types of exchanges (Direct, Fanout, Topic)
     * and binds them to appropriate queues with routing keys.
     *
     * @param producer The message producer instance used to set up exchanges and queues
     * @throws IOException If there's an error during exchange/queue declaration or binding
     */
    private static void setupExchangesAndQueues(MessageProducer producer) throws IOException {
        logger.info("Setting up exchanges and queues");
        
        try {
            // Declare exchanges
            producer.declareDirectExchange(RabbitMQConfig.DIRECT_EXCHANGE);
            producer.declareFanoutExchange(RabbitMQConfig.FANOUT_EXCHANGE);
            producer.declareTopicExchange(RabbitMQConfig.TOPIC_EXCHANGE);
            
            // Declare queues
            producer.declareQueue(RabbitMQConfig.QUEUE_1);
            producer.declareQueue(RabbitMQConfig.QUEUE_2);
            producer.declareQueue(RabbitMQConfig.QUEUE_3);
            
            // Bind queues to exchanges with routing keys
            // Direct exchange bindings
            producer.bindQueue(RabbitMQConfig.QUEUE_1, RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.ROUTING_KEY_1);
            producer.bindQueue(RabbitMQConfig.QUEUE_2, RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.ROUTING_KEY_2);
            
            // Fanout exchange bindings (no routing key needed)
            producer.bindQueue(RabbitMQConfig.QUEUE_1, RabbitMQConfig.FANOUT_EXCHANGE, "");
            producer.bindQueue(RabbitMQConfig.QUEUE_2, RabbitMQConfig.FANOUT_EXCHANGE, "");
            
            // Topic exchange bindings
            producer.bindQueue(RabbitMQConfig.QUEUE_3, RabbitMQConfig.TOPIC_EXCHANGE, RabbitMQConfig.TOPIC_KEY_PATTERN);
            
            logger.info("Exchanges and queues setup completed successfully");
            System.out.println("Exchanges and queues setup completed");
        } catch (IOException e) {
            logger.error("Failed to set up exchanges and queues: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Starts consumers for each queue.
     * Creates a consumer for each defined queue to demonstrate
     * different message routing patterns.
     *
     * @param consumer The message consumer instance used to start consuming
     * @throws IOException If there's an error starting consumers
     */
    private static void startConsumers(MessageConsumer consumer) throws IOException {
        logger.info("Starting message consumers");
        
        try {
            // Start a consumer for each queue
            consumer.startConsuming(RabbitMQConfig.QUEUE_1, "consumer-1");
            consumer.startConsuming(RabbitMQConfig.QUEUE_2, "consumer-2");
            consumer.startConsuming(RabbitMQConfig.QUEUE_3, "consumer-3");
            
            logger.info("All consumers started successfully");
            System.out.println("Consumers started and ready to receive messages");
        } catch (IOException e) {
            logger.error("Failed to start consumers: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Interactive console to send messages.
     * Provides a command-line interface for the user to select
     * exchange types and send messages with appropriate routing keys.
     *
     * @param producer The message producer instance used to send messages
     */
    private static void sendMessagesInteractively(MessageProducer producer) {
        logger.info("Starting interactive message sending console");
        
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            
            while (running) {
                System.out.println("\nSelect exchange type to send message:");
                System.out.println("1. Direct Exchange");
                System.out.println("2. Fanout Exchange");
                System.out.println("3. Topic Exchange");
                System.out.println("0. Exit");
                
                System.out.print("Enter your choice: ");
                String choice = scanner.nextLine();
                
                try {
                    switch (choice) {
                        case "1":
                            sendDirectExchangeMessage(scanner, producer);
                            break;
                        case "2":
                            sendFanoutExchangeMessage(scanner, producer);
                            break;
                        case "3":
                            sendTopicExchangeMessage(scanner, producer);
                            break;
                        case "0":
                            logger.info("User requested application exit");
                            running = false;
                            break;
                        default:
                            logger.warn("Invalid menu choice entered: {}", choice);
                            System.out.println("Invalid choice. Try again.");
                    }
                } catch (IOException e) {
                    logger.error("Error sending message: {}", e.getMessage(), e);
                    System.err.println("Error sending message: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Sends a message to a direct exchange.
     * Allows the user to select a specific routing key and
     * provide a message to be sent through a direct exchange.
     *
     * @param scanner The scanner to read user input
     * @param producer The message producer to send the message
     * @throws IOException If there's an error sending the message
     */
    private static void sendDirectExchangeMessage(Scanner scanner, MessageProducer producer) throws IOException {
        logger.debug("Preparing to send Direct Exchange message");
        
        System.out.println("\n--- Direct Exchange Message ---");
        System.out.println("Select routing key:");
        System.out.println("1. " + RabbitMQConfig.ROUTING_KEY_1);
        System.out.println("2. " + RabbitMQConfig.ROUTING_KEY_2);
        
        System.out.print("Enter your choice (1-2): ");
        String keyChoice = scanner.nextLine();
        
        String routingKey;
        if ("1".equals(keyChoice)) {
            routingKey = RabbitMQConfig.ROUTING_KEY_1;
        } else if ("2".equals(keyChoice)) {
            routingKey = RabbitMQConfig.ROUTING_KEY_2;
        } else {
            logger.warn("Invalid routing key choice: {}, using default", keyChoice);
            System.out.println("Invalid choice, using default routing key.");
            routingKey = RabbitMQConfig.ROUTING_KEY_1;
        }
        
        System.out.print("Enter message to send: ");
        String message = scanner.nextLine();
        
        logger.info("Sending message to Direct Exchange with routing key: {}", routingKey);
        producer.sendMessage(RabbitMQConfig.DIRECT_EXCHANGE, routingKey, message);
    }
    
    /**
     * Sends a message to a fanout exchange.
     * Gathers a message from the user and sends it to a
     * fanout exchange (where routing key is ignored).
     *
     * @param scanner The scanner to read user input
     * @param producer The message producer to send the message
     * @throws IOException If there's an error sending the message
     */
    private static void sendFanoutExchangeMessage(Scanner scanner, MessageProducer producer) throws IOException {
        logger.debug("Preparing to send Fanout Exchange message");
        
        System.out.println("\n--- Fanout Exchange Message ---");
        System.out.print("Enter message to send: ");
        String message = scanner.nextLine();
        
        // In fanout exchange, routing key is ignored
        logger.info("Sending message to Fanout Exchange");
        producer.sendMessage(RabbitMQConfig.FANOUT_EXCHANGE, "", message);
    }
    
    /**
     * Sends a message to a topic exchange.
     * Allows the user to specify a topic routing key and message content
     * to be sent through a topic exchange.
     *
     * @param scanner The scanner to read user input
     * @param producer The message producer to send the message
     * @throws IOException If there's an error sending the message
     */
    private static void sendTopicExchangeMessage(Scanner scanner, MessageProducer producer) throws IOException {
        logger.debug("Preparing to send Topic Exchange message");
        
        System.out.println("\n--- Topic Exchange Message ---");
        System.out.print("Enter topic routing key (e.g., poc.topic.example): ");
        String routingKey = scanner.nextLine();
        
        System.out.print("Enter message to send: ");
        String message = scanner.nextLine();
        
        logger.info("Sending message to Topic Exchange with routing key: {}", routingKey);
        producer.sendMessage(RabbitMQConfig.TOPIC_EXCHANGE, routingKey, message);
    }
}
