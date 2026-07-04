package com.example.iis.dto;

import java.time.LocalDate;

public record UpdatePlantRequest(
        String name,
        Long quantity,
        LocalDate hatchingDate,
        String color,
        Double height,
        Integer state,
        String conditionDescription
) {}
