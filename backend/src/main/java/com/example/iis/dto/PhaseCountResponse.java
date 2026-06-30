package com.example.iis.dto;

public record PhaseCountResponse(
        String phaseName,
        Long orderCount
) {
}
