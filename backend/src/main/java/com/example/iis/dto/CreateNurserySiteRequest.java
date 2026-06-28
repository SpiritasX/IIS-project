package com.example.iis.dto;

public record CreateNurserySiteRequest(
        String name,
        String address,
        String spaceName,
        String spaceType,
        String sectorName,
        Long sectorCapacity
) {}
