package com.example.iis.dto;

import java.time.LocalDate;

public record AddPlantLotRequest(
        Long varietyId,
        Long storageSpaceId,
        Long sectorId,
        Long nurserySiteId,
        Long quantity,
        String name,
        String propagationMethod,
        LocalDate hatchingDate,
        String color,
        Double height,
        String state
) {
}
