package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ALERT_QUEUE = "alertQueue";
    public static final String ALERT_EXCHANGE = "alertExchange";
    public static final String ALERT_ROUTING_KEY = "alertRoutingKey";

    public static final String ALERT_DLQ = "alertQueue.dlq";
    public static final String ALERT_DLQ_EXCHANGE = "alertDlqExchange";
    public static final String ALERT_DLQ_ROUTING_KEY = "alertDlqRoutingKey";

    /**
     * Main queue with DLQ binding for retry handling
     */
    @Bean
    public Queue alertQueue() {
        return QueueBuilder.durable(ALERT_QUEUE)
                .build();
    }

    /**
     * Dead Letter Queue
     */
    @Bean
    public Queue alertDeadLetterQueue() {
        return QueueBuilder.durable(ALERT_DLQ).build();
    }

    /**
     * Main exchange
     */
    @Bean
    public TopicExchange alertExchange() {
        return new TopicExchange(ALERT_EXCHANGE);
    }

    /**
     * DLQ exchange
     */
    @Bean
    public TopicExchange alertDlqExchange() {
        return new TopicExchange(ALERT_DLQ_EXCHANGE);
    }

    /**
     * Bind main queue to main exchange
     */
    @Bean
    public Binding alertBinding() {
        return BindingBuilder.bind(alertQueue())
                .to(alertExchange())
                .with(ALERT_ROUTING_KEY);
    }

    /**
     * Bind DLQ to DLQ exchange
     */
    @Bean
    public Binding alertDlqBinding() {
        return BindingBuilder.bind(alertDeadLetterQueue())
                .to(alertDlqExchange())
                .with(ALERT_DLQ_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }

//    @Bean
//    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
//            ConnectionFactory connectionFactory) {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory);
//        factory.setPrefetchCount(1);
//        return factory;
//    }
}