package com.example.iis.dto;

public record UpdatePlantConditionRequest(
        Integer state,
        String conditionDescription,
        String color,
        Double height
) {}
