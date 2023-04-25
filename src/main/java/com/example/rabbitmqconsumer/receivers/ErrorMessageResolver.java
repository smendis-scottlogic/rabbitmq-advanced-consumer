package com.example.rabbitmqconsumer.receivers;

import com.example.rabbitmqconsumer.config.AppProperties;
import com.example.rabbitmqconsumer.exceptions.FailedProcessException;
import com.example.rabbitmqconsumer.models.FailedMessage;
import com.example.rabbitmqconsumer.models.MyMessage;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

public class ErrorMessageResolver implements MessageRecoverer {
    private final RabbitTemplate template;
    private final AppProperties properties;
    private final Jackson2JsonMessageConverter converter;

    public ErrorMessageResolver(RabbitTemplate rabbitTemplate,
                                AppProperties properties,
                                Jackson2JsonMessageConverter converter) {
        this.template = rabbitTemplate;
        this.properties = properties;
        this.converter = converter;
    }

    @Override
    public void recover(Message message, Throwable cause){
        if(cause instanceof ListenerExecutionFailedException &&
                cause.getCause() instanceof FailedProcessException){
            try {
                //retrieve original message
                message.getMessageProperties().setInferredArgumentType(MyMessage.class);
                MyMessage originalRequest = (MyMessage) converter.fromMessage(message, MyMessage.class);

                FailedMessage failedMessage = new FailedMessage(
                        originalRequest.getMessageId(),
                        originalRequest.getMessage(),
                        cause.getCause().getMessage()
                );
                //send the message to response queue
                this.template.convertAndSend(
                        properties.getDirectExchange(),
                        properties.getResponseQueue(),
                        failedMessage
                );

            } catch (Exception ex){
                //send the message to dead letter queue
                throw new AmqpRejectAndDontRequeueException("Unable to recover message", ex);
            }
        } else {
            //send the message to dead letter queue
            throw new AmqpRejectAndDontRequeueException("Unable to recover message");
        }
    }
}
