package com.example.rabbitmq.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Modelo para requisição de envio de mensagem para o RabbitMQ.
 * Contém todos os detalhes necessários para enviar uma mensagem.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RabbitMQMessageRequest {
    
    /**
     * Nome da exchange para enviar a mensagem.
     */
    @NotBlank(message = "O nome da exchange é obrigatório")
    private String exchange;
    
    /**
     * Routing key para direcionar a mensagem para a fila correta.
     */
    @NotBlank(message = "A routing key é obrigatória")
    private String routingKey;
    
    /**
     * Conteúdo da mensagem a ser enviada.
     * Pode ser um objeto JSON ou uma string.
     */
    @NotNull(message = "O conteúdo da mensagem é obrigatório")
    private Object body;
    
    /**
     * Cabeçalhos a serem incluídos na mensagem.
     * Podem conter informações adicionais sobre a mensagem.
     */
    private Map<String, Object> headers;
}
