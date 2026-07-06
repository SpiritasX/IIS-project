package com.example.iis.service;

import com.example.iis.model.Plant;
import com.example.iis.model.PlantCategory;
import com.example.iis.model.PlantPrice;
import com.example.iis.model.PlantSpecies;
import com.example.iis.model.PlantType;
import com.example.iis.model.PlantVariety;
import com.example.iis.repository.OrderItemRepository;
import com.example.iis.repository.PlantPriceRepository;
import com.example.iis.repository.RelocationHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {
    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PlantPriceRepository plantPriceRepository;

    @Mock
    private RelocationHistoryRepository relocationHistoryRepository;

    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        catalogService = new CatalogService(orderItemRepository, plantPriceRepository, relocationHistoryRepository);
    }

    @Test
    void productResponseUsesPlantTaxonomyAndActivePrice() {
        PlantCategory category = new PlantCategory("Herbs");
        PlantType type = new PlantType("Culinary herbs", category);
        PlantSpecies species = new PlantSpecies("Basil", type);
        PlantVariety variety = new PlantVariety("Genovese basil", 60.0, "Rich soil", "Keep warm", species);
        Plant plant = new Plant("Basil seedling", "Fresh basil", "Seed", "Available", variety);
        ReflectionTestUtils.setField(plant, "id", 5L);
        PlantPrice price = new PlantPrice(new BigDecimal("750"), plant);
        RelocationHistoryRepository.PlantStockView stock = mock(RelocationHistoryRepository.PlantStockView.class);
        when(plantPriceRepository.findActiveCatalogPrices()).thenReturn(List.of(price));
        when(stock.getPlantId()).thenReturn(5L);
        when(stock.getAvailableQuantity()).thenReturn(12L);
        when(relocationHistoryRepository.findActiveStockByPlantIds(List.of(5L))).thenReturn(List.of(stock));

        var products = catalogService.getProducts(null);

        assertEquals(1, products.size());
        assertEquals("Basil seedling", products.get(0).name());
        assertEquals("Herbs", products.get(0).category());
        assertEquals(new BigDecimal("750"), products.get(0).price());
        assertEquals(12L, products.get(0).availableQuantity());
        assertTrue(products.get(0).available());
    }
}
