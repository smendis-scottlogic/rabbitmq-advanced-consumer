package com.example.rabbitmqconsumer.config;

import com.example.rabbitmqconsumer.receivers.ErrorMessageResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.exception.FatalListenerExecutionException;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.StatefulRetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {
    private final AppProperties properties;

    public RabbitMqConfig(AppProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Exchange directExchange(){
        return new DirectExchange(properties.getDirectExchange(), true, false);
    }

    @Bean
    public Queue requestQueue(){
        return QueueBuilder
                .durable(properties.getDirectQueue())
                .withArgument("x-dead-letter-exchange", properties.getDirectQueue())
                .withArgument("x-dead-letter-routing-key", properties.getDirectDeadLetterQueue())
                .build();
    }

    @Bean
    public Binding bindRequestQueue(){
        return BindingBuilder
                .bind(requestQueue())
                .to(directExchange())
                .with(properties.getDirectQueue())
                .noargs();
    }

    @Bean
    public Queue requestDLQueue(){
        return QueueBuilder
                .durable(properties.getDirectDeadLetterQueue())
                .build();
    }

    @Bean
    public Binding bindRequestDLQueue(){
        return BindingBuilder
                .bind(requestDLQueue())
                .to(directExchange())
                .with(properties.getDirectDeadLetterQueue())
                .noargs();
    }

    @Bean
    Jackson2JsonMessageConverter messageConverter(ObjectMapper mapper){
        var converter = new Jackson2JsonMessageConverter(mapper);
        converter.setCreateMessageIds(true); //create a unique message id for every message
        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            StatefulRetryOperationsInterceptor retryInterceptor,
            ObjectMapper objectMapper) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter(objectMapper));
        factory.setConcurrentConsumers(properties.getConcurrentConsumers());
        factory.setMaxConcurrentConsumers(properties.getMaxConcurrentConsumers());
//        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(0);
        factory.setAdviceChain(retryInterceptor);
        return factory;
    }

//    @Bean
//    public SimpleRetryPolicy rabbitRetryPolicy() {
//        Map<Class<? extends Throwable>, Boolean> exceptionsMap = new HashMap<>();
//        exceptionsMap.put(ListenerExecutionFailedException.class, true);
//        exceptionsMap.put(MessageConversionException.class, false);
//        exceptionsMap.put(FatalListenerExecutionException.class, false);
//        return new SimpleRetryPolicy(properties.getRetryAttempts(), exceptionsMap, true);
//    }

    @Bean
    public StatefulRetryOperationsInterceptor messageRetryInterceptor(
//            SimpleRetryPolicy retryPolicy,
            MessageRecoverer messageRecoverer){
        return RetryInterceptorBuilder.StatefulRetryInterceptorBuilder
                .stateful()
//                .retryPolicy(retryPolicy)
                .maxAttempts(properties.getRetryAttempts())
                .backOffOptions(
                        properties.getBackoffInterval(),
                        properties.getBackoffMultiplier(),
                        properties.getBackoffMaxInterval()
                )
                .recoverer(messageRecoverer)
                .build();
    }

    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper){
//        return new RejectAndDontRequeueRecoverer();
        return new ErrorMessageResolver(
                rabbitTemplate,
                properties.getDirectExchange(),
                properties.getDirectDeadLetterQueue(),
                messageConverter(objectMapper)
        );
    }
}
