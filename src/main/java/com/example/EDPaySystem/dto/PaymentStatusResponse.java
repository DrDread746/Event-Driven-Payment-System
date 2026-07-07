package com.example.EDPaySystem.dto;

import com.example.EDPaySystem.entity.PaymentRecord;
import com.example.EDPaySystem.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PaymentStatusResponse {
    private String paymentId;
    private PaymentStatus currentStatus;
    private BigDecimal amount;
    private String currency;
    private String failureReason;
    private LocalDateTime lastUpdatedAt;

    public static PaymentStatusResponse from(PaymentRecord latest){
        return new PaymentStatusResponse(
                latest.getPaymentId(),
                latest.getStatus(),
                latest.getAmount(),
                latest.getCurrency(),
                latest.getFailureReason(),
                latest.getCreatedAt()
        );
    }
}
