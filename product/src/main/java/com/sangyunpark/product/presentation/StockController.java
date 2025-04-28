package com.sangyunpark.product.presentation;

import com.sangyunpark.product.application.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stocks")
public class StockController {

    private final StockService stockService;

    @PatchMapping("/{productId}/decrease")
    public void decreaseStock(
            @PathVariable final Long productId,
            @RequestParam final Long quantity,
            @RequestParam final Long orderId
    ) {
        stockService.decreaseStockAndPublish(productId, quantity, orderId);
    }
}
