package com.example.iis.dto;

import java.math.BigDecimal;

public record OrderHistoryItemResponse(
        Long plantId,
        Long priceId,
        String name,
        Integer quantity,
        BigDecimal price
) {
}
