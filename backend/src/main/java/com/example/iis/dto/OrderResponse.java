package com.example.iis.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        String date,
        String status,
        BigDecimal total,
        List<OrderItemResponse> items,
        Long processId,
        String currentPhase,
        String expiresAt,
        List<PhaseHistoryResponse> phaseHistory,
        List<OrderHistoryResponse> orderHistory,
        boolean canAccept,
        boolean canReject,
        boolean canCancel
) {
}
