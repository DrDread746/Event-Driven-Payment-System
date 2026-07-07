package com.example.EDPaySystem.consumer;

import com.example.EDPaySystem.config.RabbitMQProperties;
import com.example.EDPaySystem.dto.PaymentEvent;
import com.example.EDPaySystem.service.PaymentAuditService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UpiConsumer extends BaseConsumer {

    @Autowired
    private PaymentAuditService auditService;

    @RabbitListener(queues = RabbitMQProperties.UPI_QUEUE)
    public void consume(PaymentEvent event, Message message) {

        if(isDuplicate(event)) return;

        // processing started
        auditService.recordProcessing(event);

        try {
            System.out.println("[UPI] Processing: " + event.getReceiverId());
            if (Math.random() < 0.7) throw new RuntimeException("UPI service down!");

            markProcessed(event);
            auditService.recordCompleted(event);
            System.out.println("[UPI] Sent successfully to: " + event.getReceiverId());

        } catch (Exception e) {
            handleRetry(
                    event,
                    RabbitMQProperties.UPI_ROUTING_KEY + ".retry",
                    RabbitMQProperties.UPI_ROUTING_KEY + ".dlq",
                    e
            );
        }
    }
}
