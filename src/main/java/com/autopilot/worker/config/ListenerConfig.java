package com.autopilot.worker.config;

import com.autopilot.worker.consumer.UserContactMessageConsumer;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ListenerConfig {

    @Value("${app.rabbitmq.queues.email}")
    private String emailQueueName;

    @Bean
    public SimpleMessageListenerContainer userContactMessageListener(
            ConnectionFactory connectionFactory,
            UserContactMessageConsumer userContactMessageConsumer
    ) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.addQueueNames(emailQueueName);
        container.setMessageListener(userContactMessageConsumer);
        return container;
    }

    //Add new bean definitions for other listeners here. New listeners for other queues can be added similarly.
}
