package com.sangyunpark.product.infrastructure.repository;

import com.sangyunpark.product.constant.OutboxStatus;
import com.sangyunpark.product.domain.entity.StockOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StockOutboxRepository extends JpaRepository<StockOutbox, Long> {

    @Query(value = "SELECT * FROM stock_outbox WHERE status = :status ORDER BY created_at LIMIT 100", nativeQuery = true)
    List<StockOutbox> findPendingOutboxEvent(@Param("status") String status);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE StockOutbox o SET o.status = :status, o.updatedAt = :updatedAt WHERE o.id IN :ids")
    int bulkUpdateStatus(@Param("status") OutboxStatus status, @Param("updatedAt") LocalDateTime updatedAt, @Param("ids") List<Long> ids);
}
