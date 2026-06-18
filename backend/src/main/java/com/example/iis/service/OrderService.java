package com.example.iis.service;

import com.example.iis.dto.CancelRequest;
import com.example.iis.dto.CreateOrderRequest;
import com.example.iis.dto.OrderItemRequest;
import com.example.iis.dto.OrderItemResponse;
import com.example.iis.dto.OrderResponse;
import com.example.iis.dto.PhaseHistoryResponse;
import com.example.iis.dto.StaffProcessResponse;
import com.example.iis.dto.StaffTransitionRequest;
import com.example.iis.model.Cancellation;
import com.example.iis.model.CancellationReason;
import com.example.iis.model.Customer;
import com.example.iis.model.Offer;
import com.example.iis.model.OfferStatus;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private static final String CUSTOMER_CANCELLATION_REASON = "Customer cancellation";
    private static final String STAFF_CANCELLATION_REASON = "Staff cancellation";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_WORKER = "WORKER";

    private final CancellationReasonRepository cancellationReasonRepository;
    private final CustomerRepository customerRepository;
    private final OfferRepository offerRepository;
    private final OfferStatusRepository offerStatusRepository;
    private final PhaseTypeRepository phaseTypeRepository;
    private final PlantPriceRepository plantPriceRepository;
    private final ProcessRepository processRepository;
    private final RelocationHistoryRepository relocationHistoryRepository;
    private final StockReservationRepository stockReservationRepository;

    public OrderService(
            CustomerRepository customerRepository,
            OfferRepository offerRepository,
            OfferStatusRepository offerStatusRepository,
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
        Map<Long, PlantPrice> pricesById = loadPrices(requestedQuantities.keySet());
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
                offer.addPrice(price);
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

        if (Boolean.TRUE.equals(request.autoAcceptIfUnchanged()) && offerMatchesRequest(savedOffer, requestedQuantities)) {
            reserveStock(savedOffer);
            finishCurrentPhase(process);
            savedOffer.setStatus(status(STATUS_RESERVATION));
            process.addPhase(new Phase(process, phaseType(PHASE_RESERVATION), savedOffer));
        }

        return toResponse(savedOffer, process);
    }

    @Transactional
    public List<OrderResponse> getOrdersForCustomer(Long customerId) {
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer is required");
        }

        return offerRepository.findOrdersForCustomer(customerId)
                .stream()
                .map(offer -> {
                    Process process = processForCustomerOffer(customerId, offer.getId());
                    expireIfNeeded(offer, process);
                    return toResponse(offer, process);
                })
                .toList();
    }

    @Transactional
    public OrderResponse getOrderForCustomer(Long customerId, Long orderId) {
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer is required");
        }

        if (orderId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is required");
        }

        Offer offer = offerRepository.findOrderForCustomer(orderId, customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        Process process = processForCustomerOffer(customerId, orderId);
        expireIfNeeded(offer, process);

        return toResponse(offer, process);
    }

    @Transactional
    public OrderResponse acceptOffer(Long customerId, Long offerId) {
        Offer offer = customerOffer(customerId, offerId);
        Process process = processForCustomerOffer(customerId, offerId);
        expireIfNeeded(offer, process);
        requireCurrentPhase(process, PHASE_OFFER);

        if (!STATUS_OFFER.equals(offer.getStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offer can no longer be accepted");
        }

        verifyStockIsStillAvailable(offer);
        reserveStock(offer);
        finishCurrentPhase(process);
        offer.setStatus(status(STATUS_RESERVATION));
        process.addPhase(new Phase(process, phaseType(PHASE_RESERVATION), offer));

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

        cancelProcess(offer, process, request == null ? null : request.reason(), CUSTOMER_CANCELLATION_REASON);
        return toResponse(offer, process);
    }

    @Transactional
    public List<StaffProcessResponse> getStaffProcesses(String role) {
        String staffRole = staffRole(role);

        return processRepository.findAllByOrderByStartTimeDesc()
                .stream()
                .map(process -> {
                    Offer offer = offerForProcess(process);
                    expireIfNeeded(offer, process);
                    return process;
                })
                .filter(process -> canViewStaffProcess(process, staffRole))
                .map(process -> toStaffResponse(process, staffRole))
                .toList();
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

        if (!canViewStaffProcess(process, staffRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Process is not available for this role");
        }

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

        cancelProcess(offer, process, request == null ? null : request.reason(), STAFF_CANCELLATION_REASON);
        return toStaffResponse(process, staffRole);
    }

    private void transition(Process process, Offer offer, String nextStatus, String nextPhase) {
        finishCurrentPhase(process);
        offer.setStatus(status(nextStatus));
        process.addPhase(new Phase(process, phaseType(nextPhase), offer));
    }

    private void cancelProcess(Offer offer, Process process, String reason, String reasonName) {
        String cancellationReason = requireText(reason, "Cancellation reason is required");
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

    private boolean offerMatchesRequest(Offer offer, Map<Long, Integer> requestedQuantities) {
        if (offer.getItems().size() != requestedQuantities.size()) {
            return false;
        }

        for (OrderItem item : offer.getItems()) {
            Long priceId = item.getPlantPrice().getId();
            Integer requestedQuantity = requestedQuantities.get(priceId);

            if (requestedQuantity == null || !requestedQuantity.equals(offeredQuantity(item))) {
                return false;
            }
        }

        return true;
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

    private Map<Long, PlantPrice> loadPrices(Collection<Long> priceIds) {
        Map<Long, PlantPrice> pricesById = new LinkedHashMap<>();

        for (Long priceId : priceIds) {
            PlantPrice price = plantPriceRepository.findById(priceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant price not found"));

            if (price.getEndTime() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plant price is no longer active");
            }

            pricesById.put(priceId, price);
        }

        return pricesById;
    }

    private Map<Long, Long> stockByPlantId(Collection<PlantPrice> prices) {
        List<Long> plantIds = prices.stream()
                .map(price -> price.getPlant().getId())
                .distinct()
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
        return Set.of(PHASE_RESERVATION, PHASE_READY).contains(currentPhase);
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

    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }

        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date.toInstant().atOffset(ZoneOffset.UTC));
    }
}
