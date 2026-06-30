package com.example.iis.dto;

import com.example.iis.model.Account;
import com.example.iis.model.PlantPrice;

import java.math.BigDecimal;
import java.util.Date;

public record PlantPriceResponse(
        Long id,
        Long plantId,
        String plantName,
        BigDecimal price,
        Date startTime,
        Date endTime,
        Long changedById,
        String changedByUsername
) {
    public static PlantPriceResponse from(PlantPrice plantPrice) {
        Account changedBy = plantPrice.getChangedBy();

        return new PlantPriceResponse(
                plantPrice.getId(),
                plantPrice.getPlant().getId(),
                plantPrice.getPlant().getName(),
                plantPrice.getPrice(),
                plantPrice.getStartTime(),
                plantPrice.getEndTime(),
                changedBy != null ? changedBy.getId() : null,
                changedBy != null ? changedBy.getUsername() : null
        );
    }
}
