package com.example.iis.controller;

import com.example.iis.dto.DynamicPriceUpdateResponse;
import com.example.iis.dto.NewPlantPriceDto;
import com.example.iis.dto.PlantDemandResponse;
import com.example.iis.dto.PlantPriceResponse;
import com.example.iis.dto.RollbackPriceDto;
import com.example.iis.model.PlantPrice;
import com.example.iis.service.DynamicPricingService;
import com.example.iis.service.PlantPriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/price")
public class PlantPriceController {
    private PlantPriceService plantPriceService;
    private DynamicPricingService dynamicPricingService;

    public PlantPriceController(PlantPriceService plantPriceService, DynamicPricingService dynamicPricingService){
        this.plantPriceService = plantPriceService;
        this.dynamicPricingService = dynamicPricingService;
    }

    @PostMapping("/update/{plantId}")
    public ResponseEntity<PlantPriceResponse> setNewPlantPrice(@PathVariable Long plantId, @RequestBody NewPlantPriceDto request){
        PlantPrice newPrice = plantPriceService.setNewPlantPrice(plantId, request.price(),request.changedById());
        return ResponseEntity.ok(PlantPriceResponse.from(newPrice));
    }

    @PostMapping("/update/{plantId}/rollback")
    public ResponseEntity<PlantPriceResponse> rollbackPlantPrice (@PathVariable Long plantId, @RequestBody RollbackPriceDto request){
        PlantPrice newPrice = plantPriceService.rollbackPlantPrice(plantId, request.changedById());
        return ResponseEntity.ok(PlantPriceResponse.from(newPrice));
    }

    @PostMapping("/recalculate-dynamic")
    public ResponseEntity<List<DynamicPriceUpdateResponse>> recalculateDynamicPrices() {
        return ResponseEntity.ok(dynamicPricingService.recalculateAllPlantPrices());
    }

    //@AuthenticationPrincipal umesto id u body-ju?

    @GetMapping("/{plantId}/history")
    public ResponseEntity<List<PlantPriceResponse>> priceHistoryView (@PathVariable Long plantId){
        return ResponseEntity.ok(plantPriceService.priceHistoryView(plantId));
    }

    @GetMapping("/{plantId}/demand")
    public ResponseEntity<List<PlantDemandResponse>> plantDemandView (@PathVariable Long plantId){
        return ResponseEntity.ok(plantPriceService.plantDemandView(plantId));
    }
}
