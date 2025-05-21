package com.sangyunpark.order.presentation;

import com.sangyunpark.order.application.OrderService;
import com.sangyunpark.order.presentation.dto.request.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Long createOrder(final @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

}
