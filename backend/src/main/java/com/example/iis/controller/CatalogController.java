package com.example.iis.controller;

import com.example.iis.dto.CatalogOptionsResponse;
import com.example.iis.dto.CreateCatalogItemRequest;
import com.example.iis.dto.PlantOrderRecommendationRequest;
import com.example.iis.dto.PlantOrderRecommendationResponse;
import com.example.iis.dto.ProductResponse;
import com.example.iis.service.CatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plants")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<ProductResponse> getProducts() {
        return catalogService.getProducts();
    }

    @GetMapping("/options")
    public CatalogOptionsResponse getCatalogOptions() {
        return catalogService.getCatalogOptions();
    }

    @PostMapping("/recommend-order")
    public PlantOrderRecommendationResponse recommendOrder(@RequestBody PlantOrderRecommendationRequest request) {
        return catalogService.recommendOrder(request);
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return catalogService.getProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createCatalogItem(@RequestBody CreateCatalogItemRequest request) {
        return catalogService.createCatalogItem(request);
    }
}
