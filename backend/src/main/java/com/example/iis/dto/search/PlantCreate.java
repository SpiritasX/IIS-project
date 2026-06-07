package com.example.iis.dto.search;

import java.math.BigDecimal;
import java.util.Date;

public record PlantCreate(
        Long id,
        String name,
        String description,
        Long varietyId,
        String varietyName,
        Long speciesId,
        String speciesName,
        Long plantTypeId,
        String plantTypeName,
        BigDecimal price,
        Date createdAt
) {
}
