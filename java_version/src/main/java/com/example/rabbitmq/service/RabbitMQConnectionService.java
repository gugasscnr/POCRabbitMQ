package com.example.rabbitmq.service;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Serviço para gerenciar a conexão direta com o RabbitMQ.
 * Este serviço é usado como fallback caso a conexão via Spring AMQP falhe.
 */
@Service
public class RabbitMQConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConnectionService.class);

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.virtual-host}")
    private String virtualHost;

    private Connection connection;

    /**
     * Cria uma conexão com o servidor RabbitMQ.
     *
     * @return Conexão com o RabbitMQ
     * @throws IOException      Se ocorrer um problema de rede
     * @throws TimeoutException Se a conexão expirar
     */
    public Connection getConnection() throws IOException, TimeoutException {
        if (connection == null || !connection.isOpen()) {
            try {
                logger.info("Criando conexão com RabbitMQ em {}:{}", host, port);
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(host);
                factory.setPort(port);
                factory.setUsername(username);
                factory.setPassword(password);
                factory.setVirtualHost(virtualHost);
                
                // Definir tempos limite mais curtos
                factory.setConnectionTimeout(5000);
                factory.setHandshakeTimeout(5000);
                
                connection = factory.newConnection();
                logger.info("Conexão com RabbitMQ estabelecida com sucesso");
            } catch (IOException | TimeoutException e) {
                logger.error("Falha ao conectar com RabbitMQ: {}", e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    /**
     * Verifica se é possível conectar ao RabbitMQ.
     *
     * @return true se a conexão for bem-sucedida, false caso contrário
     */
    public boolean isRabbitMQAvailable() {
        try {
            Connection conn = getConnection();
            return conn != null && conn.isOpen();
        } catch (Exception e) {
            logger.warn("RabbitMQ não está disponível: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Fecha a conexão com o RabbitMQ quando o serviço for destruído.
     */
    @PreDestroy
    public void closeConnection() {
        if (connection != null && connection.isOpen()) {
            try {
                logger.info("Fechando conexão com RabbitMQ");
                connection.close();
                logger.info("Conexão com RabbitMQ fechada com sucesso");
            } catch (IOException e) {
                logger.error("Erro ao fechar conexão com RabbitMQ: {}", e.getMessage());
            }
        }
    }
}
