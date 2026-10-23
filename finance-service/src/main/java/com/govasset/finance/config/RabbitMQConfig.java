package com.govasset.finance.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${govasset.rabbitmq.exchange:govasset.topic.exchange}")
    private String exchangeName;

    @Value("${govasset.rabbitmq.compensation-queue:queue.finance.compensations}")
    private String compensationQueueName;

    @Bean
    public TopicExchange govassetTopicExchange() {
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    @Bean
    public Queue financeCompensationQueue() {
        return QueueBuilder.durable(compensationQueueName).build();
    }

    @Bean
    public Binding bindingCompensationProcurement(Queue financeCompensationQueue, TopicExchange govassetTopicExchange) {
        return BindingBuilder.bind(financeCompensationQueue).to(govassetTopicExchange).with("saga.compensate.procurement");
    }

    @Bean
    public Binding bindingCompensationAssetFailed(Queue financeCompensationQueue, TopicExchange govassetTopicExchange) {
        return BindingBuilder.bind(financeCompensationQueue).to(govassetTopicExchange).with("asset.registration.failed");
    }

    @Bean
    public Binding bindingCompensationMaintenanceFailed(Queue financeCompensationQueue, TopicExchange govassetTopicExchange) {
        return BindingBuilder.bind(financeCompensationQueue).to(govassetTopicExchange).with("maintenance.schedule.failed");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
