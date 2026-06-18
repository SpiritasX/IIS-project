package com.example.iis.dto;

import java.math.BigDecimal;

public record PlantOrderRecommendationRequest(
        BigDecimal budget,
        String season,
        String category,
        String type,
        String species,
        String variety,
        Integer maxQuantityPerPlant
) {
}
