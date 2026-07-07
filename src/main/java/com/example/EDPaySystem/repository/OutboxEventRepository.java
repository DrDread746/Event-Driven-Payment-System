package com.example.EDPaySystem.repository;

import com.example.EDPaySystem.dto.OutboxEvent;
import com.example.EDPaySystem.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<OutboxEvent> findBatchByStatus(@Param("status") OutboxStatus status);

    List<OutboxEvent> findByStatus(OutboxStatus status);

}
