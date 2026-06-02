package com.example.iis.service;

import com.example.iis.dto.CreateOrderRequest;
import com.example.iis.dto.OrderItemRequest;
import com.example.iis.model.Customer;
import com.example.iis.model.Offer;
import com.example.iis.model.OfferStatus;
import com.example.iis.model.PhaseType;
import com.example.iis.model.Plant;
import com.example.iis.model.PlantCategory;
import com.example.iis.model.PlantPrice;
import com.example.iis.model.PlantSpecies;
import com.example.iis.model.PlantType;
import com.example.iis.model.PlantVariety;
import com.example.iis.model.Process;
import com.example.iis.repository.CustomerRepository;
import com.example.iis.repository.OfferRepository;
import com.example.iis.repository.OfferStatusRepository;
import com.example.iis.repository.PhaseTypeRepository;
import com.example.iis.repository.PlantPriceRepository;
import com.example.iis.repository.ProcessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private OfferStatusRepository offerStatusRepository;

    @Mock
    private PhaseTypeRepository phaseTypeRepository;

    @Mock
    private PlantPriceRepository plantPriceRepository;

    @Mock
    private ProcessRepository processRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                customerRepository,
                offerRepository,
                offerStatusRepository,
                phaseTypeRepository,
                plantPriceRepository,
                processRepository
        );
    }

    @Test
    void createOrderPersistsQuantitiesAndReturnsTotal() {
        Customer customer = new Customer("user", "password", "User", "Customer", "user@example.com");
        PlantPrice plantPrice = plantPrice();

        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer));
        when(plantPriceRepository.findById(10L)).thenReturn(Optional.of(plantPrice));
        when(offerStatusRepository.findByName("Pending")).thenReturn(Optional.of(new OfferStatus("Pending")));
        when(phaseTypeRepository.findByName("Order placed")).thenReturn(Optional.of(new PhaseType("Order placed")));
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(processRepository.save(any(Process.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.createOrder(7L, new CreateOrderRequest(
                "Main Street 1",
                List.of(new OrderItemRequest(10L, 3))
        ));

        assertEquals("Main Street 1", customer.getAddress());
        assertEquals(new BigDecimal("3000"), response.total());
        assertEquals(1, response.items().size());
        assertEquals(3, response.items().get(0).quantity());
        verify(processRepository).save(any(Process.class));
    }

    private PlantPrice plantPrice() {
        PlantCategory category = new PlantCategory("Flowers");
        PlantType type = new PlantType("Flowering plants", category);
        PlantSpecies species = new PlantSpecies("Lavender", type);
        PlantVariety variety = new PlantVariety("English lavender", 45.0, "Soil", "Sun", species);
        Plant plant = new Plant("Lavender starter", "Hardy lavender", "Cuttings", "Available", variety);
        return new PlantPrice(new BigDecimal("1000"), plant);
    }
}
