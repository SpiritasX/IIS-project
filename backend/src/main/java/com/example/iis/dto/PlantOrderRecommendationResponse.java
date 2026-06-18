package com.example.iis.dto;

import java.math.BigDecimal;
import java.util.List;

public record PlantOrderRecommendationResponse(
        BigDecimal budget,
        BigDecimal total,
        BigDecimal remainingBudget,
        Integer itemCount,
        List<PlantOrderRecommendationItemResponse> items
) {
}
