package com.example.iis.dto;

public record PlantConditionLogEntry(
        Long id,
        Integer conditionState,
        String conditionDescription,
        String color,
        Double height,
        String changedAt,
        String changedBy
) {}
