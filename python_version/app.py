#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Main application to demonstrate RabbitMQ POC with Python
"""

import pika
import time
import sys
import logging
import threading
from contextlib import contextmanager
import config
from producer import MessageProducer
from consumer import MessageConsumer

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


def setup_exchanges_and_queues(producer):
    """
    Sets up exchanges and queues for the POC
    
    Args:
        producer (MessageProducer): The producer instance to use
    """
    try:
        # Declare exchanges
        producer.declare_direct_exchange(config.DIRECT_EXCHANGE)
        producer.declare_fanout_exchange(config.FANOUT_EXCHANGE)
        producer.declare_topic_exchange(config.TOPIC_EXCHANGE)
        
        # Declare queues
        producer.declare_queue(config.QUEUE_1)
        producer.declare_queue(config.QUEUE_2)
        producer.declare_queue(config.QUEUE_3)
        
        # Bind queues to exchanges with routing keys
        # Direct exchange bindings
        producer.bind_queue(config.QUEUE_1, config.DIRECT_EXCHANGE, config.ROUTING_KEY_1)
        producer.bind_queue(config.QUEUE_2, config.DIRECT_EXCHANGE, config.ROUTING_KEY_2)
        
        # Fanout exchange bindings (no routing key needed, but empty string is used)
        producer.bind_queue(config.QUEUE_1, config.FANOUT_EXCHANGE, "")
        producer.bind_queue(config.QUEUE_2, config.FANOUT_EXCHANGE, "")
        
        # Topic exchange bindings
        producer.bind_queue(config.QUEUE_3, config.TOPIC_EXCHANGE, config.TOPIC_KEY_PATTERN)
        
        logger.info("Exchanges and queues setup completed")
    except Exception as e:
        logger.error(f"Error setting up exchanges and queues: {e}")
        raise


def start_consumers(connection):
    """
    Starts consumers for each queue
    
    Args:
        connection (pika.BlockingConnection): The connection to use
        
    Returns:
        MessageConsumer: The configured consumer instance
    """
    try:
        consumer = MessageConsumer(connection=connection)
        
        # Start a consumer for each queue
        consumer.start_consuming(config.QUEUE_1, "consumer-1")
        consumer.start_consuming(config.QUEUE_2, "consumer-2")
        consumer.start_consuming(config.QUEUE_3, "consumer-3")
        
        logger.info("Consumers started and ready to receive messages")
        return consumer
    except Exception as e:
        logger.error(f"Error starting consumers: {e}")
        raise


def start_consuming_thread(connection):
    """
    Start a thread to process consumer callbacks
    
    Args:
        connection (pika.BlockingConnection): The connection to use
    """
    def process_data_events():
        try:
            # Process events continuously
            while True:
                connection.process_data_events(time_limit=0.1)
                time.sleep(0.1)  # Short sleep to prevent CPU hogging
        except Exception as e:
            logger.error(f"Error in consumer thread: {e}")
    
    # Start thread
    consumer_thread = threading.Thread(target=process_data_events)
    consumer_thread.daemon = True  # Thread will exit when main thread exits
    consumer_thread.start()
    logger.info("Started thread for processing consumer events")
    return consumer_thread


def send_direct_exchange_message(producer):
    """
    Interactive function to send direct exchange message
    
    Args:
        producer (MessageProducer): The producer to use for sending
    """
    print("\n--- Direct Exchange Message ---")
    print("Select routing key:")
    print(f"1. {config.ROUTING_KEY_1}")
    print(f"2. {config.ROUTING_KEY_2}")
    
    key_choice = input("Enter your choice (1-2): ")
    
    if key_choice == "1":
        routing_key = config.ROUTING_KEY_1
    elif key_choice == "2":
        routing_key = config.ROUTING_KEY_2
    else:
        print("Invalid choice, using default routing key.")
        routing_key = config.ROUTING_KEY_1
    
    message = input("Enter message to send: ")
    
    try:
        producer.send_message(config.DIRECT_EXCHANGE, routing_key, message)
    except Exception as e:
        logger.error(f"Error sending to direct exchange: {e}")
        print(f"Failed to send message: {e}")


def send_fanout_exchange_message(producer):
    """
    Interactive function to send fanout exchange message
    
    Args:
        producer (MessageProducer): The producer to use for sending
    """
    print("\n--- Fanout Exchange Message ---")
    message = input("Enter message to send: ")
    
    try:
        # In fanout exchange, routing key is ignored
        producer.send_message(config.FANOUT_EXCHANGE, "", message)
    except Exception as e:
        logger.error(f"Error sending to fanout exchange: {e}")
        print(f"Failed to send message: {e}")


def send_topic_exchange_message(producer):
    """
    Interactive function to send topic exchange message
    
    Args:
        producer (MessageProducer): The producer to use for sending
    """
    print("\n--- Topic Exchange Message ---")
    routing_key = input("Enter topic routing key (e.g., poc.topic.example): ")
    message = input("Enter message to send: ")
    
    try:
        producer.send_message(config.TOPIC_EXCHANGE, routing_key, message)
    except Exception as e:
        logger.error(f"Error sending to topic exchange: {e}")
        print(f"Failed to send message: {e}")


def send_messages_interactively(producer):
    """
    Interactive console to send messages
    
    Args:
        producer (MessageProducer): The producer to use for sending
    """
    running = True
    
    while running:
        try:
            print("\nSelect exchange type to send message:")
            print("1. Direct Exchange")
            print("2. Fanout Exchange")
            print("3. Topic Exchange")
            print("0. Exit")
            
            choice = input("Enter your choice: ")
            
            if choice == "1":
                send_direct_exchange_message(producer)
            elif choice == "2":
                send_fanout_exchange_message(producer)
            elif choice == "3":
                send_topic_exchange_message(producer)
            elif choice == "0":
                running = False
                logger.info("User requested to exit interactive mode")
            else:
                print("Invalid choice. Try again.")
        except KeyboardInterrupt:
            logger.info("User interrupted the program")
            running = False
        except Exception as e:
            logger.error(f"Error in interactive mode: {e}")
            print(f"An error occurred: {e}")


def main():
    """Main function to run the RabbitMQ POC"""
    try:
        with rabbitmq_connection() as connection:
            # Initialize producer
            producer = MessageProducer(connection=connection)
            
            # Setup exchanges and queues
            setup_exchanges_and_queues(producer)
            
            # Start consumers
            consumer = start_consumers(connection)
            
            # Start consuming in a separate thread
            consumer_thread = start_consuming_thread(connection)
            
            # Interactive message sending
            send_messages_interactively(producer)
            
            print("Application terminated")
    except KeyboardInterrupt:
        logger.info("Application terminated by user")
        print("\nApplication terminated by user")
    except Exception as e:
        logger.error(f"Error in RabbitMQ application: {e}")
        print(f"Error in RabbitMQ application: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
