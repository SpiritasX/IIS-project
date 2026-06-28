package com.example.iis.dto;

public record WorkerStatsResponse(
        long varietiesCount,
        long storageSpacesCount,
        long plantsCount,
        long activeRelocations
) {
}
