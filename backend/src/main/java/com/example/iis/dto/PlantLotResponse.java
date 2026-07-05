package com.example.iis.dto;

import java.time.LocalDate;

public record PlantLotResponse(
        Long id,
        String name,
        String varietyName,
        String unitName,
        String parcelName,
        Long quantity,
        String lifecycleStage,
        LocalDate hatchingDate,
        String color,
        Double height,
        Integer state,
        String conditionDescription
) {
}
