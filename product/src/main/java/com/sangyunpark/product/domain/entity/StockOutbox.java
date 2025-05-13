package com.sangyunpark.product.domain.entity;

import com.sangyunpark.product.constant.OutboxStatus;
import com.sangyunpark.product.constant.OutboxType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_outbox", indexes = {
        @Index(name = "idx_status_created_at", columnList = "status, createdAt")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockOutbox {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private String eventId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxType type;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
