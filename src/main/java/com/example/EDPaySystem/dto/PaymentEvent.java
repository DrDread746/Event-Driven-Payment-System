package com.example.EDPaySystem.dto;

import com.example.EDPaySystem.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {

    private String paymentId;
    private String senderId;
    private String receiverId;
    private BigDecimal amount;
    private String currency;
    private PaymentChannel channel;
    private PaymentStatus status;

    private int retryCount = 0;

    public enum PaymentChannel {
        BANK_TRANSFER,
        UPI,
        CARD,
        WALLET
    }
}