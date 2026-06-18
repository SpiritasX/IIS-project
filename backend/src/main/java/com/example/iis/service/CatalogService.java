package com.example.iis.service;

import com.example.iis.dto.CatalogOptionsResponse;
import com.example.iis.dto.PlantOrderRecommendationItemResponse;
import com.example.iis.dto.PlantOrderRecommendationRequest;
import com.example.iis.dto.PlantOrderRecommendationResponse;
import com.example.iis.dto.ProductResponse;
import com.example.iis.model.Plant;
import com.example.iis.model.PlantPrice;
import com.example.iis.model.PlantSpecies;
import com.example.iis.model.PlantType;
import com.example.iis.model.PlantVariety;
import com.example.iis.repository.PlantPriceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Service
public class CatalogService {
    private static final int DEFAULT_MAX_QUANTITY_PER_PLANT = 5;
    private static final int MAX_QUANTITY_PER_PLANT = 25;
    private static final int MAX_BUDGET_UNITS = 1_000_000;

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

    @Transactional(readOnly = true)
    public CatalogOptionsResponse getCatalogOptions() {
        List<CatalogItem> items = activeCatalogItems();

        return new CatalogOptionsResponse(
                sortedUnique(items.stream().map(CatalogItem::season).toList()),
                sortedUnique(items.stream().map(CatalogItem::category).toList()),
                sortedUnique(items.stream().map(CatalogItem::type).toList()),
                sortedUnique(items.stream().map(CatalogItem::species).toList()),
                sortedUnique(items.stream().map(CatalogItem::variety).toList())
        );
    }

