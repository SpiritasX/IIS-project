package com.example.iis.dto;

import java.math.BigDecimal;
import java.util.List;

public record StaffProcessResponse(
        Long processId,
        Long offerId,
        Long customerId,
        String customerName,
        String customerEmail,
        String status,
        String currentPhase,
        String startTime,
        String endTime,
        String expiresAt,
        BigDecimal total,
        List<OrderItemResponse> items,
        List<PhaseHistoryResponse> phaseHistory,
        List<String> allowedActions
) {
}
