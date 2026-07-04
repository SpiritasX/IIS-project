package com.example.iis.dto;

public record ConditionLogEntry(
        Long plantId,
        String plantName,
        String varietyName,
        Integer conditionState,
        String conditionLabel,
        String description,
        String changedAt,
        String changedBy
) {}
