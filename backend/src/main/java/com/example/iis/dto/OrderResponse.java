package com.example.iis.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        String date,
        String status,
        BigDecimal total,
        List<OrderItemResponse> items
) {
}
