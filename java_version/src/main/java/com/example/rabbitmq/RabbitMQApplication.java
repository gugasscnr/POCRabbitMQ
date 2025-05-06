package com.example.rabbitmq;

import com.example.rabbitmq.config.RabbitMQConfig;
import com.example.rabbitmq.consumer.MessageConsumer;
import com.example.rabbitmq.producer.MessageProducer;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.Random;

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
            // Generate trade ID (random 12-digit number)
            String tradeId = generateTradeId();
            
            // Generate UUID for event ID
            String eventId = UUID.randomUUID().toString();
            
            // Generate timestamps and dates
            String timestamp = generateIsoTimestamp();
            String eventDate = generateEventDate();
            String eventReferenceDate = generateReferenceDate();
            
            // Backoffice status
            String backofficeStatus = "test.qa.123";
            
            // Create message content
            String message = createMessageContent(tradeId, timestamp, backofficeStatus);
            
            // Create headers
            Map<String, Object> headers = createHeaders(eventId, timestamp, eventDate, eventReferenceDate, tradeId, backofficeStatus);
            
            // Log details
            logger.debug("Trade ID: {}", tradeId);
            logger.debug("Event ID: {}", eventId);
            logger.debug("Timestamp: {}", timestamp);
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
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return now.format(formatter);
    }
    
    /**
     * Generates an event date string in UTC.
     *
     * @return The formatted event date string
     */
    private static String generateEventDate() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return now.format(formatter);
    }
    
    /**
     * Generates a reference date string in UTC.
     *
     * @return The formatted reference date string
     */
    private static String generateReferenceDate() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return now.format(formatter);
    }
    
    /**
     * Creates the message content in JSON format matching the Python version's structure.
     *
     * @param tradeId The trade ID
     * @param timestamp The timestamp
     * @param backofficeStatus The backoffice status
     * @return A JSON string representing the message
     */
    private static String createMessageContent(String tradeId, String timestamp, String backofficeStatus) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String currentDate = now.format(dateFmt);
        
        return "{\n" +
               "  \"pnl\": {\n" +
               "    \"party\": {\n" +
               "      \"book\": {\"name\": \"CUSTODY\", \"reference\": \"313\"}, \n" +
               "      \"operator\": {\"name\": \"CUSTODY\", \"reference\": \"12234\"}, \n" +
               "      \"strategy\": {\"name\": \"CUSTODY\", \"reference\": \"31551\"}\n" +
               "    }, \n" +
               "    \"counterparty\": {\n" +
               "      \"book\": {\"name\": \"Private\", \"reference\": \"186\"}, \n" +
               "      \"operator\": {\"name\": \"PRIVATE\", \"reference\": \"1698\"}, \n" +
               "      \"strategy\": {\"name\": \"PRIVATE BRAZIL\", \"reference\": \"29980\"}\n" +
               "    }\n" +
               "  }, \n" +
               "  \"rate\": {\"post\": 75.0, \"fixed\": 0.0, \"index\": \"DI\"}, \n" +
               "  \"tags\": [\"Avista\", \"Comercial\"], \n" +
               "  \"type\": \"Unblock\", \n" +
               "  \"asset\": {\n" +
               "    \"rate\": {\"post\": 100.0, \"fixed\": 0.0, \"index\": \"IGPM\"}, \n" +
               "    \"type\": \"Bond\", \n" +
               "    \"issuer\": {\n" +
               "      \"cge\": 113121, \n" +
               "      \"name\": \"PLANETA SECURITIZADORA SA\", \n" +
               "      \"document\": \"07587384000130\"\n" +
               "    }, \n" +
               "    \"subType\": \"CRI\", \n" +
               "    \"clearing\": \"CETIP\", \n" +
               "    \"codAtivo\": 550954, \n" +
               "    \"fullName\": \"12F0036335 13/01/2033\", \n" +
               "    \"exception\": true, \n" +
               "    \"issueDate\": \"2012-06-14\", \n" +
               "    \"shortName\": \"12F0036335\", \n" +
               "    \"maturityDate\": \"2033-01-13\", \n" +
               "    \"referenceType\": \"ISIN\", \n" +
               "    \"referenceValue\": \"BRGAIACRI261\", \n" +
               "    \"accountingGroup\": \"CRI\", \n" +
               "    \"rateResetFrequency\": {\"value\": 1, \"unit\": \"Month\"}, \n" +
               "    \"earlyTerminationCondition\": {\n" +
               "      \"rate\": {\"post\": 75.0, \"fixed\": 0.0, \"index\": \"DI\"}, \n" +
               "      \"initialDate\": \"2025-01-08\"\n" +
               "    }, \n" +
               "    \"internalCustody\": true, \n" +
               "    \"isOmnibusAccount\": false, \n" +
               "    \"isBtgConglomerate\": true\n" +
               "  }, \n" +
               "  \"party\": {\n" +
               "    \"cge\": \"123456789\", \n" +
               "    \"bank\": \"208\", \n" +
               "    \"ispb\": \"30306294\", \n" +
               "    \"name\": \"CLIENTE TESTE\", \n" +
               "    \"type\": \"PF\", \n" +
               "    \"agency\": \"1\", \n" +
               "    \"isFund\": false, \n" +
               "    \"account\": \"123456789\", \n" +
               "    \"foreing\": false, \n" +
               "    \"document\": \"123456789\", \n" +
               "    \"cashImpact\": true, \n" +
               "    \"accountType\": \"CC\", \n" +
               "    \"isPortfolio\": false, \n" +
               "    \"clearingAccount\": \"72080106\", \n" +
               "    \"internalCustody\": true, \n" +
               "    \"isOmnibusAccount\": true, \n" +
               "    \"isBtgConglomerate\": false\n" +
               "  }, \n" +
               "  \"origin\": {\n" +
               "    \"product\": \"estrategico\", \n" +
               "    \"organization\": \"btgpactual\", \n" +
               "    \"custodyEngine\": \"panorama-esg\"\n" +
               "  }, \n" +
               "  \"trader\": \"FronTradeSystem_TrocaLastro\", \n" +
               "  \"forward\": false, \n" +
               "  \"tradeId\": \"" + tradeId + "\", \n" +
               "  \"currency\": \"BRL\", \n" +
               "  \"quantity\": 50, \n" +
               "  \"tradeDate\": \"" + currentDate + "\", \n" +
               "  \"attributes\": {\n" +
               "    \"isRepo\": true, \n" +
               "    \"testKey\": \"7980227644969\"\n" +
               "  }, \n" +
               "  \"settleDate\": \"" + currentDate + "\", \n" +
               "  \"enteredUser\": \"FronTradeSystem_TrocaLastro\", \n" +
               "  \"externalIds\": {\n" +
               "    \"fts\": \"7541772882808\", \n" +
               "    \"blockMatchKey\": \"chave_de_bloqueio_2\"\n" +
               "  }, \n" +
               "  \"tradeStatus\": \"POSITION_BUILDER\", \n" +
               "  \"counterparty\": {\n" +
               "    \"cge\": 1, \n" +
               "    \"bank\": \"208\", \n" +
               "    \"ispb\": \"30306294\", \n" +
               "    \"name\": \"BANCO BTG PACTUAL S.A.\", \n" +
               "    \"type\": \"PJ\", \n" +
               "    \"agency\": \"1\", \n" +
               "    \"isFund\": false, \n" +
               "    \"account\": \"000009300\", \n" +
               "    \"foreing\": false, \n" +
               "    \"document\": \"30306294000145\", \n" +
               "    \"cashImpact\": false, \n" +
               "    \"accountType\": \"CC\", \n" +
               "    \"isPortfolio\": false, \n" +
               "    \"clearingAccount\": \"72080003\", \n" +
               "    \"internalCustody\": true, \n" +
               "    \"isOmnibusAccount\": false, \n" +
               "    \"isBtgConglomerate\": true\n" +
               "  }, \n" +
               "  \"extendedType\": \"BLOQUEIO_JUDICIAL\", \n" +
               "  \"unitaryPrice\": 765.66035715, \n" +
               "  \"objUpdateTime\": \"" + timestamp + "\", \n" +
               "  \"cashSettlement\": 42876.98, \n" +
               "  \"backofficeStatus\": \"" + backofficeStatus + "\", \n" +
               "  \"creationDateTime\": \"" + timestamp + "\", \n" +
               "  \"referencePerAcquisition\": [\n" +
               "    {\n" +
               "      \"buyDate\": \"" + currentDate + "\", \n" +
               "      \"buyRate\": {\"post\": 75.0, \"fixed\": 0.0, \"index\": \"DI\"}, \n" +
               "      \"quantity\": 1000, \n" +
               "      \"reference\": \"100502982\", \n" +
               "      \"originType\": \"Buy\", \n" +
               "      \"enteredDate\": \"" + timestamp + "\", \n" +
               "      \"cashSettlement\": 42876.98, \n" +
               "      \"buyUnitaryPrice\": 765.66035715, \n" +
               "      \"netCashSettlement\": 42876.98\n" +
               "    }\n" +
               "  ]\n" +
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
