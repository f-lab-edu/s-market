package com.sangyunpark.product.infrastructure.repository;

import com.sangyunpark.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p.id FROM Product p WHERE p.id IN :ids")
    List<Long> findExistingIds(@Param("ids") List<Long> ids);
}