    @Transactional(readOnly = true)
    public PlantOrderRecommendationResponse recommendOrder(PlantOrderRecommendationRequest request) {
        if (request == null || request.budget() == null || request.budget().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Budget must be greater than zero");
        }

        int maxQuantity = normalizeMaxQuantity(request.maxQuantityPerPlant());
        List<CatalogItem> candidates = activeCatalogItems()
                .stream()
                .filter(item -> item.available()
                        && matches(item.season(), request.season())
                        && matches(item.category(), request.category())
                        && matches(item.type(), request.type())
                        && matches(item.species(), request.species())
                        && matches(item.variety(), request.variety()))
                .toList();

        if (candidates.isEmpty()) {
            return emptyRecommendation(request.budget());
        }

        int scale = monetaryScale(request.budget(), candidates);
        BigDecimal multiplier = BigDecimal.TEN.pow(scale);
        int budgetUnits = toUnits(request.budget(), multiplier);

        if (budgetUnits > MAX_BUDGET_UNITS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Budget is too large for automatic planning");
        }

        List<KnapsackUnit> units = expandedUnits(candidates, maxQuantity, multiplier, budgetUnits);
        PlanState[] states = new PlanState[budgetUnits + 1];
        states[0] = new PlanState(0, null, null);

        for (KnapsackUnit unit : units) {
            for (int amount = budgetUnits; amount >= unit.priceUnits(); amount--) {
                PlanState previous = states[amount - unit.priceUnits()];

                if (previous == null) {
                    continue;
                }

                long score = previous.score() + unit.value();
                PlanState current = states[amount];

                if (current == null || score > current.score()) {
                    states[amount] = new PlanState(score, previous, unit);
                }
            }
        }

        PlanState bestState = null;

        for (PlanState state : states) {
            if (state != null && (bestState == null || state.score() > bestState.score())) {
                bestState = state;
            }
        }

        if (bestState == null || bestState.unit() == null) {
            return emptyRecommendation(request.budget());
        }

        Map<CatalogItem, Integer> quantities = quantitiesFor(bestState);
        List<PlantOrderRecommendationItemResponse> items = quantities.entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().name()))
                .map(entry -> toRecommendationItem(entry.getKey(), entry.getValue()))
                .toList();
        BigDecimal total = items.stream()
                .map(PlantOrderRecommendationItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PlantOrderRecommendationResponse(
                request.budget(),
                total,
                request.budget().subtract(total).max(BigDecimal.ZERO),
                items.stream().mapToInt(PlantOrderRecommendationItemResponse::quantity).sum(),
                items
        );
    }

    public ProductResponse getProduct(Long id) {
        return toResponse(plantPriceRepository.findActiveCatalogPriceByPlantId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")));
    }

    private ProductResponse toResponse(PlantPrice price) {
        CatalogItem item = toCatalogItem(price);

        return new ProductResponse(
                item.plantId(),
                item.priceId(),
                item.name(),
                item.description(),
                item.category(),
                item.type(),
                item.species(),
                item.variety(),
                item.season(),
                item.price(),
                item.status(),
                item.available()
        );
    }

    private PlantOrderRecommendationItemResponse toRecommendationItem(CatalogItem item, Integer quantity) {
        return new PlantOrderRecommendationItemResponse(
                item.plantId(),
                item.priceId(),
                item.name(),
                item.description(),
                item.category(),
                item.type(),
                item.species(),
                item.variety(),
                item.season(),
                item.price(),
                quantity,
                item.price().multiply(BigDecimal.valueOf(quantity))
        );
    }

    private List<CatalogItem> activeCatalogItems() {
        return plantPriceRepository.findActiveCatalogPrices()
                .stream()
                .map(this::toCatalogItem)
                .toList();
    }

    private CatalogItem toCatalogItem(PlantPrice price) {
        Plant plant = price.getPlant();
        PlantVariety variety = plant.getVariety();
        PlantSpecies species = variety.getSpecies();
        PlantType type = species.getType();

        return new CatalogItem(
                plant.getId(),
                price.getId(),
                plant.getName(),
                plant.getDescription(),
                type.getCategory().getName(),
                type.getName(),
                species.getName(),
                variety.getName(),
                variety.getSeason(),
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

    private List<String> sortedUnique(List<String> values) {
        Set<String> uniqueValues = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        values.stream()
                .filter(this::hasText)
                .forEach(uniqueValues::add);

        return List.copyOf(uniqueValues);
    }

    private int normalizeMaxQuantity(Integer value) {
        if (value == null) {
            return DEFAULT_MAX_QUANTITY_PER_PLANT;
        }

        return Math.max(1, Math.min(MAX_QUANTITY_PER_PLANT, value));
    }

    private boolean matches(String actual, String expected) {
        return !hasText(expected) || (actual != null && actual.equalsIgnoreCase(expected.trim()));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private int monetaryScale(BigDecimal budget, List<CatalogItem> items) {
        int scale = Math.max(0, budget.stripTrailingZeros().scale());

        for (CatalogItem item : items) {
            scale = Math.max(scale, Math.max(0, item.price().stripTrailingZeros().scale()));
        }

        return Math.min(2, scale);
    }

    private int toUnits(BigDecimal value, BigDecimal multiplier) {
        long units = value.multiply(multiplier)
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        if (units <= 0 || units > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Budget or price is outside the supported range");
        }

        return (int) units;
    }

    private List<KnapsackUnit> expandedUnits(
            List<CatalogItem> candidates,
            int maxQuantity,
            BigDecimal multiplier,
            int budgetUnits
    ) {
        long diversityBonus = (long) budgetUnits + 1;
        List<KnapsackUnit> units = new ArrayList<>();

        for (CatalogItem candidate : candidates) {
            int priceUnits = toUnits(candidate.price(), multiplier);

            if (priceUnits > budgetUnits) {
                continue;
            }

            for (int copy = 1; copy <= maxQuantity; copy++) {
                long value = priceUnits + (copy == 1 ? diversityBonus : 0);
                units.add(new KnapsackUnit(candidate, priceUnits, value));
            }
        }

        return units;
    }

    private Map<CatalogItem, Integer> quantitiesFor(PlanState state) {
        Map<CatalogItem, Integer> quantities = new LinkedHashMap<>();
        PlanState cursor = state;

        while (cursor != null && cursor.unit() != null) {
            quantities.merge(cursor.unit().item(), 1, Integer::sum);
            cursor = cursor.previous();
        }

        return quantities;
    }

    private PlantOrderRecommendationResponse emptyRecommendation(BigDecimal budget) {
        return new PlantOrderRecommendationResponse(
                budget,
                BigDecimal.ZERO,
                budget,
                0,
                List.of()
        );
    }

    private record CatalogItem(
            Long plantId,
            Long priceId,
            String name,
            String description,
            String category,
            String type,
            String species,
            String variety,
            String season,
            BigDecimal price,
            String status,
            boolean available
    ) {
        private CatalogItem {
            Objects.requireNonNull(price);
        }
    }

    private record KnapsackUnit(CatalogItem item, int priceUnits, long value) {
    }

    private record PlanState(long score, PlanState previous, KnapsackUnit unit) {
    }
}
