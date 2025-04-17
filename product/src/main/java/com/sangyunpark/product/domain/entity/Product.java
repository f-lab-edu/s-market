package com.sangyunpark.product.domain.entity;

import com.sangyunpark.product.presentation.dto.request.ProductRequestDto;
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

    public void update(ProductRequestDto dto, Category category) {
        this.category = category;
        this.title = dto.title();
        this.description = dto.description();
        this.price = dto.price();
        this.visible = dto.visible();
        this.startAt = dto.startAt();
        this.endAt = dto.endAt();
        this.updatedAt = LocalDateTime.now();
    }
}
