package com.example.rabbitmq;

import com.example.rabbitmq.config.RabbitMQConfig;
import com.example.rabbitmq.consumer.MessageConsumer;
import com.example.rabbitmq.producer.MessageProducer;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Main application to demonstrate RabbitMQ POC.
 * This class orchestrates the RabbitMQ demonstration by:
 * - Establishing connection to RabbitMQ
 * - Setting up exchange and queue
 * - Starting consumer for the queue
 * - Sending messages with custom headers
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
                
                // Setup exchange and queue
                setupExchangeAndQueue(producer);
                
                // Start consumer
                startConsumer(consumer);
                
                // Send message with headers
                sendMessageWithHeaders(producer);
                
                logger.info("Application terminating normally");
                System.out.println("Message sent successfully. Application terminated.");
            }
        } catch (Exception e) {
            logger.error("Error in RabbitMQ application: {}", e.getMessage(), e);
            System.err.println("Error in RabbitMQ application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sets up exchange and queue for the POC.
     * Creates the exchange and binds it to the queue with the appropriate routing key.
     *
     * @param producer The message producer instance used to set up exchange and queue
     * @throws IOException If there's an error during exchange/queue declaration or binding
     */
    private static void setupExchangeAndQueue(MessageProducer producer) throws IOException {
        logger.info("Setting up exchange and queue");
        
        try {
            // Declare exchange
            producer.declareExchange(RabbitMQConfig.TRADE_EXCHANGE);
            
            // Declare queue
            producer.declareQueue(RabbitMQConfig.QUEUE_NAME);
            
            // Bind queue to exchange with routing key
            producer.bindQueue(RabbitMQConfig.QUEUE_NAME, RabbitMQConfig.TRADE_EXCHANGE, RabbitMQConfig.ROUTING_KEY);
            
            logger.info("Exchange and queue setup completed successfully");
        } catch (IOException e) {
            logger.error("Failed to setup exchange and queue: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Starts consumer for the queue.
     *
     * @param consumer The message consumer instance used to start consuming
     * @throws IOException If there's an error starting the consumer
     */
    private static void startConsumer(MessageConsumer consumer) throws IOException {
        logger.info("Starting consumer");
        try {
            consumer.startConsuming(RabbitMQConfig.QUEUE_NAME, "consumer-1");
            logger.info("Consumer started successfully");
        } catch (IOException e) {
            logger.error("Failed to start consumer: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Sends a message with custom headers to the exchange.
     *
     * @param producer The message producer instance used to send message
     * @throws IOException If there's an error sending the message
     */
    private static void sendMessageWithHeaders(MessageProducer producer) throws IOException {
        logger.info("Preparing to send message with headers");
        
        try {
            // Generate unique IDs and timestamps
            String tradeId = generateTradeId();
            String eventId = UUID.randomUUID().toString();
            String timestamp = generateIsoTimestamp();
            String eventDate = generateEventDate();
            String eventReferenceDate = generateReferenceDate();
            String backofficeStatus = "test.qa.123";
            
            // Create message content
            String message = createMessageContent(tradeId, timestamp, backofficeStatus);
            
            // Create headers
            Map<String, Object> headers = createHeaders(eventId, timestamp, eventDate, eventReferenceDate, tradeId, backofficeStatus);
            
            // Log details
            logger.info("Sending message to exchange '{}' with routing key '{}'", 
                    RabbitMQConfig.TRADE_EXCHANGE, RabbitMQConfig.ROUTING_KEY);
            logger.debug("Trade ID: {}", tradeId);
            logger.debug("Event ID: {}", eventId);
            logger.debug("Object Update Time: {}", timestamp);
            logger.debug("Backoffice Status: {}", backofficeStatus);
            
            // Send message
            producer.sendMessage(RabbitMQConfig.TRADE_EXCHANGE, RabbitMQConfig.ROUTING_KEY, message, headers);
            
            logger.info("Message sent successfully");
        } catch (IOException e) {
            logger.error("Failed to send message: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Generates a random trade ID.
     *
     * @return A string representing the trade ID
     */
    private static String generateTradeId() {
        long min = 100000000000L;
        long max = 999999999999L;
        long tradeId = min + (long) (Math.random() * (max - min));
        return String.valueOf(tradeId);
    }
    
    /**
     * Generates an ISO 8601 timestamp in UTC format (Zulu).
     *
     * @return The formatted timestamp string
     */
    private static String generateIsoTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return sdf.format(new Date());
    }
    
    /**
     * Generates an event date string.
     *
     * @return The formatted event date string
     */
    private static String generateEventDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(new Date());
    }
    
    /**
     * Generates a reference date string.
     *
     * @return The formatted reference date string
     */
    private static String generateReferenceDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }
    
    /**
     * Creates the message content in JSON format.
     *
     * @param tradeId The trade ID
     * @param timestamp The timestamp
     * @param backofficeStatus The backoffice status
     * @return A JSON string representing the message
     */
    private static String createMessageContent(String tradeId, String timestamp, String backofficeStatus) {
        // Simplified JSON structure - in a real application, use a proper JSON library
        return "{\n" +
               "  \"tradeId\": \"" + tradeId + "\",\n" +
               "  \"timestamp\": \"" + timestamp + "\",\n" +
               "  \"status\": \"Matched\",\n" +
               "  \"backofficeStatus\": \"" + backofficeStatus + "\",\n" +
               "  \"objUpdateTime\": \"" + timestamp + "\",\n" +
               "  \"type\": \"Unblock\"\n" +
               "}";
    }
    
    /**
     * Creates headers for the message.
     *
     * @param eventId The event ID
     * @param timestamp The timestamp
     * @param eventDate The event date
     * @param eventReferenceDate The event reference date
     * @param tradeId The trade ID
     * @param backofficeStatus The backoffice status
     * @return A map of headers
     */
    private static Map<String, Object> createHeaders(String eventId, String timestamp, 
            String eventDate, String eventReferenceDate, String tradeId, String backofficeStatus) {
        Map<String, Object> headers = new HashMap<>();
        
        // Event headers
        headers.put("event.layout", "panorama-trade-v1");
        headers.put("objectType", "position_trade");
        headers.put("productType", "emissao_emissao1_efic");
        headers.put("account", "00009300");
        headers.put("accountingGroup", "CDB");
        headers.put("tradeType", "buy");
        headers.put("eventType", backofficeStatus);
        headers.put("eventId", eventId);
        headers.put("correlationId", "custody-engine-" + eventReferenceDate);
        headers.put("eventDate", eventDate);
        headers.put("eventReferenceDate", eventReferenceDate);
        
        // Processing headers
        headers.put("content-type", "application/json");
        headers.put("app-id", "java-rabbitmq-poc");
        headers.put("timestamp", timestamp);
        headers.put("x-trade-id", tradeId);
        headers.put("x-backoffice-status", backofficeStatus);
        
        return headers;
    }
}
