package com.example.rabbitmq.config;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Configuration class for RabbitMQ connection settings and constants.
 * Provides a central location for all RabbitMQ configuration parameters and
 * static methods to create connections to RabbitMQ server.
 */
public class RabbitMQConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);
    
    // Connection parameters
    private static final String HOST = "localhost";
    private static final int PORT = 5672;
    private static final String USERNAME = "guest";
    private static final String PASSWORD = "guest";
    
    // Exchange names
    public static final String DIRECT_EXCHANGE = "poc.direct.exchange";
    public static final String FANOUT_EXCHANGE = "poc.fanout.exchange";
    public static final String TOPIC_EXCHANGE = "poc.topic.exchange";
    
    // Queue names
    public static final String QUEUE_1 = "poc.queue.one";
    public static final String QUEUE_2 = "poc.queue.two";
    public static final String QUEUE_3 = "poc.queue.three";
    
    // Routing keys
    public static final String ROUTING_KEY_1 = "poc.key.one";
    public static final String ROUTING_KEY_2 = "poc.key.two";
    public static final String TOPIC_KEY_PATTERN = "poc.topic.#";
    
    /**
     * Creates and returns a connection to RabbitMQ server using the
     * configured connection parameters.
     * 
     * @return A new Connection object connected to the RabbitMQ server
     * @throws IOException If there is a network problem during connection establishment
     * @throws TimeoutException If the connection times out during establishment
     */
    public static Connection createConnection() throws IOException, TimeoutException {
        logger.info("Creating new RabbitMQ connection to {}:{}", HOST, PORT);
        
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
        
        Connection connection = factory.newConnection();
        logger.info("Successfully established connection to RabbitMQ server");
        return connection;
    }
}
