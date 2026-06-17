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

import java.math.BigDecimal;
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
        PlantPrice price = new PlantPrice(new BigDecimal("750"), plant);
        when(plantPriceRepository.findActiveCatalogPrices()).thenReturn(List.of(price));

        var products = catalogService.getProducts();

        assertEquals(1, products.size());
        assertEquals("Basil seedling", products.get(0).name());
        assertEquals("Herbs", products.get(0).category());
        assertEquals(new BigDecimal("750"), products.get(0).price());
        assertTrue(products.get(0).available());
    }
}
