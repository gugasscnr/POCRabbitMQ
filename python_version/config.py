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

# Exchange names
DIRECT_EXCHANGE = "poc.direct.exchange"
FANOUT_EXCHANGE = "poc.fanout.exchange"
TOPIC_EXCHANGE = "poc.topic.exchange"

# Queue names
QUEUE_1 = "poc.queue.one"
QUEUE_2 = "poc.queue.two" 
QUEUE_3 = "poc.queue.three"

# Routing keys
ROUTING_KEY_1 = "poc.key.one"
ROUTING_KEY_2 = "poc.key.two"
TOPIC_KEY_PATTERN = "poc.topic.#"
