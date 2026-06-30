package com.example.iis.dto;

import java.math.BigDecimal;
import java.util.Date;

public record NewPlantPriceDto(
        Long changedById,
        BigDecimal price
) {
}
