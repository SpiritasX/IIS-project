package com.example.iis.service;

import com.example.iis.dto.CreateOrderRequest;
import com.example.iis.dto.OrderItemRequest;
import com.example.iis.dto.OrderItemResponse;
import com.example.iis.dto.OrderResponse;
import com.example.iis.model.Customer;
import com.example.iis.model.Offer;
import com.example.iis.model.OfferStatus;
import com.example.iis.model.OrderItem;
import com.example.iis.model.Phase;
import com.example.iis.model.PhaseType;
import com.example.iis.model.Plant;
import com.example.iis.model.PlantPrice;
import com.example.iis.model.Process;
import com.example.iis.repository.CustomerRepository;
import com.example.iis.repository.OfferRepository;
import com.example.iis.repository.OfferStatusRepository;
import com.example.iis.repository.PhaseTypeRepository;
import com.example.iis.repository.PlantPriceRepository;
import com.example.iis.repository.ProcessRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private final CustomerRepository customerRepository;
    private final OfferRepository offerRepository;
    private final OfferStatusRepository offerStatusRepository;
    private final PhaseTypeRepository phaseTypeRepository;
    private final PlantPriceRepository plantPriceRepository;
    private final ProcessRepository processRepository;
    private final RecommendationClient recommendationClient;

    public OrderService(
            CustomerRepository customerRepository,
            OfferRepository offerRepository,
            OfferStatusRepository offerStatusRepository,
            PhaseTypeRepository phaseTypeRepository,
            PlantPriceRepository plantPriceRepository,
            ProcessRepository processRepository,
            RecommendationClient recommendationClient) {
        this.customerRepository = customerRepository;
        this.offerRepository = offerRepository;
        this.offerStatusRepository = offerStatusRepository;
        this.phaseTypeRepository = phaseTypeRepository;
        this.plantPriceRepository = plantPriceRepository;
        this.processRepository = processRepository;
        this.recommendationClient = recommendationClient;
    }

    @Transactional
    public OrderResponse createOrder(Long customerId, CreateOrderRequest request) {
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer is required");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        Map<Long, Integer> quantities = normalizeItems(request.items());
        Map<Long, PlantPrice> pricesById = loadPrices(quantities.keySet());

        String deliveryAddress = request.deliveryAddress();
        if (deliveryAddress != null && !deliveryAddress.trim().isEmpty()) {
            customer.setAddress(deliveryAddress.trim());
            customerRepository.save(customer);
        }

        OfferStatus pendingStatus = offerStatusRepository.findByName("Pending")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Pending status is not seeded"));
        PhaseType orderPlacedType = phaseTypeRepository.findByName("Order placed")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Order placed phase type is not seeded"));

        Offer offer = new Offer(pendingStatus, pricesById.values());
        quantities.forEach((priceId, quantity) -> offer.addItem(new OrderItem(pricesById.get(priceId), quantity)));

        Offer savedOffer = offerRepository.save(offer);
        Process process = new Process(customer);
        process.addPhase(new Phase(process, orderPlacedType, savedOffer));
        processRepository.save(process);

        offer.getItems().forEach(item -> recommendationClient.createPurchase(customer.getId(), item.getPlantPrice().getPlant().getId(), item.getQuantity()));

        return toResponse(savedOffer);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForCustomer(Long customerId) {
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer is required");
        }

        return offerRepository.findOrdersForCustomer(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderForCustomer(Long customerId, Long orderId) {
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer is required");
        }

        if (orderId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is required");
        }

        Offer offer = offerRepository.findOrderForCustomer(orderId, customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        return toResponse(offer);
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

    private OrderResponse toResponse(Offer offer) {
        List<OrderItemResponse> items = itemResponsesFor(offer);
        BigDecimal total = items.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponse(
                offer.getId(),
                formatDate(offer),
                offer.getStatus().getName(),
                total,
                items
        );
    }

    private List<OrderItemResponse> itemResponsesFor(Offer offer) {
        if (!offer.getItems().isEmpty()) {
            return offer.getItems()
                    .stream()
                    .map(item -> toItemResponse(item.getPlantPrice(), item.getQuantity()))
                    .toList();
        }

        List<OrderItemResponse> fallbackItems = new ArrayList<>();
        for (PlantPrice price : offer.getPrices()) {
            fallbackItems.add(toItemResponse(price, 1));
        }
        return fallbackItems;
    }

    private OrderItemResponse toItemResponse(PlantPrice price, Integer quantity) {
        Plant plant = price.getPlant();
        return new OrderItemResponse(
                plant.getId(),
                price.getId(),
                plant.getName(),
                quantity,
                price.getPrice()
        );
    }

    private String formatDate(Offer offer) {
        if (offer.getCreatedAt() == null) {
            return "";
        }

        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                offer.getCreatedAt().toInstant().atOffset(ZoneOffset.UTC)
        );
    }
}
