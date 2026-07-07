package com.example.EDPaySystem.service;

import com.example.EDPaySystem.dto.PaymentEvent;
import com.example.EDPaySystem.entity.PaymentRecord;
import com.example.EDPaySystem.entity.PaymentStatus;
import com.example.EDPaySystem.repository.PaymentRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentAuditService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    public void recordInitiated(PaymentEvent event) {
        save(event, PaymentStatus.INITIATED, null);
    }

    public void recordProcessing(PaymentEvent event) {
        save(event, PaymentStatus.PROCESSING, null);
    }

    public void recordCompleted(PaymentEvent event) {
        save(event, PaymentStatus.COMPLETED, null);
    }

    public void recordFailed(PaymentEvent event, String reason) {
        save(event, PaymentStatus.FAILED, reason);
    }

    private void save(PaymentEvent event, PaymentStatus status, String failureReason) {
        PaymentRecord record = PaymentRecord.builder()
                .paymentId(event.getPaymentId())
                .senderId(event.getSenderId())
                .receiverId(event.getReceiverId())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .type(event.getChannel())
                .status(status)
                .retryCount(event.getRetryCount())
                .failureReason(failureReason)
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(record);
        meterRegistry.counter("payments_processed_total", "status", status.name()).increment();
    }
}
