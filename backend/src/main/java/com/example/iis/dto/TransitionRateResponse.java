package com.example.iis.dto;

public record TransitionRateResponse(
        String fromPhase,
        String toPhase,
        Long reachedCount,
        Long transitionedCount,
        Double ratePercent
) {
}
