package com.example.rabbitmq.producer;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Producer class for sending messages to RabbitMQ.
 * This class handles all operations related to message publishing including:
 * - Exchange declaration (Direct, Fanout, Topic)
 * - Queue declaration and binding
 * - Message publishing with appropriate properties
 */
public class MessageProducer implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);
    
    private final Connection connection;
    private final Channel channel;
    
    /**
     * Constructor that initializes the connection and channel.
     * 
     * @param connection An existing RabbitMQ connection
     * @throws IOException If a channel cannot be created from the connection
     */
    public MessageProducer(Connection connection) throws IOException {
        this.connection = connection;
        this.channel = connection.createChannel();
        logger.debug("Created MessageProducer with channel number: {}", channel.getChannelNumber());
    }
    
    /**
     * Declares a direct exchange.
     * Direct exchanges route messages to queues based on exact matching 
     * between routing key and binding key.
     * 
     * @param exchangeName The name of the direct exchange to declare
     * @throws IOException If the exchange cannot be declared
     */
    public void declareDirectExchange(String exchangeName) throws IOException {
        logger.info("Declaring direct exchange: {}", exchangeName);
        channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, true);
    }
    
    /**
     * Declares a fanout exchange.
     * Fanout exchanges broadcast messages to all bound queues
     * regardless of routing key.
     * 
     * @param exchangeName The name of the fanout exchange to declare
     * @throws IOException If the exchange cannot be declared
     */
    public void declareFanoutExchange(String exchangeName) throws IOException {
        logger.info("Declaring fanout exchange: {}", exchangeName);
        channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, true);
    }
    
    /**
     * Declares a topic exchange.
     * Topic exchanges route messages to queues based on pattern matching
     * between routing key and binding pattern.
     * 
     * @param exchangeName The name of the topic exchange to declare
     * @throws IOException If the exchange cannot be declared
     */
    public void declareTopicExchange(String exchangeName) throws IOException {
        logger.info("Declaring topic exchange: {}", exchangeName);
        channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC, true);
    }
    
    /**
     * Declares a queue with durability enabled.
     * 
     * @param queueName The name of the queue to declare
     * @throws IOException If the queue cannot be declared
     */
    public void declareQueue(String queueName) throws IOException {
        logger.info("Declaring durable queue: {}", queueName);
        channel.queueDeclare(queueName, true, false, false, null);
    }
    
    /**
     * Binds a queue to an exchange with a routing key.
     * 
     * @param queueName The name of the queue to bind
     * @param exchangeName The name of the exchange to bind to
     * @param routingKey The routing key to use for binding
     * @throws IOException If the binding cannot be established
     */
    public void bindQueue(String queueName, String exchangeName, String routingKey) throws IOException {
        logger.info("Binding queue: {} to exchange: {} with routing key: {}", queueName, exchangeName, routingKey);
        channel.queueBind(queueName, exchangeName, routingKey);
    }
    
    /**
     * Sends a message to a specific exchange with a routing key.
     * The message is made persistent to survive broker restarts.
     * 
     * @param exchange The name of the exchange to publish to
     * @param routingKey The routing key to use for message routing
     * @param message The message content as a string
     * @throws IOException If the message cannot be published
     */
    public void sendMessage(String exchange, String routingKey, String message) throws IOException {
        try {
            channel.basicPublish(
                    exchange,
                    routingKey,
                    null,
                    message.getBytes(StandardCharsets.UTF_8)
            );
            logger.info("Sent message to exchange: {} with routing key: {}", exchange, routingKey);
            logger.debug("Message content: '{}'", message);
            System.out.println("Sent message: '" + message + "' to exchange: " + exchange + " with routing key: " + routingKey);
        } catch (IOException e) {
            logger.error("Failed to send message to exchange: {} with routing key: {}", exchange, routingKey, e);
            throw e;
        }
    }
    
    /**
     * Closes the channel and connection.
     * Implements AutoCloseable interface for use with try-with-resources.
     * 
     * @throws IOException If there's an error closing the channel
     * @throws TimeoutException If there's a timeout closing the connection
     */
    @Override
    public void close() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) {
            logger.debug("Closing channel: {}", channel.getChannelNumber());
            channel.close();
        }
        logger.info("MessageProducer resources closed");
    }
}
