package com.example.EDPaySystem.service;

import com.example.EDPaySystem.dto.OutboxEvent;
import com.example.EDPaySystem.dto.PaymentEvent;
import com.example.EDPaySystem.entity.OutboxStatus;
import com.example.EDPaySystem.entity.PaymentStatus;
import com.example.EDPaySystem.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OutboxEventService {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private PaymentAuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public void recordAndEnqueue(PaymentEvent event) {
        auditService.recordInitiated(event);

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize PaymentEvent for outbox: " + event.getPaymentId(), e);
        }

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateId(event.getPaymentId())
                .eventType("PAYMENT_" + PaymentStatus.INITIATED)
                .routingKey(event.getChannel().name())
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        outboxEventRepository.save(outboxEvent);
    }
}
