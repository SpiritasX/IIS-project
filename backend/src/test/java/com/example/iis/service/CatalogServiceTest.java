package com.example.iis.service;

import com.example.iis.model.Plant;
import com.example.iis.model.PlantCategory;
import com.example.iis.model.PlantPrice;
import com.example.iis.model.PlantSpecies;
import com.example.iis.model.PlantType;
import com.example.iis.model.PlantVariety;
import com.example.iis.repository.PlantPriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {
    @Mock
    private PlantPriceRepository plantPriceRepository;

    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        catalogService = new CatalogService(plantPriceRepository);
    }

    @Test
    void productResponseUsesPlantTaxonomyAndActivePrice() {
        PlantCategory category = new PlantCategory("Herbs");
        PlantType type = new PlantType("Culinary herbs", category);
        PlantSpecies species = new PlantSpecies("Basil", type);
        PlantVariety variety = new PlantVariety("Genovese basil", 60.0, "Rich soil", "Keep warm", "SUMMER", species);
        Plant plant = new Plant("Basil seedling", "Fresh basil", "Seed", "Available", variety);
        PlantPrice price = new PlantPrice(750.0, plant);
        when(plantPriceRepository.findActiveCatalogPrices()).thenReturn(List.of(price));

        var products = catalogService.getProducts();

        assertEquals(1, products.size());
        assertEquals("Basil seedling", products.get(0).name());
        assertEquals("Herbs", products.get(0).category());
        assertEquals("Culinary herbs", products.get(0).type());
        assertEquals("Basil", products.get(0).species());
        assertEquals("Genovese basil", products.get(0).variety());
        assertEquals("SUMMER", products.get(0).season());
        assertEquals(750, products.get(0).price());
        assertTrue(products.get(0).available());
    }

    @Test
    void catalogOptionsUseActivePlantTaxonomy() {
        when(plantPriceRepository.findActiveCatalogPrices()).thenReturn(List.of(
                plantPrice("Lavender starter", "Flowers", "Flowering plants", "Lavender", "English lavender", "SUMMER", 1000),
                plantPrice("Basil seedling", "Herbs", "Culinary herbs", "Basil", "Genovese basil", "WINTER", 750)
        ));

        var options = catalogService.getCatalogOptions();

        assertEquals(List.of("Flowers", "Herbs"), options.categories());
        assertEquals(List.of("SUMMER", "WINTER"), options.seasons());
        assertEquals(List.of("Basil", "Lavender"), options.species());
    }

    @Test
    void recommendOrderFitsBudgetAndMatchingFilters() {
        when(plantPriceRepository.findActiveCatalogPrices()).thenReturn(List.of(
                plantPrice("Lavender starter", "Flowers", "Flowering plants", "Lavender", "English lavender", "SUMMER", 1000),
                plantPrice("Rose bush", "Flowers", "Flowering plants", "Rose", "Garden rose", "SUMMER", 1400),
                plantPrice("Basil seedling", "Herbs", "Culinary herbs", "Basil", "Genovese basil", "WINTER", 750)
        ));

        var response = catalogService.recommendOrder(new com.example.iis.dto.PlantOrderRecommendationRequest(
                2400,
                "SUMMER",
                "Flowers",
                null,
                null,
                null,
                2
        ));

        assertEquals(2400, response.total());
        assertEquals(0, response.remainingBudget());
        assertEquals(2, response.itemCount());
        assertEquals(List.of("Lavender starter", "Rose bush"), response.items().stream().map(item -> item.name()).toList());
    }

    @Test
    void recommendOrderCanUseMultipleQuantitiesOfOnePlant() {
        when(plantPriceRepository.findActiveCatalogPrices()).thenReturn(List.of(
                plantPrice("Lavender starter", "Flowers", "Flowering plants", "Lavender", "English lavender", "SUMMER", 1000)
        ));

        var response = catalogService.recommendOrder(new com.example.iis.dto.PlantOrderRecommendationRequest(
                2500,
                null,
                null,
                null,
                null,
                null,
                3
        ));

        assertEquals(2000, response.total());
        assertEquals(500, response.remainingBudget());
        assertEquals(2, response.itemCount());
        assertEquals(2, response.items().get(0).quantity());
    }

    private PlantPrice plantPrice(
            String plantName,
            String categoryName,
            String typeName,
            String speciesName,
            String varietyName,
            String season,
            Integer price
    ) {
        PlantCategory category = new PlantCategory(categoryName);
        PlantType type = new PlantType(typeName, category);
        PlantSpecies species = new PlantSpecies(speciesName, type);
        PlantVariety variety = new PlantVariety(varietyName, 60.0, "Soil", "Care", season, species);
        Plant plant = new Plant(plantName, plantName + " description", "Seed", "Available", variety);
        return new PlantPrice(price.doubleValue(), plant);
    }
}
