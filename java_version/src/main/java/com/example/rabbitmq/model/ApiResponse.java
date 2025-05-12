package com.example.rabbitmq.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Modelo para respostas padrão da API.
 * Utilizado para retornar resultados padronizados das operações.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * Status da operação (true = sucesso, false = falha).
     */
    private boolean success;
    
    /**
     * Mensagem descritiva sobre o resultado da operação.
     */
    private String message;
    
    /**
     * Timestamp da resposta.
     */
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Dados retornados pela operação, se houver.
     */
    private T data;
    
    /**
     * Cria uma resposta de sucesso com mensagem.
     * 
     * @param message Mensagem de sucesso
     * @return Objeto ApiResponse configurado para sucesso
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, LocalDateTime.now(), null);
    }
    
    /**
     * Cria uma resposta de sucesso com mensagem e dados.
     * 
     * @param message Mensagem de sucesso
     * @param data Dados a serem retornados
     * @return Objeto ApiResponse configurado para sucesso com dados
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, LocalDateTime.now(), data);
    }
    
    /**
     * Cria uma resposta de erro com mensagem.
     * 
     * @param message Mensagem de erro
     * @return Objeto ApiResponse configurado para erro
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, LocalDateTime.now(), null);
    }
}
