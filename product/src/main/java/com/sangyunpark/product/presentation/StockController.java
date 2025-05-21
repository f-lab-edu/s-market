package com.sangyunpark.product.presentation;

import com.sangyunpark.product.application.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/{productId}")
    public Long getQuantity(@PathVariable final Long productId) {
        return stockService.getQuantityByProductId(productId);
    }

    @PostMapping("/exists")
    Map<Long,Long> checkProductExists(@RequestBody List<Long> productsId) {
        return stockService.checkExistence(productsId);
    }
}
