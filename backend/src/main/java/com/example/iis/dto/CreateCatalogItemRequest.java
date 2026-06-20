package com.example.iis.dto;

import java.math.BigDecimal;

public record CreateCatalogItemRequest(
        String categoryName,
        String plantTypeName,
        String speciesName,
        String varietyName,
        Double humidity,
        String soil,
        String instructions,
        String season,
        String plantName,
        String description,
        String propagationMethod,
        String status,
        BigDecimal price
) {
}
