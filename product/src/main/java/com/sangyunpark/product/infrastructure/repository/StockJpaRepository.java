package com.sangyunpark.product.infrastructure.repository;

import com.sangyunpark.product.domain.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {

    @Modifying
    @Query("UPDATE Stock s SET s.quantity = s.quantity - :quantity WHERE s.productId = :productId AND s.quantity >= :quantity")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Long quantity);

    @Query("SELECT s.quantity FROM Stock s WHERE s.productId = :productId")
    Optional<Long> findQuantityByProductId(Long productId);

    @Modifying
    @Query("UPDATE Stock s SET s.quantity = s.quantity + :quantity WHERE s.productId = :productId")
    int increaseStock(@Param("productId") Long productId, @Param("quantity") Long quantity);
}
