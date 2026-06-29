package com.example.iis.dto;

import java.util.Date;

public record RelocationLogEntry(
        Date startTime,
        Date endTime,
        String plantName,
        String parcelName,
        String reason,
        Long inStock
) {
}
