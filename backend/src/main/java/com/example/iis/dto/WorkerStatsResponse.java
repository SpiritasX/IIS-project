package com.example.iis.dto;

public record WorkerStatsResponse(
        long varietiesCount,
        long locationUnitsCount,
        long plantsCount,
        long activeRelocations
) {
}
