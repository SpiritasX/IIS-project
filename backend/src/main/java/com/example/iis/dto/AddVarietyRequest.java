package com.example.iis.dto;

public record AddVarietyRequest(
        String name,
        String latinName,
        Double humidity,
        String soil,
        String instructions,
        Long speciesId,
        Long storageSpaceTypeId
) {
}
