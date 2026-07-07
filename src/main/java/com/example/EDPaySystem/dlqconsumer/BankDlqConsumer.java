package com.example.EDPaySystem.dlqconsumer;

import com.example.EDPaySystem.config.RabbitMQProperties;
import com.example.EDPaySystem.dto.PaymentEvent;
import com.example.EDPaySystem.entity.PaymentRecord;
import com.example.EDPaySystem.repository.PaymentRepository;
import com.example.EDPaySystem.service.PaymentAuditService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BankDlqConsumer {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentAuditService auditService;

    @RabbitListener(queues = RabbitMQProperties.BANK_DLQ)
    public void consume(PaymentEvent event) {
        System.err.println("[BANK-DLQ] Dead lettered: " + event.getReceiverId()
                + " after " + event.getRetryCount() + " retries");

        auditService.recordFailed(event, "Exceeded max retries (" + event.getRetryCount() + ")");

        PaymentRecord Payment = PaymentRecord.builder()
                .paymentId(event.getPaymentId())
                .senderId(event.getSenderId())
                .receiverId(event.getReceiverId())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .type(event.getChannel())
                .failureReason("Exceeded max Retries " +  event.getRetryCount())
                .status(event.getStatus())
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(Payment);
        System.err.println("[BANK-DLQ] Saved to database for manual review.");

    }
}
