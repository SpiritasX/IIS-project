package com.example.iis.dto;

public record DeletionLogEntry(
        Long id,
        String plantName,
        String varietyName,
        String reason,
        String deletedAt,
        String deletedBy
) {}
