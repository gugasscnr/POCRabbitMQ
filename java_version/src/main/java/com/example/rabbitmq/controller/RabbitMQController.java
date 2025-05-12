package com.example.rabbitmq.controller;

import com.example.rabbitmq.model.ApiResponse;
import com.example.rabbitmq.model.RabbitMQMessageRequest;
import com.example.rabbitmq.service.RabbitMQService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;

/**
 * Controlador REST para enviar mensagens ao RabbitMQ.
 * API simples para geração de mensagens com headers customizados.
 */
@RestController
@RequestMapping("/api/rabbitmq")
public class RabbitMQController {
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQController.class);
    
    private final RabbitMQService rabbitMQService;
    
    /**
     * Construtor que injeta o serviço de RabbitMQ.
     * 
     * @param rabbitMQService Serviço para operações com o RabbitMQ
     */
    @Autowired
    public RabbitMQController(RabbitMQService rabbitMQService) {
        this.rabbitMQService = rabbitMQService;
    }
    
    /**
     * Endpoint para enviar mensagens para o RabbitMQ.
     * 
     * @param request Dados da mensagem a ser enviada (exchange, routing key, body, headers)
     * @return Resposta indicando sucesso ou falha na operação
     */
    @PostMapping("/message")
    public ResponseEntity<ApiResponse<Void>> sendMessage(@Valid @RequestBody RabbitMQMessageRequest request) {
        logger.info("Recebida requisição para enviar mensagem para exchange '{}' com routing key '{}'", 
                request.getExchange(), request.getRoutingKey());
        
        try {
            // Converte o corpo da mensagem para string, se necessário
            String messageBody;
            if (request.getBody() instanceof String) {
                messageBody = (String) request.getBody();
            } else {
                messageBody = request.getBody().toString();
            }
            
            // Envia a mensagem usando o serviço
            rabbitMQService.sendMessage(
                request.getExchange(), 
                request.getRoutingKey(), 
                messageBody, 
                request.getHeaders() != null ? request.getHeaders() : new HashMap<>()
            );
            
            // Retorna resposta de sucesso
            return ResponseEntity.ok(ApiResponse.success("Mensagem enviada com sucesso para o RabbitMQ"));
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem para o RabbitMQ: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Erro ao enviar mensagem: " + e.getMessage()));
        }
    }
}
