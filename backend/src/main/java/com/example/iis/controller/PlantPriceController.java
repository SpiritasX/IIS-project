package com.example.iis.controller;

import com.example.iis.dto.NewPlantPriceDto;
import com.example.iis.dto.PlantPriceResponse;
import com.example.iis.dto.RollbackPriceDto;
import com.example.iis.model.PlantPrice;
import com.example.iis.service.PlantPriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/price")
public class PlantPriceController {
    private PlantPriceService plantPriceService;

    public PlantPriceController(PlantPriceService plantPriceService){
        this.plantPriceService = plantPriceService;
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
    //@AuthenticationPrincipal umesto id u body-ju?

    @GetMapping("{plantId}/history")
    public ResponseEntity<List<PlantPriceResponse>> priceHistoryView (@PathVariable Long plantId){
        return ResponseEntity.ok(plantPriceService.priceHistoryView(plantId));
    }
}
