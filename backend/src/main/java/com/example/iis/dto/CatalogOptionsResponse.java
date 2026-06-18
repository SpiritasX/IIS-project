package com.example.iis.dto;

import java.util.List;

public record CatalogOptionsResponse(
        List<String> seasons,
        List<String> categories,
        List<String> types,
        List<String> species,
        List<String> varieties
) {
}
