package com.example.iis.controller;

import com.example.iis.dto.AddVarietyRequest;
import com.example.iis.dto.CategoryResponse;
import com.example.iis.dto.PlantDetailResponse;
import com.example.iis.dto.CategoryTreeItem;
import com.example.iis.dto.DashboardLogEntry;
import com.example.iis.dto.DashboardStatsResponse;
import com.example.iis.dto.NurserySiteResponse;
import com.example.iis.dto.RelocationDataPoint;
import com.example.iis.dto.SpeciesResponse;
import com.example.iis.dto.StockDataPoint;
import com.example.iis.dto.StorageSpaceTypeResponse;
import com.example.iis.dto.TypeResponse;
import com.example.iis.dto.VarietyResponse;
import com.example.iis.service.BotanistDashboardService;
import com.example.iis.service.PlantVarietyService;
import com.example.iis.service.WorkerPlantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/botanist")
public class BotanistController {

    private final BotanistDashboardService service;
    private final PlantVarietyService varietyService;
    private final WorkerPlantService plantService;

    public BotanistController(BotanistDashboardService service, PlantVarietyService varietyService, WorkerPlantService plantService) {
        this.service = service;
        this.varietyService = varietyService;
        this.plantService = plantService;
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

    @GetMapping("/plants/all")
    public ResponseEntity<List<PlantDetailResponse>> getPlants() {
        return ResponseEntity.ok(plantService.getPlants());
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

    @GetMapping("/taxonomy/storage-spaces")
    public ResponseEntity<List<StorageSpaceTypeResponse>> getStorageSpaces() {
        return ResponseEntity.ok(varietyService.getStorageSpaces());
    }

    @GetMapping("/taxonomy/tree")
    public ResponseEntity<List<CategoryTreeItem>> getCategoryTree() {
        return ResponseEntity.ok(varietyService.getCategoryTree());
    }

    @PostMapping("/taxonomy/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(varietyService.createCategory(body.get("name")));
    }

    @PutMapping("/taxonomy/categories/{id}")
    public ResponseEntity<CategoryResponse> renameCategory(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(varietyService.renameCategory(id, body.get("name")));
    }

    @DeleteMapping("/taxonomy/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        varietyService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/taxonomy/types")
    public ResponseEntity<TypeResponse> createType(@RequestBody Map<String, Object> body) {
        Long categoryId = Long.valueOf(body.get("categoryId").toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(varietyService.createType(categoryId, (String) body.get("name")));
    }

    @PutMapping("/taxonomy/types/{id}")
    public ResponseEntity<TypeResponse> renameType(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(varietyService.renameType(id, body.get("name")));
    }

    @DeleteMapping("/taxonomy/types/{id}")
    public ResponseEntity<Void> deleteType(@PathVariable Long id) {
        varietyService.deleteType(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/taxonomy/species")
    public ResponseEntity<SpeciesResponse> createSpecies(@RequestBody Map<String, Object> body) {
        Long typeId = Long.valueOf(body.get("typeId").toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(varietyService.createSpecies(typeId, (String) body.get("name")));
    }

    @PutMapping("/taxonomy/species/{id}")
    public ResponseEntity<SpeciesResponse> renameSpecies(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(varietyService.renameSpecies(id, body.get("name")));
    }

    @DeleteMapping("/taxonomy/species/{id}")
    public ResponseEntity<Void> deleteSpecies(@PathVariable Long id) {
        varietyService.deleteSpecies(id);
        return ResponseEntity.noContent().build();
    }
}
