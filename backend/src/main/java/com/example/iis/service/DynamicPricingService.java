package com.example.iis.service;

import com.example.iis.dto.DynamicPriceUpdateResponse;
import com.example.iis.model.PlantPrice;
import com.example.iis.repository.OrderHistoryItemRepository;
import com.example.iis.repository.PlantPriceRepository;
import com.example.iis.repository.RelocationHistoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DynamicPricingService {
    private static final BigDecimal MIN_PRICE = BigDecimal.ONE;
    private static final List<String> DEMAND_STATUSES = List.of(
            "Rezervacija",
            "Spremno",
            "Isporuka",
            "Isporuceno"
    );

    private final PlantPriceRepository plantPriceRepository;
    private final RelocationHistoryRepository relocationHistoryRepository;
    private final OrderHistoryItemRepository orderHistoryItemRepository;
    private final PlantPriceService plantPriceService;

    public DynamicPricingService(PlantPriceRepository plantPriceRepository, RelocationHistoryRepository relocationHistoryRepository, OrderHistoryItemRepository orderHistoryItemRepository, PlantPriceService plantPriceService) {
        this.plantPriceRepository = plantPriceRepository;
        this.relocationHistoryRepository = relocationHistoryRepository;
        this.orderHistoryItemRepository = orderHistoryItemRepository;
        this.plantPriceService = plantPriceService;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Belgrade")
    public void recalculateScheduledPrices() {
        recalculateAllPlantPrices();
    }

    public List<DynamicPriceUpdateResponse> recalculateAllPlantPrices() {
        Date demandFromDate = Date.from(Instant.now().minus(Duration.ofDays(30)));
        List<DynamicPriceUpdateResponse> responses = new ArrayList<>();

        for (PlantPrice activePrice : plantPriceRepository.findActiveCatalogPrices()) {
            Long plantId = activePrice.getPlant().getId();
            long stock = valueOrZero(relocationHistoryRepository.sumActiveStockByPlantId(plantId));
            long demand = valueOrZero(orderHistoryItemRepository.sumDemandForPlantSince(plantId, demandFromDate, DEMAND_STATUSES));

            BigDecimal oldPrice = activePrice.getPrice();
            BigDecimal newPrice = calculatePrice(oldPrice, stock, demand);
            boolean changed = newPrice.compareTo(oldPrice) != 0;

            if (changed) {
                plantPriceService.setNewPlantPrice(plantId, newPrice, null);
            }

            responses.add(new DynamicPriceUpdateResponse(
                    plantId,
                    activePrice.getPlant().getName(),
                    oldPrice,
                    newPrice,
                    stock,
                    demand,
                    changed
            ));
        }

        return responses;
    }

    private BigDecimal calculatePrice(BigDecimal currentPrice, long stock, long demand) {
        BigDecimal multiplier = BigDecimal.ONE
                .add(stockAdjustment(stock))
                .add(demandAdjustment(demand));

        BigDecimal calculatedPrice = currentPrice.multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
        return calculatedPrice.max(MIN_PRICE);
    }

    private BigDecimal stockAdjustment(long stock) {
        if (stock <= 5) {
            return new BigDecimal("0.15");
        }
        if (stock <= 20) {
            return new BigDecimal("0.07");
        }
        if (stock >= 100) {
            return new BigDecimal("-0.10");
        }
        if (stock >= 50) {
            return new BigDecimal("-0.05");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal demandAdjustment(long demand) {
        if (demand >= 20) {
            return new BigDecimal("0.15");
        }
        if (demand >= 10) {
            return new BigDecimal("0.08");
        }
        if (demand <= 2) {
            return new BigDecimal("-0.07");
        }
        if (demand <= 5) {
            return new BigDecimal("-0.03");
        }
        return BigDecimal.ZERO;
    }

    private long valueOrZero(Long value) {
        return value != null ? value : 0L;
    }
}
