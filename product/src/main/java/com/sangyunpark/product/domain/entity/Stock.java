package com.sangyunpark.product.domain.entity;

import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "product_stock")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    @Getter
    @Column(nullable = false)
    private Long quantity;

    public void decreaseQuantity(final Long amount) {
        if(this.quantity < amount) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }
        this.quantity -= amount;
    }

    public void increaseQuantity(final Long amount) {
        this.quantity += amount;
    }
}
