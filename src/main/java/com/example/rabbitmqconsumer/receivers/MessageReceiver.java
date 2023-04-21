package com.example.rabbitmqconsumer.receivers;

import com.example.rabbitmqconsumer.models.MyMessage;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class MessageReceiver {

    @RabbitListener(
            queues = "#{requestQueue.name}",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void receiveMessage(MyMessage message) throws Exception {
        System.out.println("-----------Received: "+message.toString());
        Thread.sleep(5000);
        if(message.getMessageId() > 5){
            throw new Exception("abc");
        } else if(message.getMessageId() == 3){
            System.out.println("Sleep for another 60 seconds");
            Thread.sleep(60000);
            System.out.println("---------Listener method executed successfully after 65 seconds");
        } else {
            System.out.println("---------Listner method executed successfully");
        }
    }

//    @RabbitListener(
//            queues = "#{completeQueue.name}",
//            containerFactory = "rabbitListenerManualContainerFactory"
//    )
    public void receiveMessageManual(MyMessage message,
                                     Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
        System.out.println("-----------Received: "+message.toString());
        Thread.sleep(5000);
        if(message.getMessageId() > 5){
//            throw new Exception("abc");
            channel.basicNack(tag, false, true);
        } else if(message.getMessageId() == 3){
            System.out.println("Sleep for another 60 seconds");
            Thread.sleep(60000);
            System.out.println("---------Listener method executed successfully after 65 seconds");
            channel.basicAck(tag, false);
        } else {
            System.out.println("---------Listner method executed successfully");
            channel.basicAck(tag, false);
        }
    }
}
