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
    
    def declare_exchange(self, exchange_name, exchange_type='topic'):
        """
        Declares an exchange with the specified type
        
        Args:
            exchange_name (str): Name of the exchange to declare
            exchange_type (str, optional): Type of exchange to create ('direct', 'topic', 'fanout', 'headers')
                                          Defaults to 'topic'
        """
        try:
            with self.channel_operation() as channel:
                channel.exchange_declare(
                    exchange=exchange_name,
                    exchange_type=exchange_type,
                    durable=True
                )
            logger.info(f"Declared {exchange_type} exchange: {exchange_name}")
        except Exception as e:
            logger.error(f"Failed to declare exchange: {e}")
            raise
    
    def declare_queue(self, queue_name):
        """
        Declares a queue with durability enabled
        
        Args:
            queue_name (str): Name of the queue to declare
        """
        try:
            with self.channel_operation() as channel:
                try:
                    # First, try to check if the queue exists using queue_declare with passive=True
                    # This won't create the queue if it doesn't exist, and won't modify it if it does exist
                    channel.queue_declare(queue=queue_name, passive=True)
                    logger.info(f"Queue already exists, using existing queue: {queue_name}")
                except pika.exceptions.ChannelClosedByBroker:
                    # Queue doesn't exist, need to reopen the channel since it was closed by the broker
                    self._create_channel()
                    
                    with self.channel_operation() as new_channel:
                        # Create the queue with the necessary arguments
                        arguments = {
                            'x-dead-letter-exchange': ''  # Empty string as per the existing queue configuration
                        }
                        new_channel.queue_declare(
                            queue=queue_name,
                            durable=True,
                            arguments=arguments
                        )
                        logger.info(f"Queue doesn't exist, created queue: {queue_name} with dead-letter-exchange")
        except Exception as e:
            logger.error(f"Failed to declare queue: {e}")
            raise
    
    def bind_queue(self, queue_name, exchange_name, routing_key):
        """
        Binds a queue to an exchange with a routing key
        
        Args:
            queue_name (str): Name of the queue to bind
            exchange_name (str): Name of the exchange to bind to
            routing_key (str): Routing key to use for binding
        """
        try:
            with self.channel_operation() as channel:
                channel.queue_bind(
                    queue=queue_name,
                    exchange=exchange_name,
                    routing_key=routing_key
                )
            logger.info(f"Bound queue {queue_name} to exchange {exchange_name} with routing key {routing_key}")
        except Exception as e:
            logger.error(f"Failed to bind queue: {e}")
            raise
    
    def send_message(self, exchange, routing_key, message, headers=None):
        """
        Sends a message to a specific exchange with a routing key
        
        Args:
            exchange (str): Exchange name
            routing_key (str): Routing key
            message (str): Message content
            headers (dict, optional): Dictionary of headers to include with the message
        """
        try:
            with self.channel_operation() as channel:
                # Create basic properties with delivery mode for persistence
                properties = pika.BasicProperties(
                    delivery_mode=2,  # make message persistent
                    headers=headers   # include custom headers if provided
                )
                
                channel.basic_publish(
                    exchange=exchange,
                    routing_key=routing_key,
                    body=message.encode('utf-8'),
                    properties=properties
                )
            logger.info(f"Sent message to exchange: {exchange} with routing key: {routing_key}")
            logger.debug(f"Message content: '{message}'")
            logger.debug(f"Headers: {headers}")
        except Exception as e:
            logger.error(f"Failed to send message: {e}")
            raise
            
    def close(self):
        """Closes the channel"""
        if self.channel and self.channel.is_open:
            self.channel.close()
            
    def run(self):
        """Implementation of abstract method from RabbitMQClient"""
        logger.info("Producer ready to send messages")
