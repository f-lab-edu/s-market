package com.sangyunpark.product.application;

import com.sangyunpark.product.infrastructure.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;


}
