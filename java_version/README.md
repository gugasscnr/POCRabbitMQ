# RabbitMQ POC - Versão Java

Esta é a versão em Java da Prova de Conceito (POC) que demonstra o uso do RabbitMQ, incluindo diferentes tipos de exchanges, filas e routing keys.

## Estrutura do Projeto

- **RabbitMQConfig**: Classe de configuração com constantes para exchanges, filas e routing keys
- **MessageProducer**: Responsável por enviar mensagens para o RabbitMQ
- **MessageConsumer**: Responsável por consumir mensagens das filas
- **RabbitMQApplication**: Aplicação principal que demonstra o funcionamento da POC

## Conceitos Demonstrados

1. **Exchanges**:
   - Direct Exchange: Roteia mensagens para filas baseado no routing key exato
   - Fanout Exchange: Entrega mensagens para todas as filas vinculadas, ignorando routing keys
   - Topic Exchange: Roteia mensagens baseado em padrões de routing keys usando wildcards

2. **Queues**: Três filas diferentes são criadas para demonstrar diferentes padrões de consumo

3. **Routing Keys**: Usadas para direcionar mensagens do exchange para as filas apropriadas

## Pré-requisitos

- Java 11 ou superior
- Maven (opcional)
- RabbitMQ Server rodando localmente (padrão: localhost:5672)

## Como Executar

### Usando Maven

Se você tem o Maven instalado:

1. Compile o projeto:
   ```
   mvn clean package
   ```

2. Execute a aplicação:
   ```
   java -jar target/rabbitmq-poc-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

### Compilação Manual (sem Maven)

1. Crie a pasta para os arquivos compilados:
   ```
   mkdir -p target/classes
   ```

2. Compile as classes:
   ```
   javac -d target\classes -cp "path\to\amqp-client-5.16.0.jar;path\to\slf4j-api-2.0.5.jar;path\to\slf4j-simple-2.0.5.jar" src\main\java\com\example\rabbitmq\config\*.java src\main\java\com\example\rabbitmq\consumer\*.java src\main\java\com\example\rabbitmq\producer\*.java src\main\java\com\example\rabbitmq\*.java
   ```

3. Execute a aplicação:
   ```
   java -cp "target\classes;path\to\amqp-client-5.16.0.jar;path\to\slf4j-api-2.0.5.jar;path\to\slf4j-simple-2.0.5.jar" com.example.rabbitmq.RabbitMQApplication
   ```

## Como Testar

Após iniciar a aplicação, você verá um menu interativo:

1. **Direct Exchange (Opção 1)**:
   - Escolha uma routing key (1 ou 2)
   - Digite uma mensagem
   - A mensagem será enviada apenas para a fila correspondente à routing key escolhida

2. **Fanout Exchange (Opção 2)**:
   - Digite uma mensagem
   - A mensagem será enviada para todas as filas vinculadas ao fanout exchange

3. **Topic Exchange (Opção 3)**:
   - Digite uma routing key no formato 'poc.topic.*' (ex: poc.topic.test)
   - Digite uma mensagem
   - A mensagem será enviada para filas que correspondem ao padrão da routing key

4. **Sair (Opção 0)**:
   - Encerra a aplicação

## Observações

- O código usa conexões e canais do RabbitMQ de forma apropriada
- As mensagens são confirmadas (acknowledged) quando processadas
- A aplicação inclui tratamento de erros básico
- Este é um exemplo didático e pode precisar de ajustes para uso em produção
