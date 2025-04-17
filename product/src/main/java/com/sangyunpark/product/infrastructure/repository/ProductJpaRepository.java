package com.sangyunpark.product.infrastructure.repository;

import com.sangyunpark.product.domain.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.createdAt < :cursor AND p.startAt <= :now AND p.endAt >: now ORDER BY p.createdAt DESC")
    List<Product> findAllByCursor(@Param("cursor") LocalDateTime cursor, @Param("now") LocalDateTime now, Pageable pageable);
}
