package com.example.iis.dto;

public record OrderItemRequest(
        Long plantPriceId,
        Integer quantity
) {
}
