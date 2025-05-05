#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Consumer module for receiving messages from RabbitMQ
"""

import pika
import logging
from rabbitmq_client import RabbitMQClient

# Configure logging
logger = logging.getLogger(__name__)


class MessageConsumer(RabbitMQClient):
    """Consumer class for receiving messages from RabbitMQ"""
    
    def __init__(self, connection=None, connection_params=None):
        """
        Initialize with either an existing connection or connection parameters
        
        Args:
            connection (pika.BlockingConnection, optional): Existing connection
            connection_params (pika.ConnectionParameters, optional): Connection parameters
        """
        super().__init__(connection_params=connection_params, connection=connection)
        self.active_consumers = {}
    
    def start_consuming(self, queue_name, consumer_tag):
        """
        Starts consuming messages from a specific queue
        
        Args:
            queue_name (str): Name of the queue to consume from
            consumer_tag (str): Identifier for this consumer
        """
        logger.info(f"Starting consumer '{consumer_tag}' for queue: {queue_name}")
        print(f"Starting consumer '{consumer_tag}' for queue: {queue_name}")
        
        # Set prefetch count to 1 to ensure fair dispatch
        with self.channel_operation() as channel:
            channel.basic_qos(prefetch_count=1)
            
            def callback(ch, method, properties, body):
                """Callback function for processing received messages"""
                try:
                    message = body.decode('utf-8')
                    
                    # Extract headers from properties if available
                    headers = properties.headers if properties and hasattr(properties, 'headers') else None
                    
                    # Print message in a formatted way
                    self._print_received_message(consumer_tag, message, method.exchange, method.routing_key, headers)
                    
                    # Acknowledge message receipt
                    ch.basic_ack(delivery_tag=method.delivery_tag)
                    
                    # For better UX, display menu again
                    self._print_menu()
                except Exception as e:
                    logger.error(f"Error processing message: {e}")
                    # Negative acknowledgment to requeue
                    ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)
            
            # Start consuming
            self.active_consumers[consumer_tag] = channel.basic_consume(
                queue=queue_name,
                on_message_callback=callback,
                consumer_tag=consumer_tag,
                auto_ack=False
            )
    
    def _print_received_message(self, consumer_tag, message, exchange, routing_key, headers=None):
        """
        Helper method to print received message in a formatted way
        
        Args:
            consumer_tag (str): The consumer identifier
            message (str): The message content
            exchange (str): The exchange from which the message was received
            routing_key (str): The routing key used
            headers (dict, optional): Headers from the message properties
        """
        print("\n")
        print("##############################################")
        print("##          MENSAGEM RECEBIDA              ##")
        print("##############################################")
        print(f"# Consumer: {consumer_tag}")
        print(f"# Exchange: {exchange}")
        print(f"# Routing Key: {routing_key}")
        print(f"# Mensagem: {message[:100]}..." if len(message) > 100 else f"# Mensagem: {message}")
        if headers:
            print("# Headers:")
            for key, value in headers.items():
                print(f"#   {key}: {value}")
        print("##############################################")
        
        logger.info(f"Received message by consumer '{consumer_tag}' from exchange: {exchange} with routing key: {routing_key}")
        logger.debug(f"Message content: '{message}'")
        logger.debug(f"Headers: {headers}")
    
    def _print_menu(self):
        """Helper method to redisplay the menu after message reception"""
        print("\nSelect exchange type to send message:")
        print("1. Direct Exchange")
        print("2. Fanout Exchange")
        print("3. Topic Exchange")
        print("0. Exit")
        print("Enter your choice: ", end="", flush=True)
    
    def stop_consuming(self, consumer_tag=None):
        """
        Stop consuming from queues
        
        Args:
            consumer_tag (str, optional): Specific consumer to stop. If None, stops all consumers.
        """
        with self.channel_operation() as channel:
            if consumer_tag and consumer_tag in self.active_consumers:
                channel.basic_cancel(consumer_tag=consumer_tag)
                del self.active_consumers[consumer_tag]
                logger.info(f"Stopped consumer: {consumer_tag}")
            elif consumer_tag is None:
                for tag in list(self.active_consumers.keys()):
                    channel.basic_cancel(consumer_tag=tag)
                    del self.active_consumers[tag]
                logger.info("Stopped all consumers")
    
    def run(self):
        """Implementation of abstract method from RabbitMQClient - starts consuming loop"""
        if not self.active_consumers:
            logger.warning("No active consumers to run")
            return
            
        logger.info("Starting to consume messages...")
        try:
            self.channel.start_consuming()
        except KeyboardInterrupt:
            logger.info("Stopping consumers due to keyboard interrupt")
            self.stop_consuming()
        except Exception as e:
            logger.error(f"Error in consume loop: {e}")
            self.stop_consuming()
            raise
