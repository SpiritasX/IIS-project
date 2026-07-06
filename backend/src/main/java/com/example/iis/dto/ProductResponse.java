package com.example.iis.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        Long priceId,
        String name,
        String description,
        String category,
        BigDecimal price,
        String status,
        Long availableQuantity,
        boolean available,
        boolean priceLocked
) {
}
