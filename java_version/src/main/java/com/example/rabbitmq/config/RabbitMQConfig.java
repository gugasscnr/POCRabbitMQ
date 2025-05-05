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
    private static final String VIRTUAL_HOST = "/";
    
    // Exchange name - trade inbound (matching Python version)
    public static final String TRADE_EXCHANGE = "ex.trade.standard.corporatebond";
    
    // Queue name
    public static final String QUEUE_NAME = "poc.queue.one";
    
    // Routing key
    public static final String ROUTING_KEY = "poc.key.one";
    
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
        factory.setVirtualHost(VIRTUAL_HOST);
        
        Connection connection = factory.newConnection();
        logger.info("Successfully established connection to RabbitMQ server");
        return connection;
    }
}
