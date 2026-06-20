package com.example.iis.dto;

import java.util.List;

public record PlantOrderRecommendationResponse(
        Integer budget,
        Integer total,
        Integer remainingBudget,
        Integer itemCount,
        List<PlantOrderRecommendationItemResponse> items
) {
}
