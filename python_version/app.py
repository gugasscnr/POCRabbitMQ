#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Aplicação para enviar mensagens para RabbitMQ com headers customizados
"""

import pika
import time
import sys
import logging
import json
import random
from datetime import datetime
from contextlib import contextmanager
import config
from producer import MessageProducer

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


def create_connection_params():
    """Creates and returns connection parameters for RabbitMQ server"""
    credentials = pika.PlainCredentials(config.USERNAME, config.PASSWORD)
    return pika.ConnectionParameters(
        host=config.HOST,
        port=config.PORT,
        credentials=credentials
    )


@contextmanager
def rabbitmq_connection():
    """Context manager for RabbitMQ connection to ensure proper cleanup"""
    connection = None
    try:
        # Create connection
        connection_params = create_connection_params()
        connection = pika.BlockingConnection(connection_params)
        logger.info("Connected to RabbitMQ successfully")
        yield connection
    except pika.exceptions.AMQPConnectionError as e:
        logger.error(f"Failed to connect to RabbitMQ: {e}")
        raise
    finally:
        # Ensure connection is closed
        if connection and connection.is_open:
            connection.close()
            logger.info("RabbitMQ connection closed")


def send_message_with_headers(producer, exchange, routing_key, queue):
    """
    Envia uma mensagem com headers customizados para o RabbitMQ
    
    Args:
        producer (MessageProducer): Instância do produtor
        exchange (str): Nome da exchange
        routing_key (str): Routing key
        queue (str): Nome da fila
    """
    # Gerar um trade ID (número inteiro aleatório)
    trade_id = random.randint(100000, 999999)
    
    # Timestamp atual em formato ISO 8601 UTC (Zulu)
    update_time = datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%S.%fZ')
    
    # Backoffice status
    backoffice_status = "test.qa.123"
    
    # Mensagem em formato JSON
    message_body = {
        "trade_id": trade_id,
        "update_time": update_time,
        "backoffice_status": backoffice_status,
        "status": "PROCESSED",
        "message": "Teste de mensagem com cabeçalhos"
    }
    
    # Converter para string JSON
    message = json.dumps(message_body)
    
    # Headers customizados
    headers = {
        "content-type": "application/json",
        "app-id": "python-rabbitmq-poc",
        "correlation-id": f"test-{trade_id}",
        "timestamp": update_time,
        "x-trade-id": str(trade_id),
        "x-backoffice-status": backoffice_status
    }
    
    try:
        logger.info(f"Enviando mensagem para exchange '{exchange}' com routing key '{routing_key}'")
        logger.info(f"Trade ID: {trade_id}")
        logger.info(f"Update Time: {update_time}")
        logger.info(f"Backoffice Status: {backoffice_status}")
        
        producer.send_message(exchange, routing_key, message, headers)
        
        logger.info("Mensagem enviada com sucesso")
    except Exception as e:
        logger.error(f"Erro ao enviar mensagem: {e}")
        raise


def main():
    """Função principal para executar o envio de mensagem para RabbitMQ"""
    try:
        # Definir variáveis para exchange, routing key e queue
        exchange = config.DIRECT_EXCHANGE
        routing_key = config.ROUTING_KEY_1
        queue = config.QUEUE_1
        
        logger.info(f"Usando exchange: {exchange}")
        logger.info(f"Usando routing key: {routing_key}")
        logger.info(f"Enviando para queue: {queue}")
        
        with rabbitmq_connection() as connection:
            # Inicializa o produtor
            producer = MessageProducer(connection=connection)
            
            # Envia a mensagem com headers
            send_message_with_headers(producer, exchange, routing_key, queue)
            
            logger.info("Aplicação finalizada com sucesso")
            print("\nMensagem enviada com sucesso. Aplicação finalizada.")
    except KeyboardInterrupt:
        logger.info("Aplicação terminada pelo usuário")
        print("\nAplicação terminada pelo usuário")
    except Exception as e:
        logger.error(f"Erro na aplicação RabbitMQ: {e}")
        print(f"Erro na aplicação RabbitMQ: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
