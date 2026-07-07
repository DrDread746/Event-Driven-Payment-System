package com.example.EDPaySystem.dto;

import com.example.EDPaySystem.entity.OutboxStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_aggregate_id", columnList = "aggregate_id"),
        @Index(name = "idx_outbox_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String aggregateId;
    private String eventType;
    private String routingKey;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;  // serialized PaymentEvent JSON

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;

    @Builder.Default
    private int retryCount = 0;

    private String lastError;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
}
