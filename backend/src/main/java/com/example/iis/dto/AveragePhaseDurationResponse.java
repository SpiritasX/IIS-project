package com.example.iis.dto;

public record AveragePhaseDurationResponse(
        String phaseName,
        Long averageDurationSeconds,
        Long sampleCount
) {
}
