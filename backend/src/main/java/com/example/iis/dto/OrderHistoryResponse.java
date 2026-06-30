package com.example.iis.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderHistoryResponse(
        Long id,
        String changedAt,
        BigDecimal total,
        List<OrderHistoryItemResponse> items
) {
}
