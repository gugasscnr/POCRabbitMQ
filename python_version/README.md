# RabbitMQ Python POC

Esta é uma implementação em Python para envio de mensagens para RabbitMQ com headers customizados.

## Requisitos

- Python 3.6+
- RabbitMQ Server em execução (localhost:5672)
- Biblioteca pika 1.3.2

## Instalação

1. Certifique-se de que o RabbitMQ está em execução no seu ambiente.
2. Instale as dependências necessárias:

```bash
pip install -r requirements.txt
```

3. Ative o ambiente virtual, se existir:

```bash
# Windows
.\venv\Scripts\activate

# Linux/Mac
source venv/bin/activate
```

## Estrutura do Projeto

- `config.py` - Configurações de conexão e definições de exchanges/filas/routing keys
- `producer.py` - Implementação do produtor de mensagens
- `rabbitmq_client.py` - Classe base para operações com RabbitMQ
- `app.py` - Aplicação principal que envia mensagens com headers para o RabbitMQ

## Como Executar

Execute a aplicação principal:

```bash
python app.py
```

A aplicação irá:
1. Conectar ao servidor RabbitMQ
2. Gerar um novo trade ID aleatório
3. Criar um timestamp no formato UTC Zulu (ISO 8601)
4. Enviar uma mensagem JSON para o RabbitMQ com os seguintes headers:
   - content-type: application/json
   - app-id: python-rabbitmq-poc
   - correlation-id: test-{trade_id}
   - timestamp: {update_time}
   - x-trade-id: {trade_id}
   - x-backoffice-status: test.qa.123
5. Finalizar após o envio bem-sucedido

## Funcionamento

A aplicação utiliza o exchange e routing key definidos no arquivo `config.py` para enviar mensagens para o RabbitMQ. Não é necessário configurar exchanges ou filas, pois assume-se que estas já existem no ambiente.

### Formato da Mensagem

A mensagem enviada tem o seguinte formato JSON:

```json
{
  "trade_id": 123456,
  "update_time": "2025-05-05T11:45:23.456Z",
  "backoffice_status": "test.qa.123",
  "status": "PROCESSED",
  "message": "Teste de mensagem com cabeçalhos"
}
```

### Headers da Mensagem

Os headers enviados com a mensagem incluem:

| Header | Descrição |
|--------|-----------|
| content-type | Tipo de conteúdo da mensagem (application/json) |
| app-id | Identificador da aplicação |
| correlation-id | ID de correlação para rastreamento |
| timestamp | Timestamp UTC no formato ISO 8601 com Z (Zulu) |
| x-trade-id | ID da transação |
| x-backoffice-status | Status do backoffice |

## Customização

Para customizar o comportamento da aplicação, você pode modificar os seguintes arquivos:

- `config.py` - Para alterar parâmetros de conexão, nome do exchange ou routing key
- `app.py` - Para modificar a estrutura da mensagem ou os headers enviados
