package com.example.rabbitmq.consumer;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Consumer class for receiving messages from RabbitMQ.
 * This class handles message consumption from RabbitMQ queues including:
 * - Setting up consumers with appropriate callback handlers
 * - Message acknowledgment
 * - Formatted message display
 */
public class MessageConsumer implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    
    private final Connection connection;
    private final Channel channel;
    
    /**
     * Constructor that initializes the connection and channel.
     * 
     * @param connection An existing RabbitMQ connection
     * @throws IOException If a channel cannot be created from the connection
     */
    public MessageConsumer(Connection connection) throws IOException {
        this.connection = connection;
        this.channel = connection.createChannel();
        logger.debug("Created MessageConsumer with channel number: {}", channel.getChannelNumber());
    }
    
    /**
     * Starts consuming messages from a specific queue.
     * Sets up callback handlers for message delivery and cancellation,
     * and configures quality of service (QoS) settings.
     * 
     * @param queueName The name of the queue to consume from
     * @param consumerTag A unique identifier for this consumer
     * @throws IOException If consumption cannot be started
     */
    public void startConsuming(String queueName, String consumerTag) throws IOException {
        logger.info("Starting consumer '{}' for queue: {}", consumerTag, queueName);
        System.out.println("Starting consumer '" + consumerTag + "' for queue: " + queueName);
        
        // Set prefetch count to 1 to ensure fair dispatch
        channel.basicQos(1);
        
        DeliverCallback deliverCallback = (consumerTag1, delivery) -> {
            try {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                String exchange = delivery.getEnvelope().getExchange();
                String routingKey = delivery.getEnvelope().getRoutingKey();
                Map<String, Object> headers = delivery.getProperties().getHeaders();
                
                logger.info("Received message from exchange: {} with routing key: {}", exchange, routingKey);
                logger.debug("Message content: '{}'", message);
                logger.debug("Headers: {}", headers);
                
                // Print formatted message
                printFormattedMessage(consumerTag1, message, exchange, routingKey, headers);
                
                // Acknowledge message receipt
                try {
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    logger.debug("Acknowledged message delivery tag: {}", delivery.getEnvelope().getDeliveryTag());
                } catch (IOException e) {
                    logger.error("Failed to acknowledge message: {}", e.getMessage(), e);
                }
                
                // Display menu again for better UX
                printMenu();
            } catch (Exception e) {
                logger.error("Error processing delivered message: {}", e.getMessage(), e);
                // In case of exception, attempt to nack the message
                try {
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                } catch (IOException nackException) {
                    logger.error("Failed to nack message after processing error: {}", nackException.getMessage());
                }
            }
        };
        
        CancelCallback cancelCallback = consumerTag1 -> {
            logger.info("Consumer {} cancelled", consumerTag1);
            System.out.println("Consumer " + consumerTag1 + " cancelled");
        };
        
        // Start consuming
        channel.basicConsume(queueName, false, consumerTag, deliverCallback, cancelCallback);
        logger.info("Consumer '{}' successfully registered", consumerTag);
    }
    
    /**
     * Prints the received message in a formatted box.
     * 
     * @param consumerTag The consumer tag that received the message
     * @param message The message content
     * @param exchange The exchange from which the message was received
     * @param routingKey The routing key used
     * @param headers The headers from the message
     */
    private void printFormattedMessage(String consumerTag, String message, String exchange, String routingKey, Map<String, Object> headers) {
        System.out.println("\n");
        System.out.println("##############################################");
        System.out.println("##          MENSAGEM RECEBIDA              ##");
        System.out.println("##############################################");
        System.out.println("# Consumer: " + consumerTag);
        System.out.println("# Exchange: " + exchange);
        System.out.println("# Routing Key: " + routingKey);
        
        // Format message with truncation for long messages
        if (message.length() > 100) {
            System.out.println("# Mensagem: " + message.substring(0, 100) + "...");
        } else {
            System.out.println("# Mensagem: " + message);
        }
        
        // Format headers if present
        if (headers != null && !headers.isEmpty()) {
            System.out.println("# Headers:");
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                System.out.println("#   " + entry.getKey() + ": " + entry.getValue());
            }
        }
        
        System.out.println("##############################################");
    }
    
    /**
     * Prints the menu options for the interactive console.
     */
    private void printMenu() {
        System.out.println("\nSelect exchange type to send message:");
        System.out.println("1. Direct Exchange");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }
    
    /**
     * Closes the channel.
     * Implements AutoCloseable interface for use with try-with-resources.
     *
     * @throws IOException If there's an error closing the channel
     * @throws TimeoutException If there's a timeout during closing
     */
    @Override
    public void close() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) {
            logger.debug("Closing channel: {}", channel.getChannelNumber());
            channel.close();
        }
        logger.debug("MessageConsumer resources closed");
    }
}
