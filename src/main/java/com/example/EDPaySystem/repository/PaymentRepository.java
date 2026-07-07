package com.example.EDPaySystem.repository;

import com.example.EDPaySystem.entity.PaymentRecord;
import com.example.EDPaySystem.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface PaymentRepository extends JpaRepository<PaymentRecord, String> {
    List<PaymentRecord> findByStatus(PaymentStatus status);
    Optional<PaymentRecord> findByPaymentId(String paymentId);

    boolean existsByPaymentId(String paymentId);

    List<PaymentRecord> findByPaymentIdOrderByCreatedAtDesc(String paymentId);
}
