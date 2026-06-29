package com.example.iis.dto;

import java.util.List;

public record OrderAnalysisResponse(
        List<PhaseCountResponse> phaseCounts,
        List<TransitionRateResponse> transitionRates,
        List<AveragePhaseDurationResponse> averageDurations,
        List<LongestPhaseOrderResponse> longestActiveOrders
) {
}
