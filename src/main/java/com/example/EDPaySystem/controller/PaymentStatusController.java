package com.example.EDPaySystem.controller;


import com.example.EDPaySystem.dto.OutboxEvent;
import com.example.EDPaySystem.dto.PaymentStatusResponse;
import com.example.EDPaySystem.entity.OutboxStatus;
import com.example.EDPaySystem.entity.PaymentRecord;
import com.example.EDPaySystem.repository.OutboxEventRepository;
import com.example.EDPaySystem.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentStatusController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    // Current status only
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<?> getStatus(@PathVariable String paymentId) {
        List<PaymentRecord> records = paymentRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId);

        if (records.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(PaymentStatusResponse.from(records.get(0)));
    }

    // Full append-only audit trail - INITIATED -> PROCESSING -> ... -> COMPLETED/FAILED
    @GetMapping("/{paymentId}/history")
    public ResponseEntity<?> getHistory(@PathVariable String paymentId) {
        List<PaymentRecord> records = paymentRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId);

        if (records.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(records);
    }

    @PostMapping("/api/outbox/{outboxEventId}/replay")
    public ResponseEntity<?> replay(@PathVariable String outboxEventId) {
        OutboxEvent outboxEvent = outboxEventRepository.findById(outboxEventId).orElse(null);
        if (outboxEvent == null) return ResponseEntity.notFound().build();

        outboxEvent.setStatus(OutboxStatus.PENDING);
        outboxEvent.setRetryCount(0);
        outboxEventRepository.save(outboxEvent);
        return ResponseEntity.ok("Re-queued for relay: " + outboxEventId);
    }
}
