package com.example.iis.dto;

import java.time.LocalDate;

public record PlantDetailResponse(
        Long id,
        String name,
        String description,
        Integer state,
        String conditionDescription,
        String lifecycleStage,
        LocalDate hatchingDate,
        String color,
        Double height,
        Long varietyId,
        String varietyName,
        String latinName,
        Double humidity,
        String soil,
        String careInstructions,
        String storageSpaceTypeName,
        String speciesName,
        String typeName,
        String categoryName,
        String sectorName,
        String storageSpaceName,
        String siteName,
        Long currentQuantity
) {}
