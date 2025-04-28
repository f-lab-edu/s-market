package com.sangyunpark.product.infrastructure.repository;

import com.sangyunpark.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

}
