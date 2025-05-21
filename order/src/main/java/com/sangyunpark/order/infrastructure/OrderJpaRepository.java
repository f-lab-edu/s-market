package com.sangyunpark.order.infrastructure;

import com.sangyunpark.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
}
