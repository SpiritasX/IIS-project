package com.example.iis.dto;

public record PlantHealthLogEntry(
        Long id,
        String lifeStage,
        String healthGrade,
        String description,
        String changedAt,
        String changedBy
) {}
