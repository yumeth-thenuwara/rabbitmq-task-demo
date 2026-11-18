
package com.neopadma.rabbitmqdemo;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Main task queue
    public static final String QUEUE_NAME = "demo.tasks.v2";

    // Failed messages are stored here
    public static final String DEAD_LETTER_QUEUE_NAME =
            "demo.tasks.dlq";

    // Exchange used to route failed messages
    public static final String DEAD_LETTER_EXCHANGE =
            "demo.tasks.dlx";

    public static final String DEAD_LETTER_ROUTING_KEY =
            "failed";

    @Bean
    public Queue taskQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(
                DEAD_LETTER_QUEUE_NAME
        ).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Binding deadLetterBinding(
            @Qualifier("deadLetterQueue") Queue dlq,
            @Qualifier("deadLetterExchange") DirectExchange dlx) {

        return BindingBuilder.bind(dlq)
                .to(dlx)
                .with(DEAD_LETTER_ROUTING_KEY);
    }
}
