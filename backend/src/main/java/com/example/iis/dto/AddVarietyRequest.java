package com.example.iis.dto;

public record AddVarietyRequest(
        String name,
        Double humidity,
        String soil,
        String instructions,
        Long speciesId,
        Long storageSpaceTypeId
) {
}
