package com.sangyunpark.product.presentation;

import com.sangyunpark.product.application.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StockController.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    @Test
    @DisplayName("재고 감소 API 호출 성공")
    void decreaseStock_success() throws Exception {
        // given
        Long productId = 1L;
        Long quantity = 5L;
        Long orderId = 100L;

        doNothing().when(stockService).decreaseStockAndPublish(productId, quantity, orderId);

        // when & then
        mockMvc.perform(patch("/api/v1/stocks/{productId}/decrease", productId)
                        .param("quantity", quantity.toString())
                        .param("orderId", orderId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("재고 증가 API 호출 성공")
    void increaseStock_success() throws Exception {
        // given
        Long productId = 2L;
        Long quantity = 3L;

        doNothing().when(stockService).increaseStock(productId, quantity);

        // when & then
        mockMvc.perform(patch("/api/v1/stocks/{productId}/increase", productId)
                        .param("quantity", quantity.toString()))
                .andExpect(status().isOk());
    }
}