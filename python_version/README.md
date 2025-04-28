# RabbitMQ Python POC

Esta é uma implementação em Python da POC de RabbitMQ, demonstrando o uso de diferentes tipos de exchanges (Direct, Fanout, Topic) e filas.

## Requisitos

- Python 3.6+
- RabbitMQ Server em execução (localhost:5672)
- Biblioteca pika

## Instalação

1. Certifique-se de que o RabbitMQ está em execução no seu ambiente.
2. Instale as dependências necessárias:

```bash
pip install -r requirements.txt
```

## Estrutura do Projeto

- `config.py` - Configurações de conexão e definições de exchanges/filas/routing keys
- `producer.py` - Implementação do produtor de mensagens
- `consumer.py` - Implementação do consumidor de mensagens
- `app.py` - Aplicação principal que configura e demonstra o uso do RabbitMQ

## Como Executar

Execute a aplicação principal:

```bash
python app.py
```

A aplicação irá:
1. Conectar ao servidor RabbitMQ
2. Configurar exchanges (Direct, Fanout, Topic) e filas
3. Iniciar consumidores para cada fila
4. Apresentar um menu interativo para enviar mensagens a diferentes exchanges

## Tipos de Exchanges

### Direct Exchange
- As mensagens são roteadas para filas com base em uma combinação exata da routing key
- Exemplo: `poc.direct.exchange` com routing keys `poc.key.one` e `poc.key.two`

### Fanout Exchange
- As mensagens são enviadas para todas as filas vinculadas, ignorando a routing key
- Exemplo: `poc.fanout.exchange`

### Topic Exchange
- As mensagens são roteadas para filas com base em padrões de routing key
- Padrão: `poc.topic.#` (onde # pode ser qualquer sequência de palavras)
- Exemplo: enviar mensagem com routing key `poc.topic.test` será recebida pela fila 3

## Diferenças em Relação à Implementação Java

Esta implementação em Python segue os mesmos conceitos e estruturas da versão Java original, porém utilizando:

1. A biblioteca `pika` - cliente oficial Python para RabbitMQ
2. Estruturas de dados e convenções de nomenclatura típicas de Python (snake_case)
3. Implementação adaptada para o modelo de comunicação do pika

A funcionalidade e o comportamento são equivalentes à versão Java.
