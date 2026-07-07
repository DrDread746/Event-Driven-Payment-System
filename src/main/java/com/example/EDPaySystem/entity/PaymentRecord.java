package com.example.EDPaySystem.entity;


import com.example.EDPaySystem.dto.PaymentEvent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_payment_id", columnList = "payment_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String paymentId;
    private String senderId;
    private String receiverId;

    private BigDecimal amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentEvent.PaymentChannel type;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private int retryCount;
    private String failureReason;

    private LocalDateTime createdAt;

    @PreUpdate
    public void preventUpdate() {
        throw new IllegalStateException("Payment record is immutable. Create a new record for status: " + this.status);
    }

}

