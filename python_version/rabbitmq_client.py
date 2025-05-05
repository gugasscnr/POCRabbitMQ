#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Base abstraction for RabbitMQ client operations.
This module provides a common interface for both producers and consumers.
"""

import pika
import logging
from abc import ABC, abstractmethod
from contextlib import contextmanager

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)


class RabbitMQClient(ABC):
    """Abstract base class for RabbitMQ client operations"""
    
    def __init__(self, connection_params=None, connection=None):
        """
        Initialize with either connection parameters or an existing connection
        
        Args:
            connection_params (pika.ConnectionParameters, optional): Connection parameters
            connection (pika.BlockingConnection, optional): Existing connection
        """
        self.connection = connection
        self.connection_params = connection_params
        self.channel = None
        self._initialize_channel()
    
    def _initialize_channel(self):
        """Initialize the channel from connection"""
        if not self.connection and self.connection_params:
            try:
                self.connection = pika.BlockingConnection(self.connection_params)
                logger.info("Created new RabbitMQ connection")
            except Exception as e:
                logger.error(f"Failed to create RabbitMQ connection: {e}")
                raise
        
        if self.connection:
            try:
                self.channel = self.connection.channel()
                logger.info("Created RabbitMQ channel")
            except Exception as e:
                logger.error(f"Failed to create RabbitMQ channel: {e}")
                raise
        else:
            raise ValueError("Either connection or connection_params must be provided")
    
    @contextmanager
    def channel_operation(self):
        """
        Context manager for channel operations to ensure proper error handling
        
        Yields:
            pika.channel.Channel: The active channel
        """
        try:
            yield self.channel
        except pika.exceptions.AMQPChannelError as e:
            logger.error(f"Channel error: {e}")
            self._initialize_channel()  # Try to recover
            raise
        except pika.exceptions.AMQPConnectionError as e:
            logger.error(f"Connection error: {e}")
            self._initialize_channel()  # Try to recover
            raise
        except Exception as e:
            logger.error(f"Unexpected error in channel operation: {e}")
            raise
    
    def close(self):
        """Close the channel and connection"""
        try:
            if self.channel and self.channel.is_open:
                self.channel.close()
                logger.info("Closed RabbitMQ channel")
        except Exception as e:
            logger.error(f"Error closing channel: {e}")
        
        # Don't close the connection if it was provided externally
        if self.connection_params and self.connection:
            try:
                self.connection.close()
                logger.info("Closed RabbitMQ connection")
            except Exception as e:
                logger.error(f"Error closing connection: {e}")
    
    @abstractmethod
    def run(self):
        """Abstract method to be implemented by subclasses"""
        pass
