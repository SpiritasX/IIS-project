package com.example.iis.dto;

import java.math.BigDecimal;

public record PlantOrderRecommendationItemResponse(
        Long id,
        Long priceId,
        String name,
        String description,
        String category,
        String type,
        String species,
        String variety,
        String season,
        BigDecimal price,
        Integer quantity,
        BigDecimal lineTotal
) {
}
