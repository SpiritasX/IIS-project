package com.example.iis.dto;

public record PlantRelocationEntry(
        Long id,
        String reason,
        String sectorName,
        String storageSpaceName,
        String siteName,
        Long inStock,
        String startTime,
        String endTime,
        String movedBy
) {}
