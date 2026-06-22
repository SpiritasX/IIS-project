package com.example.iis.dto;

public record DashboardStatsResponse(
        long varietiesCount,
        long nurserySitesCount,
        long totalPlants,
        long activeRelocations
) {
}
