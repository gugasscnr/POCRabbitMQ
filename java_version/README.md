# API REST RabbitMQ - Versão Java

Esta é uma API REST em Java para comunicação com o RabbitMQ. A API permite enviar mensagens para diferentes exchanges com routing keys personalizadas, configurar filas e exchanges, além de verificar a saúde da conexão.

## Estrutura do Projeto

- **RabbitMQApplication**: Classe principal de inicialização do Spring Boot
- **RabbitMQConfiguration**: Configuração para conexão com o servidor RabbitMQ
- **RabbitMQService**: Serviço para operações com o RabbitMQ (envio de mensagens, criação de filas, etc.)
- **RabbitMQController**: Controlador REST para exposição dos endpoints da API
- **Modelos**: Classes de modelo para request/response da API

## Funcionalidades da API

1. **Envio de Mensagens**:
   - Envio de mensagens para exchanges com routing keys e cabeçalhos personalizados
   - Suporte a diferentes tipos de mensagens JSON

2. **Configuração de Recursos**:
   - Criação e configuração de exchanges (direct, fanout, topic, headers)
   - Criação e configuração de filas com parâmetros personalizáveis
   - Associação de filas a exchanges com routing keys específicas

3. **Monitoramento**:
   - Verificação da existência de filas e exchanges
   - Verificação da saúde da conexão com o RabbitMQ

## Pré-requisitos

- Java 11 ou superior
- Maven
- RabbitMQ Server rodando localmente (padrão: localhost:5672)

## Como Executar

### Usando Maven

1. Compile e execute o projeto com Spring Boot:
   ```
   mvn spring-boot:run
   ```

   Ou compile e execute separadamente:
   ```
   mvn clean package
   java -jar target/rabbitmq-poc-1.0-SNAPSHOT.jar
   ```

2. A API estará disponível em: http://localhost:8080/
   
3. Acesse a documentação da API em: http://localhost:8080/api-doc.html

## Como Usar a API

### 1. Enviando Mensagens

**Endpoint**: `POST /api/rabbitmq/message`

**Exemplo de requisição**:
```json
{
  "exchange": "ex.trade.standard.corporatebond",
  "routingKey": "poc.key.one",
  "body": {
    "id": "1234567890123",
    "tradeType": "BUY",
    "message": "Teste de mensagem"
  },
  "headers": {
    "event.layout": "panorama-trade-v1",
    "objectType": "position_trade",
    "content-type": "application/json"
  }
}
```

### 2. Configurando uma Fila e Exchange

**Endpoint**: `POST /api/rabbitmq/queue`

**Exemplo de requisição**:
```json
{
  "queueName": "poc.queue.one",
  "exchangeName": "ex.trade.standard.corporatebond",
  "exchangeType": "topic",
  "routingKey": "poc.key.one",
  "durable": true,
  "exclusive": false,
  "autoDelete": false,
  "arguments": {
    "x-dead-letter-exchange": ""
  }
}
```

### 3. Verificando a Existência de uma Exchange

**Endpoint**: `GET /api/rabbitmq/exchange/{name}`

### 4. Verificando a Existência de uma Fila

**Endpoint**: `GET /api/rabbitmq/queue/{name}`

### 5. Verificando a Saúde da Conexão

**Endpoint**: `GET /api/rabbitmq/health`

## Observações

- A API usa Spring Boot com RestControllers para exposição dos endpoints
- O serviço utiliza a biblioteca oficial do RabbitMQ para Java
- As conexões e canais são gerenciados de forma apropriada
- A API inclui tratamento de erros e validação de entradas
- Os endpoints são documentados e podem ser testados via ferramentas como Postman ou cURL
- Para uso em produção, considere adicionar autenticação e mecanismos de rate limiting
