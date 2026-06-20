package com.example.iis.dto;

public record PlantOrderRecommendationRequest(
        Integer budget,
        String season,
        String category,
        String type,
        String species,
        String variety,
        Integer maxQuantityPerPlant
) {
}
