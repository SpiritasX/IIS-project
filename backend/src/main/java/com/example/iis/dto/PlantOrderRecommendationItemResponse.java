package com.example.iis.dto;

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
        Integer price,
        Integer quantity,
        Integer lineTotal
) {
}
