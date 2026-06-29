package com.example.iis.dto;

public record PhaseHistoryResponse(
        Long id,
        String name,
        String startTime,
        String endTime,
        Long durationSeconds,
        String cancellationReason,
        String cancellationDetail
) {
}
