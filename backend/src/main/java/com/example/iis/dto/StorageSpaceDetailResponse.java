package com.example.iis.dto;

import java.util.List;

public record StorageSpaceDetailResponse(Long id, String name, String type, List<SectorDetailResponse> sectors) {}
