package com.example.iis.service;

import com.example.iis.dto.ProductResponse;
import com.example.iis.model.Plant;
import com.example.iis.model.PlantPrice;
import com.example.iis.repository.OrderItemRepository;
import com.example.iis.repository.PlantPriceRepository;
import com.example.iis.repository.RelocationHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CatalogService {
    private static final List<String> PRICE_LOCK_STATUSES = List.of(
            "Rezervacija",
            "Spremno",
            "Isporuka"
    );

    private final OrderItemRepository orderItemRepository;
    private final PlantPriceRepository plantPriceRepository;
    private final RelocationHistoryRepository relocationHistoryRepository;

    public CatalogService(
            OrderItemRepository orderItemRepository,
            PlantPriceRepository plantPriceRepository,
            RelocationHistoryRepository relocationHistoryRepository
    ) {
        this.orderItemRepository = orderItemRepository;
        this.plantPriceRepository = plantPriceRepository;
        this.relocationHistoryRepository = relocationHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts(Long customerId) {
        List<PlantPrice> prices = plantPriceRepository.findActiveCatalogPrices();
        Map<Long, Long> stockByPlantId = stockByPlantId(prices);

        return prices.stream()
                .map(price -> {
                    Optional<PlantPrice> lockedPrice = lockedPriceForCustomerAndPlant(
                            customerId,
                            price.getPlant().getId()
                    );
                    PlantPrice effectivePrice = lockedPrice.orElse(price);

                    return toResponse(
                            effectivePrice,
                            stockByPlantId.getOrDefault(price.getPlant().getId(), 0L),
                            lockedPrice.isPresent()
                    );
                })
                .toList();
    }

    private Map<Long, Long> stockByPlantId(List<PlantPrice> prices) {
        List<Long> plantIds = prices.stream()
                .map(price -> price.getPlant().getId())
                .toList();

        if (plantIds.isEmpty()) {
            return Map.of();
        }

        return relocationHistoryRepository.findActiveStockByPlantIds(plantIds)
                .stream()
                .collect(Collectors.toMap(
                        RelocationHistoryRepository.PlantStockView::getPlantId,
                        RelocationHistoryRepository.PlantStockView::getAvailableQuantity,
                        Long::sum
                ));
    }

    private ProductResponse toResponse(PlantPrice price, Long availableQuantity, boolean priceLocked) {
        Plant plant = price.getPlant();
        String category = plant.getVariety()
                .getSpecies()
                .getType()
                .getCategory()
                .getName();

        return new ProductResponse(
                plant.getId(),
                price.getId(),
                plant.getName(),
                plant.getDescription(),
                category,
                price.getPrice(),
                plant.getStatus(),
                availableQuantity,
                isAvailable(plant, availableQuantity),
                priceLocked
        );
    }

    private Optional<PlantPrice> lockedPriceForCustomerAndPlant(Long customerId, Long plantId) {
        if (customerId == null) {
            return Optional.empty();
        }

        return orderItemRepository.findLockedPricesForCustomerAndPlant(
                        customerId,
                        plantId,
                        PRICE_LOCK_STATUSES
                )
                .stream()
                .findFirst();
    }

    private boolean isAvailable(Plant plant, Long availableQuantity) {
        String status = plant.getStatus();
        boolean statusAllowsSale = status == null
                || (!status.equalsIgnoreCase("unavailable") && !status.equalsIgnoreCase("removed"));

        return statusAllowsSale && availableQuantity != null && availableQuantity > 0;
    }
}
