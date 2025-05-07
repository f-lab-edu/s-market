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

    @Query(value = "SELECT o FROM StockOutbox o WHERE o.status = :status ORDER BY o.createdAt LIMIT 100")
    List<StockOutbox> findPendingOutboxEvent(@Param("status") OutboxStatus status, LocalDateTime createdAt);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE StockOutbox o SET o.status = :status, o.updatedAt = :updatedAt WHERE o.id IN :ids")
    void bulkUpdateStatus(@Param("status") OutboxStatus status, @Param("updatedAt") LocalDateTime updatedAt, @Param("ids") List<Long> ids);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE StockOutbox o SET o.status = :status, o.updatedAt = :updatedAt WHERE o.orderId = :orderId")
    void updateStatusByOrderId(@Param("orderId") Long orderId, @Param("status") OutboxStatus status, @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE StockOutbox o SET o.status = :status, o.updatedAt = :updatedAt WHERE o.eventId = :eventId")
    void updateStatusByEventId(@Param("eventId") String eventId, @Param("status") OutboxStatus status, @Param("updatedAt") LocalDateTime updatedAt);
}
