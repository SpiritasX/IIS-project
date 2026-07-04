package com.example.iis.dto;

public record UpdateVarietyRequest(
        String name,
        String latinName,
        Double humidity,
        String soil,
        String instructions,
        Long storageSpaceTypeId
) {
}
