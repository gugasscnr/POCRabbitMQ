#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Configuration module for RabbitMQ connection
"""

# RabbitMQ connection parameters
HOST = "localhost"
PORT = 5672
USERNAME = "guest"
PASSWORD = "guest"

# Exchange name - trade inbound
TRADE_EXCHANGE = "ex.trade.standard.corporatebond"

# Queue name
QUEUE_NAME = "poc.queue.one"

# Routing key
ROUTING_KEY = "poc.key.one"
