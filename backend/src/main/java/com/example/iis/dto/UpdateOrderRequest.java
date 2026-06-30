package com.example.iis.dto;

import java.util.List;

public record UpdateOrderRequest(
        List<OrderItemRequest> items
) {
}
