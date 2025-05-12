package com.example.rabbitmq.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Serviço simplificado para enviar mensagens ao RabbitMQ.
 * Focado apenas na funcionalidade de geração de mensagens com headers customizados.
 */
@Service
public class RabbitMQService {
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQService.class);
    
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConnectionService connectionService;
    
    /**
     * Construtor que injeta as dependências necessárias.
     * 
     * @param rabbitTemplate Template do Spring AMQP para RabbitMQ
     * @param connectionService Serviço de conexão com o RabbitMQ
     */
    @Autowired
    public RabbitMQService(RabbitTemplate rabbitTemplate, RabbitMQConnectionService connectionService) {
        this.rabbitTemplate = rabbitTemplate;
        this.connectionService = connectionService;
        logger.info("Serviço de RabbitMQ inicializado com sucesso");
    }
    
    /**
     * Envia uma mensagem para uma exchange com uma routing key e headers customizados.
     * 
     * @param exchange Nome da exchange
     * @param routingKey Routing key para roteamento da mensagem
     * @param message Conteúdo da mensagem
     * @param headers Cabeçalhos da mensagem
     * @return true se a mensagem foi enviada com sucesso
     */
    public boolean sendMessage(String exchange, String routingKey, String message, Map<String, Object> headers) {
        try {
            logger.info("Enviando mensagem para exchange: {} com routing key: {}", exchange, routingKey);
            logger.debug("Conteúdo da mensagem: '{}'", message);
            logger.debug("Headers: {}", headers);
            
            // Cria as propriedades da mensagem
            MessageProperties properties = new MessageProperties();
            
            // Adiciona os headers se existirem
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(properties::setHeader);
            }
            
            // Cria a mensagem com o conteúdo e propriedades
            Message messageObject = new Message(message.getBytes(StandardCharsets.UTF_8), properties);
            
            // Envia a mensagem usando o template
            rabbitTemplate.send(exchange, routingKey, messageObject);
            
            logger.info("Mensagem enviada com sucesso");
            return true;
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem: {}", e.getMessage(), e);
            return false;
        }
    }
}
