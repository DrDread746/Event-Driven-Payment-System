package com.example.EDPaySystem.producer;

import com.example.EDPaySystem.config.RabbitMQProperties;
import com.example.EDPaySystem.dto.PaymentEvent;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {

    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sendPayment(PaymentEvent event){
        amqpTemplate.convertAndSend(
                RabbitMQProperties.EXCHANGE_NAME,
                event.getChannel().name(), // routing key becomes EMAIL SMS PUSH dynamically
                event
        );
        System.out.println("Published: to [" + event.getChannel().name() + "] queue : " + event);
    }
}
