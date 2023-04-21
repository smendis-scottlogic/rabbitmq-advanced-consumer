package com.example.rabbitmqconsumer.receivers;

import com.example.rabbitmqconsumer.models.MyMessage;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

public class ErrorMessageResolver extends RepublishMessageRecoverer {

    private final Jackson2JsonMessageConverter converter;
    public ErrorMessageResolver(AmqpTemplate errorTemplate,
                                String errorExchange,
                                String errorRoutingKey,
                                Jackson2JsonMessageConverter converter) {
        super(errorTemplate, errorExchange, errorRoutingKey);
        this.converter = converter;
    }

    @Override
    public void recover(Message message, Throwable cause){
        System.out.println("--------------Recoverer called");
        if(cause instanceof ListenerExecutionFailedException &&
                cause.getCause() instanceof Exception){
            try {
                message.getMessageProperties().setInferredArgumentType(MyMessage.class);
                MyMessage originalRequest = (MyMessage) converter.fromMessage(message, MyMessage.class);

                //can fire the original message to a different queue if needed

                //send to dead letter queue
                throw new AmqpRejectAndDontRequeueException("Can't process "+originalRequest.getMessageId());
            } catch (Exception ex){
                super.recover(message, cause);
            }
        } else {
            super.recover(message, cause);
        }
    }
}
