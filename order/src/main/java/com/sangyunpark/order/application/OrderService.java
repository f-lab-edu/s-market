package com.sangyunpark.order.application;

import com.sangyunpark.order.client.ProductClient;
import com.sangyunpark.order.client.StockClient;
import com.sangyunpark.order.constant.code.ErrorCode;
import com.sangyunpark.order.constant.enums.OrderStatus;
import com.sangyunpark.order.domain.Order;
import com.sangyunpark.order.exception.BusinessException;
import com.sangyunpark.order.infrastructure.OrderJpaRepository;
import com.sangyunpark.order.presentation.dto.request.CreateOrderRequest;
import com.sangyunpark.order.presentation.dto.request.OrderItemRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductClient productClient;
    private final StockClient stockClient;
    private final OrderJpaRepository orderJpaRepository;

    @Transactional
    public Long createOrder(final CreateOrderRequest request) {

        final Long userId = request.userId();
        List<OrderItemRequest> orderItems = request.items();
        List<Long> productIds = orderItems.stream().map(OrderItemRequest::productId).toList();

        final Map<Long,Boolean> isExistedProduct = productClient.checkProductsExist(productIds);
        final Map<Long, Long> productQuantities = stockClient.getQuantitiesByProductIds(productIds);

        for(OrderItemRequest item: orderItems) {
            final Long productId = item.productId();
            final int orderQuantity = item.quantity();

            if(!isExistedProduct.getOrDefault(productId, false)) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            if(productQuantities.getOrDefault(productId, 0L) < orderQuantity) {
                throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK);
            }
        }

        Order order = Order.builder()
                .userId(userId)
                .orderNumber(UUID.randomUUID().toString())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderJpaRepository.save(order);
        return savedOrder.getId();
    }
}
