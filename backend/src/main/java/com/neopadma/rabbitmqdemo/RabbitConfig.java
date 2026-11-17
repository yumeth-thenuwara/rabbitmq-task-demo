package com.neopadma.rabbitmqdemo;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_NAME = "demo.tasks";

    @Bean
    public Queue taskQueue() {
        return new Queue(QUEUE_NAME, true);
    }
}