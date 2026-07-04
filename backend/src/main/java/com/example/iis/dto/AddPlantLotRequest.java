package com.example.iis.dto;

import java.time.LocalDate;

public record AddPlantLotRequest(
        Long varietyId,
        Long storageSpaceId,
        Long sectorId,
        Long quantity,
        String name,
        String lifecycleStage,
        LocalDate hatchingDate,
        String color,
        Double height,
        Integer state,
        String conditionDescription
) {
}
