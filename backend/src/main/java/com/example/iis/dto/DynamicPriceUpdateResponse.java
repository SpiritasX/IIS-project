package com.example.iis.dto;

import java.math.BigDecimal;

public record DynamicPriceUpdateResponse(
        Long plantId,
        String plantName,
        BigDecimal oldPrice,
        BigDecimal newPrice,
        Long stock,
        Long demand,
        boolean changed
) {
}
