package com.sangyunpark.product.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Boolean visible;

    @Setter
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    public void updateCategory(final Category category) {
        this.category = category;
    }

    public void updateTitle(final String title) {
        this.title = title;
    }

    public void updateDescription(final String description) {
        this.description = description;
    }

    public void updatePrice(final Long price) {
        this.price = price;
    }

    public void updateVisible(final Boolean visible) {
        this.visible = visible;
    }

    public void updateStartAt(final LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public void updateEndAt(final LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public void updateUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
