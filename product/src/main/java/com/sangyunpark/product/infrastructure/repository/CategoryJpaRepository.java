package com.sangyunpark.product.infrastructure.repository;

import com.sangyunpark.product.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {

    @Query("select c from Category c left join fetch c.children where c.id = :id")
    Optional<Category> findWithChildrenById(@Param("id") Long id);
}
