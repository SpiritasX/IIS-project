package com.example.iis.dto;

public record VarietyResponse(
        Long id,
        String name,
        String latinName,
        Double humidity,
        String soil,
        String instructions,
        Long speciesId,
        String speciesName,
        Long typeId,
        String typeName,
        Long categoryId,
        String categoryName,
        Long storageSpaceTypeId,
        String storageSpaceTypeName
) {
}
