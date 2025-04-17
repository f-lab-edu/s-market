package com.sangyunpark.product.infrastructure.repository;

import com.sangyunpark.product.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {
}
