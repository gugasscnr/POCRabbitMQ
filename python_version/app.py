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
from datetime import datetime, timezone
from contextlib import contextmanager
import config
from producer import MessageProducer
import uuid

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
        virtual_host=config.VIRTUAL_HOST,
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
    trade_id = random.randint(100000000000, 999999999999)
    
    # Gerar um UUID para o eventId
    event_id = str(uuid.uuid4())
    
    # Timestamp atual em formato ISO 8601 UTC (Zulu)
    update_time = datetime.now(timezone.utc).strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3] + 'Z'
    
    # Data atual para eventDate em formato UTC
    event_date = datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]
    
    # Data de referência para eventReferenceDate
    event_reference_date = datetime.now(timezone.utc).strftime('%Y-%m-%d')
    
    # Backoffice status
    backoffice_status = "test.qa.123"
    
    # Mensagem em formato JSON baseada no exemplo fornecido
    message_body = {
        "pnl": {
            "party": {
                "book": {"name": "CUSTODY", "reference": "313"}, 
                "operator": {"name": "CUSTODY", "reference": "12234"}, 
                "strategy": {"name": "CUSTODY", "reference": "31551"}
            }, 
            "counterparty": {
                "book": {"name": "Private", "reference": "186"}, 
                "operator": {"name": "PRIVATE", "reference": "1698"}, 
                "strategy": {"name": "PRIVATE BRAZIL", "reference": "29980"}
            }
        }, 
        "rate": {"post": 75.0, "fixed": 0.0, "index": "DI"}, 
        "tags": ["Avista", "Comercial"], 
        "type": "Unblock", 
        "asset": {
            "rate": {"post": 100.0, "fixed": 0.0, "index": "IGPM"}, 
            "type": "Bond", 
            "issuer": {
                "cge": 113121, 
                "name": "PLANETA SECURITIZADORA SA", 
                "document": "07587384000130"
            }, 
            "subType": "CRI", 
            "clearing": "CETIP", 
            "codAtivo": 550954, 
            "fullName": "12F0036335 13/01/2033", 
            "exception": True, 
            "issueDate": "2012-06-14", 
            "shortName": "12F0036335", 
            "maturityDate": "2033-01-13", 
            "referenceType": "ISIN", 
            "referenceValue": "BRGAIACRI261", 
            "accountingGroup": "CRI", 
            "rateResetFrequency": {"value": 1, "unit": "Month"},
            "calculationConvention": "buss/252",
            "earlyTerminationCondition": {
                "rate": {"post": 75.0, "fixed": 0.0, "index": "DI"}, 
                "custody": {
                    "name": "BANCO BTG PACTUAL S.A.", 
                    "type": "PJ", 
                    "agency": "1", 
                    "isFund": False, 
                    "account": "000009300", 
                    "foreing": False, 
                    "document": "30306294000145", 
                    "cashImpact": False, 
                    "accountType": "CC", 
                    "isPortfolio": False, 
                    "clearingAccount": "72080003", 
                    "internalCustody": True, 
                    "isOmnibusAccount": False, 
                    "isBtgConglomerate": True
                }, 
                "position": 1000, 
                "reference": "POSITION", 
                "unitaryPrice": 765.66035715, 
                "cashSettlement": 765660.36, 
                "netCashSettlement": 765660.36
            }
        }, 
        "quote": {
            "id": "1694792300", 
            "priority": 1, 
            "creationDateTime": update_time,  
            "lastCheckDateTime": update_time
        }, 
        "source": "Asset", 
        "status": "Matched", 
        "tradeId": str(trade_id),
        "version": 1, 
        "custody": {
            "name": "BANCO BTG PACTUAL S.A.", 
            "type": "PJ", 
            "agency": "1", 
            "isFund": False, 
            "account": "000009300", 
            "foreing": False, 
            "document": "30306294000145", 
            "cashImpact": False, 
            "accountType": "CC", 
            "isPortfolio": False, 
            "clearingAccount": "72080003", 
            "internalCustody": True, 
            "isOmnibusAccount": False, 
            "isBtgConglomerate": True
        }, 
        "extendedType": "BLOQUEIO_JUDICIAL", 
        "unitaryPrice": 765.66035715, 
        "objUpdateTime": update_time,
        "cashSettlement": 42876.98, 
        "backofficeStatus": backoffice_status,
        "creationDateTime": update_time, 
        "referencePerAcquisition": [
            {
                "buyDate": datetime.now(timezone.utc).strftime('%Y-%m-%d'), 
                "buyRate": {"post": 75.0, "fixed": 0.0, "index": "DI"}, 
                "quantity": 1000, 
                "reference": "100502982", 
                "originType": "Buy", 
                "enteredDate": update_time, 
                "cashSettlement": 42876.98, 
                "buyUnitaryPrice": 765.66035715, 
                "netCashSettlement": 42876.98
            }
        ]
    }
    
    # Headers customizados conforme a tabela
    headers = {
        # Headers de atributos conforme a imagem
        "event.layout": "panorama-trade-v1",
        "objectType": "position_trade",
        "productType": "emissao_emissao1_efic",
        "account": "00009300",
        "accountingGroup": "CDB",
        "tradeType": "buy",
        "eventType": backoffice_status,
        "eventId": event_id,
        "correlationId": f"custody-engine-{event_reference_date}",
        "eventDate": event_date,
        "eventReferenceDate": event_reference_date,
        
        # Headers adicionais para processamento
        "content-type": "application/json",
        "app-id": "python-rabbitmq-poc",
        "timestamp": update_time,
        "x-trade-id": str(trade_id),
        "x-backoffice-status": backoffice_status
    }
    
    # Converter para string JSON
    message = json.dumps(message_body)
    
    try:
        logger.info(f"Enviando mensagem para exchange '{exchange}' com routing key '{routing_key}'")
        logger.debug(f"Trade ID: {trade_id}")
        logger.debug(f"Event ID: {event_id}")
        logger.debug(f"Object Update Time: {update_time}")
        logger.debug(f"Backoffice Status: {backoffice_status}")
        
        producer.send_message(exchange, routing_key, message, headers)
        
        logger.info("Mensagem enviada com sucesso")
    except Exception as e:
        logger.error(f"Erro ao enviar mensagem: {e}")
        raise


def main():
    """Função principal para executar o envio de mensagem para RabbitMQ"""
    try:
        # Definir variáveis para exchange, routing key e queue
        exchange = config.TRADE_EXCHANGE
        routing_key = config.ROUTING_KEY
        queue = config.QUEUE_NAME
        
        logger.info(f"Usando exchange: {exchange}")
        logger.info(f"Usando routing key: {routing_key}")
        logger.info(f"Enviando para queue: {queue}")
        
        with rabbitmq_connection() as connection:
            # Inicializa o produtor
            producer = MessageProducer(connection=connection)
            
            # Declara a exchange como tipo topic
            producer.declare_exchange(exchange, exchange_type='topic')
            
            # Declara a fila
            producer.declare_queue(queue)
            
            # Vincula a fila à exchange com a routing key
            producer.bind_queue(queue, exchange, routing_key)
            
            logger.info("Exchange, queue e binding configurados com sucesso")
            
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
