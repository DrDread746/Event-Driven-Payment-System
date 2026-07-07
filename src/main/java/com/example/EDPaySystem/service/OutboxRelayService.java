package com.example.EDPaySystem.service;


import com.example.EDPaySystem.config.RabbitMQProperties;
import com.example.EDPaySystem.dto.OutboxEvent;
import com.example.EDPaySystem.dto.PaymentEvent;
import com.example.EDPaySystem.entity.OutboxStatus;
import com.example.EDPaySystem.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxRelayService {
    private static final int MAX_RELAY_RETRIES  = 5;

    @Autowired
    private OutboxEventRepository  outboxEventRepository;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MeterRegistry meterRegistry;

    @Scheduled(fixedDelayString = "${outbox.poll.interval.ms:3000}")
    public void relayPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findBatchByStatus(OutboxStatus.PENDING);
        if (pending.isEmpty()) return;

        System.out.println("[OUTBOX] Relaying " + pending.size() + " pending event(s)");
        for (OutboxEvent outboxEvent : pending) {
            relayOne(outboxEvent.getId());
        }
    }

    @Transactional
    public void relayOne(String outboxEventId) {
        OutboxEvent outboxEvent = outboxEventRepository.findById(outboxEventId).orElse(null);
        if (outboxEvent == null || outboxEvent.getStatus() != OutboxStatus.PENDING) return;

        try {
            PaymentEvent event = objectMapper.readValue(outboxEvent.getPayload(), PaymentEvent.class);

            amqpTemplate.convertAndSend(
                    RabbitMQProperties.EXCHANGE_NAME,
                    outboxEvent.getRoutingKey(),
                    event
            );

            outboxEvent.setStatus(OutboxStatus.PUBLISHED);
            outboxEvent.setPublishedAt(LocalDateTime.now());
            System.out.println("[OUTBOX] Published " + outboxEvent.getAggregateId()
                    + " -> [" + outboxEvent.getRoutingKey() + "]");
        } catch (Exception e) {
            int attempts = outboxEvent.getRetryCount() + 1;
            outboxEvent.setRetryCount(attempts);
            outboxEvent.setLastError(e.getMessage());

            if (attempts >= MAX_RELAY_RETRIES) {
                outboxEvent.setStatus(OutboxStatus.FAILED);
                System.err.println("[OUTBOX] Giving up on " + outboxEvent.getAggregateId()
                        + " after " + attempts + " attempts: " + e.getMessage());
            } else {
                System.err.println("[OUTBOX] Relay attempt " + attempts + " failed for "
                        + outboxEvent.getAggregateId() + ": " + e.getMessage());
            }
        }

        outboxEventRepository.save(outboxEvent);
    }

    // Runs once a day (3 AM) - deletes PUBLISHED rows older than 7 days.
    // FAILED rows are deliberately for manual inspection
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldPublishedEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<OutboxEvent> stale = outboxEventRepository.findByStatus(OutboxStatus.PUBLISHED)
                .stream()
                .filter(e -> e.getPublishedAt() != null && e.getPublishedAt().isBefore(cutoff))
                .toList();

        if (!stale.isEmpty()) {
            outboxEventRepository.deleteAll(stale);
            System.out.println("[OUTBOX] Cleaned up " + stale.size() + " old published event(s)");
        }
    }

    @PostConstruct
    public void registerGauges() {
        meterRegistry.gauge("outbox_pending_count", this, s ->
                outboxEventRepository.findByStatus(OutboxStatus.PENDING).size());

        meterRegistry.gauge("outbox_relay_lag_seconds", this, s -> {
            List<OutboxEvent> pending = outboxEventRepository.findByStatus(OutboxStatus.PENDING);
            if (pending.isEmpty()) return 0.0;
            LocalDateTime oldest = pending.stream()
                    .map(OutboxEvent::getCreatedAt)
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());
            return (double) Duration.between(oldest, LocalDateTime.now()).getSeconds();
        });
    }
}
