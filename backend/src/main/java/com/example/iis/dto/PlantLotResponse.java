package com.example.iis.dto;

import java.time.LocalDate;

public record PlantLotResponse(
        Long id,
        String name,
        String varietyName,
        String unitName,
        String parcelName,
        Long quantity,
        String propagationMethod,
        LocalDate hatchingDate,
        String color,
        Double height,
        String state
) {
}
