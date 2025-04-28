#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Producer module for sending messages to RabbitMQ
"""

import pika
import logging
from rabbitmq_client import RabbitMQClient

# Configure logging
logger = logging.getLogger(__name__)


class MessageProducer(RabbitMQClient):
    """Producer class for sending messages to RabbitMQ"""
    
    def __init__(self, connection=None, connection_params=None):
        """
        Initialize with either an existing connection or connection parameters
        
        Args:
            connection (pika.BlockingConnection, optional): Existing connection
            connection_params (pika.ConnectionParameters, optional): Connection parameters
        """
        super().__init__(connection_params=connection_params, connection=connection)
    
    def declare_direct_exchange(self, exchange_name):
        """
        Declares a direct exchange
        
        Args:
            exchange_name (str): Name of the direct exchange
        """
        self.declare_exchange(exchange_name, 'direct')
        
    def declare_fanout_exchange(self, exchange_name):
        """
        Declares a fanout exchange
        
        Args:
            exchange_name (str): Name of the fanout exchange
        """
        self.declare_exchange(exchange_name, 'fanout')
        
    def declare_topic_exchange(self, exchange_name):
        """
        Declares a topic exchange
        
        Args:
            exchange_name (str): Name of the topic exchange
        """
        self.declare_exchange(exchange_name, 'topic')
        
    def declare_queue(self, queue_name):
        """Declares a queue"""
        self.channel.queue_declare(
            queue=queue_name,
            durable=True
        )
        
    def bind_queue(self, queue_name, exchange_name, routing_key):
        """Binds a queue to an exchange with a routing key"""
        self.channel.queue_bind(
            queue=queue_name,
            exchange=exchange_name,
            routing_key=routing_key
        )
        
    def send_message(self, exchange, routing_key, message):
        """
        Sends a message to a specific exchange with a routing key
        
        Args:
            exchange (str): Exchange name
            routing_key (str): Routing key
            message (str): Message content
        """
        try:
            with self.channel_operation() as channel:
                channel.basic_publish(
                    exchange=exchange,
                    routing_key=routing_key,
                    body=message.encode('utf-8'),
                    properties=pika.BasicProperties(
                        delivery_mode=2  # make message persistent
                    )
                )
            logger.info(f"Sent message: '{message}' to exchange: {exchange} with routing key: {routing_key}")
            print(f"Sent message: '{message}' to exchange: {exchange} with routing key: {routing_key}")
        except Exception as e:
            logger.error(f"Failed to send message: {e}")
            raise
            
    def close(self):
        """Closes the channel"""
        if self.channel.is_open:
            self.channel.close()
            
    def run(self):
        """Implementation of abstract method from RabbitMQClient"""
        logger.info("Producer ready to send messages")
