package com.example.iis.dto;

public record LongestPhaseOrderResponse(
        String phaseName,
        Long rank,
        Long processId,
        Long offerId,
        Long customerId,
        String customerName,
        String customerEmail,
        String phaseStartTime,
        Long durationSeconds
) {
}
