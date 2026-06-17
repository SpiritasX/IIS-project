package com.example.iis.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long plantId,
        Long priceId,
        String name,
        Integer quantity,
        Integer requestedQuantity,
        Integer offeredQuantity,
        Integer reservedQuantity,
        boolean adjusted,
        BigDecimal price
) {
}
