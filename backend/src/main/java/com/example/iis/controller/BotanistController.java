package com.example.iis.controller;

import com.example.iis.dto.AddVarietyRequest;
import com.example.iis.dto.CategoryResponse;
import com.example.iis.dto.DashboardLogEntry;
import com.example.iis.dto.LocationTypeResponse;
import com.example.iis.dto.DashboardStatsResponse;
import com.example.iis.dto.NurserySiteResponse;
import com.example.iis.dto.RelocationDataPoint;
import com.example.iis.dto.SpeciesResponse;
import com.example.iis.dto.StockDataPoint;
import com.example.iis.dto.TypeResponse;
import com.example.iis.dto.VarietyResponse;
import com.example.iis.service.BotanistDashboardService;
import com.example.iis.service.PlantVarietyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/botanist")
public class BotanistController {

    private final BotanistDashboardService service;
    private final PlantVarietyService varietyService;

    public BotanistController(BotanistDashboardService service, PlantVarietyService varietyService) {
        this.service = service;
        this.varietyService = varietyService;
    }

    @GetMapping("/sites")
    public ResponseEntity<List<NurserySiteResponse>> getSites() {
        return ResponseEntity.ok(service.getSites());
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsResponse> getStats(
            @RequestParam(required = false) Long siteId
    ) {
        return ResponseEntity.ok(service.getStats(siteId));
    }

    @GetMapping("/dashboard/stock")
    public ResponseEntity<List<StockDataPoint>> getStock(
            @RequestParam(required = false) Long siteId
    ) {
        return ResponseEntity.ok(service.getStockData(siteId));
    }

    @GetMapping("/dashboard/relocations")
    public ResponseEntity<List<RelocationDataPoint>> getRelocations(
            @RequestParam(required = false) Long siteId
    ) {
        return ResponseEntity.ok(service.getRelocationTimeline(siteId));
    }

    @GetMapping("/dashboard/logs")
    public ResponseEntity<List<DashboardLogEntry>> getLogs(
            @RequestParam(required = false) Long siteId
    ) {
        return ResponseEntity.ok(service.getRecentLogs(siteId));
    }

    @GetMapping("/varieties")
    public ResponseEntity<List<VarietyResponse>> getVarieties() {
        return ResponseEntity.ok(varietyService.getAllVarieties());
    }

    @PostMapping("/varieties")
    public ResponseEntity<VarietyResponse> addVariety(@RequestBody AddVarietyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(varietyService.addVariety(request));
    }

    @GetMapping("/taxonomy/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(varietyService.getCategories());
    }

    @GetMapping("/taxonomy/types")
    public ResponseEntity<List<TypeResponse>> getTypes(@RequestParam Long categoryId) {
        return ResponseEntity.ok(varietyService.getTypesByCategory(categoryId));
    }

    @GetMapping("/taxonomy/species")
    public ResponseEntity<List<SpeciesResponse>> getSpecies(@RequestParam Long typeId) {
        return ResponseEntity.ok(varietyService.getSpeciesByType(typeId));
    }

    @GetMapping("/taxonomy/location-types")
    public ResponseEntity<List<LocationTypeResponse>> getLocationTypes() {
        return ResponseEntity.ok(varietyService.getLocationTypes());
    }
}
