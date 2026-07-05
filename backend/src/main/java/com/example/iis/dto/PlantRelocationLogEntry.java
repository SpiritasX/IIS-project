package com.example.iis.dto;

public record PlantRelocationLogEntry(
        Long id,
        Long plantId,
        String plantName,
        String fromSectorName,
        String fromStorageSpaceName,
        String fromSiteName,
        String toSectorName,
        String toStorageSpaceName,
        String toSiteName,
        String state,
        String startedAt,
        String finishedAt,
        String initiatedBy
) {}
