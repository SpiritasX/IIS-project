package com.example.iis.service;

import com.example.iis.dto.CancelRequest;
import com.example.iis.dto.CreateOrderRequest;
import com.example.iis.dto.OrderItemRequest;
import com.example.iis.dto.StaffTransitionRequest;
import com.example.iis.model.CancellationReason;
import com.example.iis.model.Customer;
import com.example.iis.model.Offer;
import com.example.iis.model.OfferStatus;
import com.example.iis.model.OrderItem;
import com.example.iis.model.Phase;
import com.example.iis.model.PhaseType;
import com.example.iis.model.Plant;
import com.example.iis.model.PlantCategory;
import com.example.iis.model.PlantPrice;
import com.example.iis.model.PlantSpecies;
import com.example.iis.model.PlantType;
import com.example.iis.model.PlantVariety;
import com.example.iis.model.Process;
import com.example.iis.model.RelocationHistory;
import com.example.iis.model.StockReservation;
import com.example.iis.repository.CancellationReasonRepository;
import com.example.iis.repository.CustomerRepository;
import com.example.iis.repository.OfferRepository;
import com.example.iis.repository.OfferStatusRepository;
import com.example.iis.repository.PhaseTypeRepository;
import com.example.iis.repository.PlantPriceRepository;
import com.example.iis.repository.ProcessRepository;
import com.example.iis.repository.RelocationHistoryRepository;
import com.example.iis.repository.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    @Mock
    private CancellationReasonRepository cancellationReasonRepository;

    @Mock
    private RelocationHistoryRepository relocationHistoryRepository;

    @Mock
    private StockReservationRepository stockReservationRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                customerRepository,
                offerRepository,
                offerStatusRepository,
                phaseTypeRepository,
                plantPriceRepository,
                processRepository,
                cancellationReasonRepository,
                relocationHistoryRepository,
                stockReservationRepository
        );
    }

    @Test
    void createOrderCreatesOfferWithOfferedQuantitiesAndExpiry() {
        Customer customer = new Customer("user", "password", "User", "Customer", "user@example.com");
        PlantPrice plantPrice = plantPrice();
        RelocationHistoryRepository.PlantStockView stock = mock(RelocationHistoryRepository.PlantStockView.class);

        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer));
        when(plantPriceRepository.findById(10L)).thenReturn(Optional.of(plantPrice));
        when(relocationHistoryRepository.findActiveStockByPlantIds(List.of(5L))).thenReturn(List.of(stock));
        when(stock.getPlantId()).thenReturn(5L);
        when(stock.getAvailableQuantity()).thenReturn(2L);
        when(offerStatusRepository.findByName("Ponuda")).thenReturn(Optional.of(new OfferStatus("Ponuda")));
        when(phaseTypeRepository.findByName("Ponuda")).thenReturn(Optional.of(new PhaseType("Ponuda")));
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(processRepository.save(any(Process.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.createOrder(7L, new CreateOrderRequest(
                "Main Street 1",
                List.of(new OrderItemRequest(10L, 3))
        ));

        assertEquals("Main Street 1", customer.getAddress());
        assertEquals("Ponuda", response.status());
        assertEquals("Ponuda", response.currentPhase());
        assertNotNull(response.expiresAt());
        assertEquals(new BigDecimal("2000"), response.total());
        assertEquals(1, response.items().size());
        assertEquals(2, response.items().get(0).quantity());
        assertEquals(3, response.items().get(0).requestedQuantity());
        assertEquals(2, response.items().get(0).offeredQuantity());
        assertEquals(0, response.items().get(0).reservedQuantity());
        assertEquals(true, response.items().get(0).adjusted());
        verify(processRepository).save(any(Process.class));
    }

    @Test
    void createOrderRejectsFullyUnavailableInquiry() {
        Customer customer = new Customer("user", "password", "User", "Customer", "user@example.com");
        PlantPrice plantPrice = plantPrice();
        RelocationHistoryRepository.PlantStockView stock = mock(RelocationHistoryRepository.PlantStockView.class);

        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer));
        when(plantPriceRepository.findById(10L)).thenReturn(Optional.of(plantPrice));
        when(relocationHistoryRepository.findActiveStockByPlantIds(List.of(5L))).thenReturn(List.of(stock));
        when(stock.getPlantId()).thenReturn(5L);
        when(stock.getAvailableQuantity()).thenReturn(0L);
        when(offerStatusRepository.findByName("Ponuda")).thenReturn(Optional.of(new OfferStatus("Ponuda")));
        when(phaseTypeRepository.findByName("Ponuda")).thenReturn(Optional.of(new PhaseType("Ponuda")));

        assertThrows(
                ResponseStatusException.class,
                () -> orderService.createOrder(7L, new CreateOrderRequest(
                        "Main Street 1",
                        List.of(new OrderItemRequest(10L, 3))
                ))
        );
    }

    @Test
    void acceptOfferReservesStockAndStartsReservation() {
        Customer customer = new Customer("user", "password", "User", "Customer", "user@example.com");
        ReflectionTestUtils.setField(customer, "id", 7L);
        PlantPrice plantPrice = plantPrice();
        Offer offer = new Offer(new OfferStatus("Ponuda"), List.of(plantPrice));
        ReflectionTestUtils.setField(offer, "id", 20L);
        offer.setExpiresAt(new java.util.Date(System.currentTimeMillis() + 60_000));
        offer.addItem(new com.example.iis.model.OrderItem(plantPrice, 3, 3, 0));
        Process process = new Process(customer);
        ReflectionTestUtils.setField(process, "id", 30L);
        process.addPhase(new com.example.iis.model.Phase(process, new PhaseType("Ponuda"), offer));
        RelocationHistory history = new RelocationHistory("Initial stock", plantPrice.getPlant(), null, 5L);
        RelocationHistoryRepository.PlantStockView stock = mock(RelocationHistoryRepository.PlantStockView.class);

        when(offerRepository.findOrderForCustomer(20L, 7L)).thenReturn(Optional.of(offer));
        when(processRepository.findByOfferIdAndCustomerId(20L, 7L)).thenReturn(Optional.of(process));
        when(relocationHistoryRepository.findActiveStockByPlantIds(List.of(5L))).thenReturn(List.of(stock));
        when(stock.getPlantId()).thenReturn(5L);
        when(stock.getAvailableQuantity()).thenReturn(5L);
        when(relocationHistoryRepository.findByPlant_IdAndEndTimeIsNullOrderByStartTimeAsc(5L))
                .thenReturn(List.of(history));
        when(offerStatusRepository.findByName("Rezervacija"))
                .thenReturn(Optional.of(new OfferStatus("Rezervacija")));
        when(phaseTypeRepository.findByName("Rezervacija"))
                .thenReturn(Optional.of(new PhaseType("Rezervacija")));

        var response = orderService.acceptOffer(7L, 20L);

        assertEquals("Rezervacija", response.status());
        assertEquals("Rezervacija", response.currentPhase());
        assertEquals(3, response.items().get(0).quantity());
        assertEquals(3, response.items().get(0).reservedQuantity());
        assertEquals(2L, history.getInStock());
        verify(stockReservationRepository).save(any());
    }

    @Test
    void acceptOfferWithInsufficientStockReturnsConflictAndLeavesOfferUnchanged() {
        Customer customer = customer();
        PlantPrice plantPrice = plantPrice();
        Offer offer = offer("Ponuda", plantPrice, 3, 3, 0);
        Process process = process(customer, offer, "Ponuda");
        RelocationHistoryRepository.PlantStockView stock = mock(RelocationHistoryRepository.PlantStockView.class);

        when(offerRepository.findOrderForCustomer(20L, 7L)).thenReturn(Optional.of(offer));
        when(processRepository.findByOfferIdAndCustomerId(20L, 7L)).thenReturn(Optional.of(process));
        when(relocationHistoryRepository.findActiveStockByPlantIds(List.of(5L))).thenReturn(List.of(stock));
        when(stock.getPlantId()).thenReturn(5L);
        when(stock.getAvailableQuantity()).thenReturn(2L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.acceptOffer(7L, 20L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Ponuda", offer.getStatus().getName());
        assertEquals(0, offer.getItems().iterator().next().getQuantity());
        assertFalse(process.getPhases().get(0).getEndTime() != null);
        verify(stockReservationRepository, never()).save(any());
    }

    @Test
    void rejectOfferFinishesOfferAsRejected() {
        Customer customer = customer();
        PlantPrice plantPrice = plantPrice();
        Offer offer = offer("Ponuda", plantPrice, 3, 3, 0);
        Process process = process(customer, offer, "Ponuda");

        when(offerRepository.findOrderForCustomer(20L, 7L)).thenReturn(Optional.of(offer));
        when(processRepository.findByOfferIdAndCustomerId(20L, 7L)).thenReturn(Optional.of(process));
        when(offerStatusRepository.findByName("Odbijeno")).thenReturn(Optional.of(new OfferStatus("Odbijeno")));

        var response = orderService.rejectOffer(7L, 20L);

        assertEquals("Odbijeno", response.status());
        assertEquals(null, response.currentPhase());
        assertNotNull(process.getEndTime());
        assertNotNull(process.getPhases().get(0).getEndTime());
    }

    @Test
    void customerCancelReservationRestoresReservedStock() {
        Customer customer = customer();
        PlantPrice plantPrice = plantPrice();
        Offer offer = offer("Rezervacija", plantPrice, 3, 3, 3);
        Process process = process(customer, offer, "Rezervacija");
        OrderItem item = offer.getItems().iterator().next();
        RelocationHistory history = new RelocationHistory("Reserved stock", plantPrice.getPlant(), null, 2L);
        StockReservation reservation = new StockReservation(item, history, 3);

        when(offerRepository.findOrderForCustomer(20L, 7L)).thenReturn(Optional.of(offer));
        when(processRepository.findByOfferIdAndCustomerId(20L, 7L)).thenReturn(Optional.of(process));
        when(stockReservationRepository.findByOrderItem_Offer_Id(20L)).thenReturn(List.of(reservation));
        when(cancellationReasonRepository.findByName("Customer cancellation"))
                .thenReturn(Optional.of(new CancellationReason("Customer cancellation")));
        when(offerStatusRepository.findByName("Otkazano")).thenReturn(Optional.of(new OfferStatus("Otkazano")));

        var response = orderService.cancelOrder(7L, 20L, new CancelRequest("No longer needed"));

        assertEquals("Otkazano", response.status());
        assertEquals(null, response.currentPhase());
        assertEquals(5L, history.getInStock());
        assertEquals(0, item.getQuantity());
        assertEquals("No longer needed", process.getPhases().get(0).getCancellation().getReason());
        verify(stockReservationRepository).deleteByOrderItem_Offer_Id(20L);
    }

    @Test
    void staffTransitionsReservationThroughDeliveryCompletion() {
        Customer customer = customer();
        PlantPrice plantPrice = plantPrice();
        Offer offer = offer("Rezervacija", plantPrice, 3, 3, 3);
        Process process = process(customer, offer, "Rezervacija");
        stubWorkflowLookups();

        when(processRepository.findById(30L)).thenReturn(Optional.of(process));

        var ready = orderService.transitionStaffProcess(30L, new StaffTransitionRequest("MARK_READY"));
        assertEquals("Spremno", ready.status());
        assertEquals("Spremno", ready.currentPhase());
        assertEquals(List.of("START_DELIVERY", "CANCEL"), ready.allowedActions());

        var delivery = orderService.transitionStaffProcess(30L, new StaffTransitionRequest("START_DELIVERY"));
        assertEquals("Isporuka", delivery.status());
        assertEquals("Isporuka", delivery.currentPhase());
        assertEquals(List.of("COMPLETE_DELIVERY"), delivery.allowedActions());

        var delivered = orderService.transitionStaffProcess(30L, new StaffTransitionRequest("COMPLETE_DELIVERY"));
        assertEquals("Isporuceno", delivered.status());
        assertEquals(null, delivered.currentPhase());
        assertNotNull(process.getEndTime());
    }

    @Test
    void staffCancelReadyProcessRestoresReservedStock() {
        Customer customer = customer();
        PlantPrice plantPrice = plantPrice();
        Offer offer = offer("Spremno", plantPrice, 3, 3, 3);
        Process process = process(customer, offer, "Spremno");
        OrderItem item = offer.getItems().iterator().next();
        RelocationHistory history = new RelocationHistory("Reserved stock", plantPrice.getPlant(), null, 1L);
        StockReservation reservation = new StockReservation(item, history, 3);

        when(processRepository.findById(30L)).thenReturn(Optional.of(process));
        when(stockReservationRepository.findByOrderItem_Offer_Id(20L)).thenReturn(List.of(reservation));
        when(cancellationReasonRepository.findByName("Staff cancellation"))
                .thenReturn(Optional.of(new CancellationReason("Staff cancellation")));
        when(offerStatusRepository.findByName("Otkazano")).thenReturn(Optional.of(new OfferStatus("Otkazano")));

        var response = orderService.cancelStaffProcess(30L, new CancelRequest("Damaged plants"));

        assertEquals("Otkazano", response.status());
        assertEquals(null, response.currentPhase());
        assertEquals(4L, history.getInStock());
        assertEquals(0, item.getQuantity());
        assertEquals("Damaged plants", process.getPhases().get(0).getCancellation().getReason());
    }

    @Test
    void getOrderLazilyExpiresStaleOffer() {
        Customer customer = customer();
        PlantPrice plantPrice = plantPrice();
        Offer offer = offer("Ponuda", plantPrice, 3, 3, 0);
        offer.setExpiresAt(new Date(System.currentTimeMillis() - 60_000));
        Process process = process(customer, offer, "Ponuda");

        when(offerRepository.findOrderForCustomer(20L, 7L)).thenReturn(Optional.of(offer));
        when(processRepository.findByOfferIdAndCustomerId(20L, 7L)).thenReturn(Optional.of(process));
        when(offerStatusRepository.findByName("Isteklo")).thenReturn(Optional.of(new OfferStatus("Isteklo")));

        var response = orderService.getOrderForCustomer(7L, 20L);

        assertEquals("Isteklo", response.status());
        assertEquals(null, response.currentPhase());
        assertEquals(false, response.canAccept());
        assertNotNull(process.getEndTime());
    }

    private PlantPrice plantPrice() {
        PlantCategory category = new PlantCategory("Flowers");
        PlantType type = new PlantType("Flowering plants", category);
        PlantSpecies species = new PlantSpecies("Lavender", type);
        PlantVariety variety = new PlantVariety("English lavender", 45.0, "Soil", "Sun", species);
        Plant plant = new Plant("Lavender starter", "Hardy lavender", "Cuttings", "Available", variety);
        ReflectionTestUtils.setField(plant, "id", 5L);
        return new PlantPrice(new BigDecimal("1000"), plant);
    }

    private Customer customer() {
        Customer customer = new Customer("user", "password", "User", "Customer", "user@example.com");
        ReflectionTestUtils.setField(customer, "id", 7L);
        return customer;
    }

    private Offer offer(String statusName, PlantPrice plantPrice, int requested, int offered, int reserved) {
        Offer offer = new Offer(new OfferStatus(statusName), List.of(plantPrice));
        ReflectionTestUtils.setField(offer, "id", 20L);
        offer.setExpiresAt(new Date(System.currentTimeMillis() + 60_000));
        offer.addItem(new OrderItem(plantPrice, requested, offered, reserved));
        return offer;
    }

    private Process process(Customer customer, Offer offer, String phaseName) {
        Process process = new Process(customer);
        ReflectionTestUtils.setField(process, "id", 30L);
        process.addPhase(new Phase(process, new PhaseType(phaseName), offer));
        return process;
    }

    private void stubWorkflowLookups() {
        when(offerStatusRepository.findByName(anyString()))
                .thenAnswer(invocation -> Optional.of(new OfferStatus(invocation.getArgument(0))));
        when(phaseTypeRepository.findByName(anyString()))
                .thenAnswer(invocation -> Optional.of(new PhaseType(invocation.getArgument(0))));
    }
}
