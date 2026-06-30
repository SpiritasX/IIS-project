package com.example.iis.dto;

import java.util.List;

public record CreateOrderRequest(
        String deliveryAddress,
        List<OrderItemRequest> items,
        Boolean autoAcceptIfUnchanged
) {
}
