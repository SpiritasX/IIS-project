package com.example.iis.service;

import com.example.iis.dto.ProductResponse;
import com.example.iis.model.Plant;
import com.example.iis.model.PlantPrice;
import com.example.iis.repository.PlantPriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {
    private final PlantPriceRepository plantPriceRepository;

    public CatalogService(PlantPriceRepository plantPriceRepository) {
        this.plantPriceRepository = plantPriceRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts() {
        return plantPriceRepository.findActiveCatalogPrices()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductResponse toResponse(PlantPrice price) {
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
                isAvailable(plant)
        );
    }

    private boolean isAvailable(Plant plant) {
        String status = plant.getStatus();
        return status == null
                || (!status.equalsIgnoreCase("unavailable") && !status.equalsIgnoreCase("removed"));
    }
}
