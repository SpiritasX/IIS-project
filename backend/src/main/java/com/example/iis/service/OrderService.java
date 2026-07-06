package com.example.iis.service;

import com.example.iis.dto.CancelRequest;
import com.example.iis.dto.CreateOrderRequest;
import com.example.iis.dto.OrderHistoryItemResponse;
import com.example.iis.dto.OrderHistoryResponse;
import com.example.iis.dto.OrderItemRequest;
import com.example.iis.dto.OrderItemResponse;
import com.example.iis.dto.OrderResponse;
import com.example.iis.dto.PhaseHistoryResponse;
import com.example.iis.dto.StaffProcessResponse;
import com.example.iis.dto.StaffTransitionRequest;
import com.example.iis.dto.UpdateOrderRequest;
import com.example.iis.model.Cancellation;
import com.example.iis.model.CancellationReason;
import com.example.iis.model.Customer;
import com.example.iis.model.Offer;
import com.example.iis.model.OfferStatus;
import com.example.iis.model.OrderHistory;
import com.example.iis.model.OrderHistoryItem;
import com.example.iis.model.OrderItem;
import com.example.iis.model.Phase;
import com.example.iis.model.PhaseType;
import com.example.iis.model.Plant;
import com.example.iis.model.PlantPrice;
import com.example.iis.model.Process;
import com.example.iis.model.RelocationHistory;
import com.example.iis.model.StockReservation;
import com.example.iis.repository.CancellationReasonRepository;
import com.example.iis.repository.CustomerRepository;
import com.example.iis.repository.OfferRepository;
import com.example.iis.repository.OfferStatusRepository;
import com.example.iis.repository.OrderItemRepository;
import com.example.iis.repository.PhaseTypeRepository;
import com.example.iis.repository.PlantPriceRepository;
import com.example.iis.repository.ProcessRepository;
import com.example.iis.repository.RelocationHistoryRepository;
import com.example.iis.repository.StockReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final String STATUS_OFFER = "Ponuda";
    private static final String STATUS_RESERVATION = "Rezervacija";
    private static final String STATUS_READY = "Spremno";
    private static final String STATUS_DELIVERY = "Isporuka";
    private static final String STATUS_DELIVERED = "Isporuceno";
    private static final String STATUS_REJECTED = "Odbijeno";
    private static final String STATUS_EXPIRED = "Isteklo";
    private static final String STATUS_CANCELLED = "Otkazano";

    private static final String PHASE_OFFER = "Ponuda";
    private static final String PHASE_RESERVATION = "Rezervacija";
    private static final String PHASE_READY = "Spremno";
    private static final String PHASE_DELIVERY = "Isporuka";
    private static final List<String> PRICE_LOCK_STATUSES = List.of(
            STATUS_RESERVATION,
            STATUS_READY,
            STATUS_DELIVERY
    );

    private static final String CUSTOMER_CANCELLATION_REASON = "Customer cancellation";
    private static final String STAFF_CANCELLATION_REASON = "Staff cancellation";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_WORKER = "WORKER";

    private final CancellationReasonRepository cancellationReasonRepository;
    private final CustomerRepository customerRepository;
    private final OfferRepository offerRepository;
    private final OfferStatusRepository offerStatusRepository;
    private final OrderItemRepository orderItemRepository;
    private final PhaseTypeRepository phaseTypeRepository;
    private final PlantPriceRepository plantPriceRepository;
    private final ProcessRepository processRepository;
    private final RelocationHistoryRepository relocationHistoryRepository;
    private final StockReservationRepository stockReservationRepository;

    public OrderService(
            CustomerRepository customerRepository,
            OfferRepository offerRepository,
            OfferStatusRepository offerStatusRepository,
            OrderItemRepository orderItemRepository,
            PhaseTypeRepository phaseTypeRepository,
            PlantPriceRepository plantPriceRepository,
            ProcessRepository processRepository,
            CancellationReasonRepository cancellationReasonRepository,
            RelocationHistoryRepository relocationHistoryRepository,
            StockReservationRepository stockReservationRepository
    ) {
        this.customerRepository = customerRepository;
        this.offerRepository = offerRepository;
        this.offerStatusRepository = offerStatusRepository;
        this.orderItemRepository = orderItemRepository;
        this.phaseTypeRepository = phaseTypeRepository;
        this.plantPriceRepository = plantPriceRepository;
        this.processRepository = processRepository;
        this.cancellationReasonRepository = cancellationReasonRepository;
        this.relocationHistoryRepository = relocationHistoryRepository;
        this.stockReservationRepository = stockReservationRepository;
    }

    @Transactional
    public OrderResponse createOrder(Long customerId, CreateOrderRequest request) {
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer is required");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        Map<Long, Integer> requestedQuantities = normalizeItems(request.items());
        Map<Long, PlantPrice> pricesById = loadPrices(customerId, requestedQuantities.keySet());
        Map<Long, Long> stockByPlantId = stockByPlantId(pricesById.values());

        String deliveryAddress = request.deliveryAddress();
        if (deliveryAddress != null && !deliveryAddress.trim().isEmpty()) {
            customer.setAddress(deliveryAddress.trim());
            customerRepository.save(customer);
        }

        OfferStatus offerStatus = status(STATUS_OFFER);
        PhaseType offerPhaseType = phaseType(PHASE_OFFER);
        Date now = new Date(System.currentTimeMillis());
        Offer offer = new Offer(offerStatus, List.of());
        offer.setExpiresAt(new Date(now.getTime() + Duration.ofHours(24).toMillis()));

        requestedQuantities.forEach((priceId, requestedQuantity) -> {
            PlantPrice price = pricesById.get(priceId);
            long availableQuantity = stockByPlantId.getOrDefault(price.getPlant().getId(), 0L);
            int offeredQuantity = (int) Math.min(requestedQuantity, availableQuantity);
            if (offeredQuantity > 0) {
                offer.addItem(new OrderItem(price, requestedQuantity, offeredQuantity, 0));
            }
        });

        if (offer.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No requested plants are available");
        }

        Offer savedOffer = offerRepository.save(offer);
        Process process = new Process(customer);
        process.addPhase(new Phase(process, offerPhaseType, savedOffer));
        processRepository.save(process);

        // ako je request u potpunosti ispunjen, automatski prihvati ponudu.
        if (Boolean.TRUE.equals(request.autoAcceptIfUnchanged()) && offerMatchesRequest(savedOffer, requestedQuantities, pricesById)) {
            reserveStock(savedOffer);
            finishCurrentPhase(process);
            savedOffer.setStatus(status(STATUS_RESERVATION));
            process.addPhase(new Phase(process, phaseType(PHASE_RESERVATION), savedOffer));
            addOrderHistorySnapshot(savedOffer, currentPhase(process).getStartTime());
        }

        return toResponse(savedOffer, process);
    }

    @Transactional
    public List<OrderResponse> getOrdersForCustomer(Long customerId) {
        List<OrderResponse> responses = new ArrayList<>();

        for (Offer offer : offerRepository.findOrdersForCustomer(customerId)) {
            Process process = processForCustomerOffer(customerId, offer.getId());
            expireIfNeeded(offer, process);
            responses.add(toResponse(offer, process));
        }

        return responses;
    }

    @Transactional
    public OrderResponse getOrderForCustomer(Long customerId, Long orderId) {

        Offer offer = offerRepository.findOrderForCustomer(orderId, customerId).orElseThrow(() -> new ResponseStatusException((HttpStatus.NOT_FOUND), "Order not found"));
        Process process = processForCustomerOffer(customerId,orderId);
        expireIfNeeded(offer, process);
        return toResponse(offer, process);
    }

    @Transactional
    public OrderResponse updateOrder(Long customerId, Long orderId, UpdateOrderRequest request) {
        Offer offer = customerOffer(customerId, orderId);
        Process process = processForCustomerOffer(customerId, orderId);
        requireCurrentPhase(process, PHASE_RESERVATION);

        Map<Long, Integer> updatedQuantities = normalizeItems(request == null ? null : request.items());
        Map<Long, PlantPrice> pricesById = loadEditPrices(customerId, offer, updatedQuantities.keySet());
        Map<Long, Integer> currentQuantities = currentQuantitiesByPlantId(offer);
        Map<Long, Integer> updatedQuantitiesByPlantId = quantitiesByPlantId(updatedQuantities, pricesById);

        if (currentQuantities.equals(updatedQuantitiesByPlantId)) {
            return toResponse(offer, process);
        }

        restoreReservedStock(offer);
        offer.getItems().clear();

        updatedQuantities.forEach((priceId, quantity) ->
                offer.addItem(new OrderItem(pricesById.get(priceId), quantity, quantity, 0))
        );

        reserveStock(offer);
        addOrderHistorySnapshot(offer, new Date(System.currentTimeMillis()));

        return toResponse(offer, process);
    }

    @Transactional
    public OrderResponse acceptOffer(Long customerId, Long offerId) {
        Offer offer = customerOffer(customerId, offerId);
        Process process = processForCustomerOffer(customerId, offerId);
        expireIfNeeded(offer, process);
        requireCurrentPhase(process, PHASE_OFFER);


        verifyStockIsStillAvailable(offer);
        reserveStock(offer);
        finishCurrentPhase(process);
        offer.setStatus(status(STATUS_RESERVATION));
        process.addPhase(new Phase(process, phaseType(PHASE_RESERVATION), offer));
        addOrderHistorySnapshot(offer, currentPhase(process).getStartTime());

        return toResponse(offer, process);
    }

    @Transactional
    public OrderResponse rejectOffer(Long customerId, Long offerId) {
        Offer offer = customerOffer(customerId, offerId);
        Process process = processForCustomerOffer(customerId, offerId);
        expireIfNeeded(offer, process);
        requireCurrentPhase(process, PHASE_OFFER);

        offer.setStatus(status(STATUS_REJECTED));
        finishCurrentPhase(process);
        process.finish();

        return toResponse(offer, process);
    }

    @Transactional
    public OrderResponse cancelOrder(Long customerId, Long offerId, CancelRequest request) {
        Offer offer = customerOffer(customerId, offerId);
        Process process = processForCustomerOffer(customerId, offerId);
        expireIfNeeded(offer, process);

        String currentPhase = currentPhaseName(process);
        if (currentPhase == null || !Set.of(PHASE_OFFER, PHASE_RESERVATION).contains(currentPhase)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Process cannot be cancelled in this phase");
        }

        cancelProcess(offer, process, request == null ? null : request.reason(), CUSTOMER_CANCELLATION_REASON, false);
        return toResponse(offer, process);
    }

    @Transactional
    public List<StaffProcessResponse> getStaffProcesses(String role) {
        String staffRole = staffRole(role);

        List<StaffProcessResponse> responses = new ArrayList<>();

        List<Process> processes = processRepository.findAllByOrderByStartTimeDesc();

        for (Process process : processes) {
            Offer offer = offerForProcess(process);
            expireIfNeeded(offer, process);

            if (canViewStaffProcess(process, staffRole)) {
                StaffProcessResponse response = toStaffResponse(process, staffRole);
                responses.add(response);
            }
        }

        return responses;
    }

    @Transactional
    public StaffProcessResponse getStaffProcess(Long processId, String role) {
        String staffRole = staffRole(role);
        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Process not found"));
        expireIfNeeded(offerForProcess(process), process);

        if (!canViewStaffProcess(process, staffRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Process is not available for this role");
        }

        return toStaffResponse(process, staffRole);
    }

    @Transactional
    public StaffProcessResponse transitionStaffProcess(Long processId, StaffTransitionRequest request, String role) {
        String staffRole = staffRole(role);

        if (!ROLE_WORKER.equals(staffRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only workers can transition processes");
        }

        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Process not found"));
        Offer offer = offerForProcess(process);
        expireIfNeeded(offer, process);

        String action = requireText(request == null ? null : request.action(), "Action is required");
        String currentPhase = currentPhaseName(process);

        if ("MARK_READY".equals(action) && PHASE_RESERVATION.equals(currentPhase)) {
            transition(process, offer, STATUS_READY, PHASE_READY);
        } else if ("START_DELIVERY".equals(action) && PHASE_READY.equals(currentPhase)) {
            transition(process, offer, STATUS_DELIVERY, PHASE_DELIVERY);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid process transition");
        }

        return toStaffResponse(process, staffRole);
    }

    @Transactional
    public StaffProcessResponse cancelStaffProcess(Long processId, CancelRequest request, String role) {
        String staffRole = staffRole(role);
        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Process not found"));
        Offer offer = offerForProcess(process);
        expireIfNeeded(offer, process);

        if (!canViewStaffProcess(process, staffRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Process is not available for this role");
        }

        String currentPhase = currentPhaseName(process);
        if (!canCancelStaffProcess(staffRole, currentPhase)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Process cannot be cancelled in this phase");
        }

        cancelProcess(offer, process, request == null ? null : request.reason(), STAFF_CANCELLATION_REASON, true);
        return toStaffResponse(process, staffRole);
    }

    private void transition(Process process, Offer offer, String nextStatus, String nextPhase) {
        finishCurrentPhase(process);
        offer.setStatus(status(nextStatus));
        process.addPhase(new Phase(process, phaseType(nextPhase), offer));
    }

    private void cancelProcess(
            Offer offer,
            Process process,
            String reason,
            String reasonName,
            boolean reasonRequired
    ) {
        String cancellationReason = reasonRequired
                ? requireText(reason, "Cancellation reason is required")
                : optionalText(reason);
        restoreReservedStock(offer);
        offer.setStatus(status(STATUS_CANCELLED));
        Phase currentPhase = currentPhase(process);
        CancellationReason reasonType = cancellationReason(reasonName);
        currentPhase.setCancellation(new Cancellation(currentPhase, reasonType, cancellationReason));
        currentPhase.finish();
        process.finish();
    }

    private void expireIfNeeded(Offer offer, Process process) {
        if (offer == null || process == null || process.getEndTime() != null) {
            return;
        }

        if (!STATUS_OFFER.equals(offer.getStatus().getName()) || offer.getExpiresAt() == null) {
            return;
        }

        if (offer.getExpiresAt().after(new Date(System.currentTimeMillis()))) {
            return;
        }

        offer.setStatus(status(STATUS_EXPIRED));
        finishCurrentPhase(process);
        process.finish();
    }

    private void verifyStockIsStillAvailable(Offer offer) {
        Map<Long, Long> stockByPlantId = stockByPlantId(offer.getItems()
                .stream()
                .map(OrderItem::getPlantPrice)
                .toList());

        for (OrderItem item : offer.getItems()) {
            long availableQuantity = stockByPlantId.getOrDefault(item.getPlantPrice().getPlant().getId(), 0L);
            if (availableQuantity < offeredQuantity(item)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Insufficient stock for " + item.getPlantPrice().getPlant().getName()
                );
            }
        }
    }
// prolazi kroz sve iteme u offeru, i za svaki od njih prolazi kroz sve lokacije i proverava koliko je itema available na toj lokaciji, i uzima toliko itema.
    private void reserveStock(Offer offer) {


        for (OrderItem item : offer.getItems()) {
            int remainingQuantity = offeredQuantity(item);
            List<RelocationHistory> histories = relocationHistoryRepository
                    .findByPlant_IdAndEndTimeIsNullOrderByStartTimeAsc(item.getPlantPrice().getPlant().getId());

            for (RelocationHistory history : histories) {
                if (remainingQuantity <= 0) {
                    break;
                }

                long availableQuantity = history.getInStock() == null ? 0L : history.getInStock();
                if (availableQuantity <= 0) {
                    continue;
                }

                int reservedQuantity = (int) Math.min(remainingQuantity, availableQuantity);
                history.setInStock(availableQuantity - reservedQuantity);
                stockReservationRepository.save(new StockReservation(item, history, reservedQuantity));
                remainingQuantity -= reservedQuantity;
            }

            if (remainingQuantity > 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Insufficient stock for " + item.getPlantPrice().getPlant().getName()
                );
            }

            item.setQuantity(offeredQuantity(item));
        }
    }

    private boolean offerMatchesRequest(
            Offer offer,
            Map<Long, Integer> requestedQuantities,
            Map<Long, PlantPrice> pricesById
    ) {
        return offeredQuantitiesByPlantId(offer).equals(quantitiesByPlantId(requestedQuantities, pricesById));
    }

    private void restoreReservedStock(Offer offer) {
        List<StockReservation> reservations = stockReservationRepository.findByOrderItem_Offer_Id(offer.getId());

        for (StockReservation reservation : reservations) {
            RelocationHistory history = reservation.getRelocationHistory();
            long currentStock = history.getInStock() == null ? 0L : history.getInStock();
            history.setInStock(currentStock + reservation.getQuantity());
        }

        stockReservationRepository.deleteByOrderItem_Offer_Id(offer.getId());
        offer.getItems().forEach(item -> item.setQuantity(0));
    }

    private Map<Long, Integer> normalizeItems(List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one item is required");
        }

        Map<Long, Integer> quantities = new LinkedHashMap<>();

        for (OrderItemRequest item : items) {
            if (item == null || item.plantPriceId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plant price is required");
            }

            if (item.quantity() == null || item.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
            }

            quantities.merge(item.plantPriceId(), item.quantity(), Integer::sum);
        }

        return quantities;
    }

    private Map<Long, Integer> currentQuantitiesByPlantId(Offer offer) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItem item : offer.getItems()) {
            quantities.merge(item.getPlantPrice().getPlant().getId(), reservedQuantity(item), Integer::sum);
        }
        return quantities;
    }

    private Map<Long, Integer> offeredQuantitiesByPlantId(Offer offer) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItem item : offer.getItems()) {
            quantities.merge(item.getPlantPrice().getPlant().getId(), offeredQuantity(item), Integer::sum);
        }
        return quantities;
    }

    private Map<Long, Integer> quantitiesByPlantId(
            Map<Long, Integer> quantitiesByPriceId,
            Map<Long, PlantPrice> pricesById
    ) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();

        quantitiesByPriceId.forEach((priceId, quantity) -> {
            PlantPrice price = pricesById.get(priceId);
            if (price == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plant price is required");
            }
            quantities.merge(price.getPlant().getId(), quantity, Integer::sum);
        });

        return quantities;
    }

    private Map<Long, PlantPrice> loadEditPrices(Long customerId, Offer offer, Collection<Long> priceIds) {
        Map<Long, PlantPrice> existingPricesByPlantId = offer.getItems()
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getPlantPrice().getPlant().getId(),
                        OrderItem::getPlantPrice,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
        Map<Long, PlantPrice> pricesById = new LinkedHashMap<>();

        for (Long priceId : priceIds) {
            PlantPrice price = plantPriceRepository.findById(priceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant price not found"));

            PlantPrice existingPrice = existingPricesByPlantId.get(price.getPlant().getId());
            if (existingPrice != null) {
                pricesById.put(priceId, existingPrice);
                continue;
            }

            Optional<PlantPrice> lockedPrice = lockedPriceForCustomerAndPlant(customerId, price.getPlant().getId());
            if (lockedPrice.isPresent()) {
                pricesById.put(priceId, lockedPrice.get());
            } else if (price.getEndTime() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plant price is no longer active");
            } else {
                pricesById.put(priceId, price);
            }
        }

        return pricesById;
    }

    private Map<Long, PlantPrice> loadPrices(Long customerId, Collection<Long> priceIds) {
        Map<Long, PlantPrice> pricesById = new LinkedHashMap<>();
        for(Long priceId : priceIds) {
            PlantPrice price = plantPriceRepository.findById(priceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "plant price not found"));

            Optional<PlantPrice> lockedPrice = lockedPriceForCustomerAndPlant(customerId, price.getPlant().getId());
            if (lockedPrice.isPresent()) {
                pricesById.put(priceId, lockedPrice.get());
            } else if(price.getEndTime() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plant price is no longer active");
            } else {
                pricesById.put(priceId, price);
            }
        }

        return pricesById;
    }

    private Optional<PlantPrice> lockedPriceForCustomerAndPlant(Long customerId, Long plantId) {
        return orderItemRepository.findLockedPricesForCustomerAndPlant(
                        customerId,
                        plantId,
                        PRICE_LOCK_STATUSES
                )
                .stream()
                .findFirst();
    }

    private Map<Long, Long> stockByPlantId(Collection<PlantPrice> prices) {

        List<Long> plantIds = new ArrayList<>();
        for( PlantPrice price : prices) {
            plantIds.add(price.getPlant().getId());
        }
        List<RelocationHistoryRepository.PlantStockView> activeStocks = relocationHistoryRepository.findActiveStockByPlantIds((plantIds));
        Map<Long, Long> StocksByPlantId = new HashMap<>();
        for(RelocationHistoryRepository.PlantStockView stockView : activeStocks){
            StocksByPlantId.put(stockView.getPlantId(), stockView.getAvailableQuantity());
        }
        return StocksByPlantId;
    }

    private Offer customerOffer(Long customerId, Long offerId) {
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer is required");
        }

        if (offerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is required");
        }

        return offerRepository.findOrderForCustomer(offerId, customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private Process processForCustomerOffer(Long customerId, Long offerId) {
        return processRepository.findByOfferIdAndCustomerId(offerId, customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Process not found"));
    }

    private Offer offerForProcess(Process process) {
        return process.getPhases()
                .stream()
                .filter(phase -> phase.getOffer() != null)
                .max(Comparator.comparing(Phase::getStartTime))
                .map(Phase::getOffer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
    }

    private Phase currentPhase(Process process) {
        return process.getPhases()
                .stream()
                .filter(phase -> phase.getEndTime() == null)
                .max(Comparator.comparing(Phase::getStartTime))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Process is finished"));
    }

    private String currentPhaseName(Process process) {
        if (process.getEndTime() != null) {
            return null;
        }

        return currentPhase(process).getType().getName();
    }

    private void requireCurrentPhase(Process process, String phaseName) {
        if (!phaseName.equals(currentPhaseName(process))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid process phase");
        }
    }

    private void finishCurrentPhase(Process process) {
        Phase phase = currentPhase(process);
        if (phase.getEndTime() == null) {
            phase.finish();
        }
    }

    private OfferStatus status(String name) {
        return offerStatusRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, name + " status is not seeded"));
    }

    private PhaseType phaseType(String name) {
        return phaseTypeRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, name + " phase type is not seeded"));
    }

    private CancellationReason cancellationReason(String name) {
        return cancellationReasonRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        name + " cancellation reason is not seeded"
                ));
    }

    private OrderResponse toResponse(Offer offer, Process process) {
        List<OrderItemResponse> items = itemResponsesFor(offer);
        BigDecimal total = totalFor(items);

        return new OrderResponse(
                offer.getId(),
                formatDate(offer.getCreatedAt()),
                offer.getStatus().getName(),
                total,
                items,
                process.getId(),
                currentPhaseName(process),
                formatDate(offer.getExpiresAt()),
                phaseHistoryFor(process),
                orderHistoryFor(offer),
                canCustomerAccept(offer, process),
                canCustomerReject(offer, process),
                canCustomerCancel(offer, process)
        );
    }

    private StaffProcessResponse toStaffResponse(Process process, String role) {
        Offer offer = offerForProcess(process);
        List<OrderItemResponse> items = itemResponsesFor(offer);
        Customer customer = process.getCustomer();

        return new StaffProcessResponse(
                process.getId(),
                offer.getId(),
                customer.getId(),
                (customer.getFirstName() + " " + customer.getLastName()).trim(),
                customer.getEmail(),
                offer.getStatus().getName(),
                currentPhaseName(process),
                formatDate(process.getStartTime()),
                formatDate(process.getEndTime()),
                formatDate(offer.getExpiresAt()),
                totalFor(items),
                items,
                phaseHistoryFor(process),
                orderHistoryFor(offer),
                staffAllowedActions(process, role)
        );
    }

    private List<OrderItemResponse> itemResponsesFor(Offer offer) {
        if (!offer.getItems().isEmpty()) {
            return offer.getItems()
                    .stream()
                    .map(this::toItemResponse)
                    .toList();
        }

        List<OrderItemResponse> fallbackItems = new ArrayList<>();
        for (PlantPrice price : offer.getPrices()) {
            fallbackItems.add(toItemResponse(price, 1, 1, 1));
        }
        return fallbackItems;
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return toItemResponse(
                item.getPlantPrice(),
                requestedQuantity(item),
                offeredQuantity(item),
                reservedQuantity(item)
        );
    }

    private OrderItemResponse toItemResponse(
            PlantPrice price,
            Integer requestedQuantity,
            Integer offeredQuantity,
            Integer reservedQuantity
    ) {
        Plant plant = price.getPlant();
        return new OrderItemResponse(
                plant.getId(),
                price.getId(),
                plant.getName(),
                offeredQuantity,
                requestedQuantity,
                offeredQuantity,
                reservedQuantity,
                !requestedQuantity.equals(offeredQuantity),
                price.getPrice()
        );
    }

    private BigDecimal totalFor(List<OrderItemResponse> items) {
        return items.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }



    private void addOrderHistorySnapshot(Offer offer, Date changedAt) {
        OrderHistory snapshot = new OrderHistory(offer, changedAt);
        offer.getItems()
                .stream()
                .sorted(Comparator.comparing(item -> item.getPlantPrice().getId()))
                .forEach(item -> snapshot.addItem(new OrderHistoryItem(
                        item.getPlantPrice(),
                        reservedQuantity(item)
                )));
        offer.addOrderHistory(snapshot);
    }

    private List<OrderHistoryResponse> orderHistoryFor(Offer offer) {
        return offer.getOrderHistory()
                .stream()
                .sorted(Comparator.comparing(OrderHistory::getChangedAt)
                        .thenComparing(OrderHistory::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toOrderHistoryResponse)
                .toList();
    }

    private OrderHistoryResponse toOrderHistoryResponse(OrderHistory snapshot) {
        List<OrderHistoryItemResponse> items = snapshot.getItems()
                .stream()
                .map(item -> {
                    PlantPrice price = item.getPlantPrice();
                    Plant plant = price.getPlant();
                    return new OrderHistoryItemResponse(
                            plant.getId(),
                            price.getId(),
                            plant.getName(),
                            item.getQuantity(),
                            price.getPrice()
                    );
                })
                .toList();
        BigDecimal total = items.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderHistoryResponse(
                snapshot.getId(),
                formatDate(snapshot.getChangedAt()),
                total,
                items
        );
    }

    private List<PhaseHistoryResponse> phaseHistoryFor(Process process) {
        return process.getPhases()
                .stream()
                .sorted(Comparator.comparing(Phase::getStartTime))
                .map(this::toPhaseHistoryResponse)
                .toList();
    }

    private PhaseHistoryResponse toPhaseHistoryResponse(Phase phase) {
        Cancellation cancellation = phase.getCancellation();
        Long durationSeconds = null;

        if (phase.getStartTime() != null && phase.getEndTime() != null) {
            durationSeconds = Duration.between(
                    phase.getStartTime().toInstant(),
                    phase.getEndTime().toInstant()
            ).getSeconds();
        }

        return new PhaseHistoryResponse(
                phase.getId(),
                phase.getType().getName(),
                formatDate(phase.getStartTime()),
                formatDate(phase.getEndTime()),
                durationSeconds,
                cancellation == null ? null : cancellation.getCancellationReason().getName(),
                cancellation == null ? null : cancellation.getReason()
        );
    }

    private List<String> staffAllowedActions(Process process, String role) {
        String currentPhase = currentPhaseName(process);

        if (currentPhase == null) {
            return List.of();
        }

        if (ROLE_ADMIN.equals(role)) {
            return canCancelStaffProcess(role, currentPhase) ? List.of("CANCEL") : List.of();
        }

        if (!ROLE_WORKER.equals(role)) {
            return List.of();
        }

        List<String> actions = new ArrayList<>();

        if (PHASE_RESERVATION.equals(currentPhase)) {
            actions.add("MARK_READY");
        } else if (PHASE_READY.equals(currentPhase)) {
            actions.add("START_DELIVERY");
        }

        if (canCancelStaffProcess(role, currentPhase)) {
            actions.add("CANCEL");
        }

        return actions;
    }

    private boolean canViewStaffProcess(Process process, String role) {
        if (ROLE_ADMIN.equals(role)) {
            return true;
        }

        if (!ROLE_WORKER.equals(role)) {
            return false;
        }

        String currentPhase = currentPhaseName(process);
        return (PHASE_RESERVATION.equals(currentPhase) || PHASE_READY.equals(currentPhase));
//        return Set.of(PHASE_RESERVATION, PHASE_READY).contains(currentPhase);

    }

    private boolean canCancelStaffProcess(String role, String currentPhase) {
        if (currentPhase == null) {
            return false;
        }

        if (ROLE_ADMIN.equals(role)) {
            return Set.of(PHASE_OFFER, PHASE_RESERVATION, PHASE_READY).contains(currentPhase);
        }

        if (ROLE_WORKER.equals(role)) {
            return Set.of(PHASE_RESERVATION, PHASE_READY).contains(currentPhase);
        }

        return false;
    }

    private String staffRole(String role) {
        if (ROLE_ADMIN.equals(role) || ROLE_WORKER.equals(role)) {
            return role;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff process access is not available for this role");
    }

    private boolean canCustomerAccept(Offer offer, Process process) {
        return STATUS_OFFER.equals(offer.getStatus().getName()) && PHASE_OFFER.equals(currentPhaseName(process));
    }

    private boolean canCustomerReject(Offer offer, Process process) {
        return canCustomerAccept(offer, process);
    }

    private boolean canCustomerCancel(Offer offer, Process process) {
        String currentPhase = currentPhaseName(process);

        return !STATUS_CANCELLED.equals(offer.getStatus().getName())
                && currentPhase != null
                && Set.of(PHASE_OFFER, PHASE_RESERVATION).contains(currentPhase);
    }

    private Integer requestedQuantity(OrderItem item) {
        if (item.getRequestedQuantity() != null) {
            return item.getRequestedQuantity();
        }

        return item.getQuantity() == null ? 0 : item.getQuantity();
    }

    private Integer offeredQuantity(OrderItem item) {
        if (item.getOfferedQuantity() != null) {
            return item.getOfferedQuantity();
        }

        return item.getQuantity() == null ? 0 : item.getQuantity();
    }

    private Integer reservedQuantity(OrderItem item) {
        return item.getQuantity() == null ? 0 : item.getQuantity();
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return value.trim();
    }

    private String optionalText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }

        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date.toInstant().atOffset(ZoneOffset.UTC));
    }
}
