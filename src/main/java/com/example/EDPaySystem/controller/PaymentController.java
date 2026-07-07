package com.example.EDPaySystem.controller;


import com.example.EDPaySystem.dto.PaymentEvent;
import com.example.EDPaySystem.entity.PaymentStatus;
import com.example.EDPaySystem.repository.PaymentRepository;
import com.example.EDPaySystem.service.OutboxEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OutboxEventService outboxEventService;

    @PostMapping("/process")
    public ResponseEntity<String> sendNotification(@RequestBody PaymentEvent event) {
        // If no paymentId provided, generate one
        if (event.getPaymentId() == null || event.getPaymentId().isBlank()) {
            event.setPaymentId(UUID.randomUUID().toString());
        }

        // Don't re-initiate if already exists
        if (paymentRepository.existsById(event.getPaymentId())) {
            return ResponseEntity.ok("Payment already exists: " + event.getPaymentId());
        }

        event.setStatus(PaymentStatus.INITIATED);
        outboxEventService.recordAndEnqueue(event);
        return ResponseEntity.ok("Payment queued: " + event.getPaymentId());
    }
}
