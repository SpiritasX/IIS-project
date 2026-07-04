package com.example.iis.controller;

import com.example.iis.dto.AddPlantLotRequest;
import com.example.iis.dto.DeletePlantRequest;
import com.example.iis.dto.DeletionLogEntry;
import com.example.iis.dto.DeletionReasonStatsPoint;
import com.example.iis.dto.PlantConditionLogEntry;
import com.example.iis.dto.PlantDetailResponse;
import com.example.iis.dto.PlantHealthLogEntry;
import com.example.iis.dto.PlantRelocationEntry;
import com.example.iis.dto.PlantRelocationLogEntry;
import com.example.iis.dto.StartRelocationRequest;
import com.example.iis.dto.UpdatePlantConditionRequest;
import com.example.iis.dto.UpdatePlantRequest;
import com.example.iis.dto.UpdateRelocationStateRequest;
import com.example.iis.dto.CreateNurserySiteRequest;
import com.example.iis.dto.CreateSectorRequest;
import com.example.iis.dto.CreateStorageSpaceRequest;
import com.example.iis.dto.UpdateSectorRequest;
import com.example.iis.dto.SectorSummaryResponse;
import com.example.iis.dto.StorageSpaceDetailResponse;
import com.example.iis.dto.StorageSpaceResponse;
import com.example.iis.dto.StorageSpaceTypeResponse;
import com.example.iis.dto.NurserySiteResponse;
import com.example.iis.dto.PlantCountByUnitPoint;
import com.example.iis.dto.PlantLotResponse;
import com.example.iis.dto.RelocationLogEntry;
import com.example.iis.dto.StockByVarietyPoint;
import com.example.iis.dto.UpdateStorageSpaceRequest;
import com.example.iis.dto.AddVarietyRequest;
import com.example.iis.dto.UpdateVarietyRequest;
import com.example.iis.dto.CategoryResponse;
import com.example.iis.dto.CategoryTreeItem;
import com.example.iis.dto.SpeciesResponse;
import com.example.iis.dto.TypeResponse;
import com.example.iis.dto.VarietyResponse;
import com.example.iis.dto.ConditionDistributionPoint;
import com.example.iis.dto.ConditionLogEntry;
import com.example.iis.dto.WorkerStatsResponse;
import com.example.iis.service.BotanistDashboardService;
import com.example.iis.service.LocationManagementService;
import com.example.iis.service.PlantVarietyService;
import com.example.iis.service.WorkerDashboardService;
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
@RequestMapping("/api/admin")
public class AdminController {

    private final WorkerDashboardService dashboardService;
    private final BotanistDashboardService botanistDashboardService;
    private final WorkerPlantService plantService;
    private final LocationManagementService locationService;
    private final PlantVarietyService varietyService;

    public AdminController(WorkerDashboardService dashboardService, BotanistDashboardService botanistDashboardService,
                           WorkerPlantService plantService, LocationManagementService locationService,
                           PlantVarietyService varietyService) {
        this.dashboardService = dashboardService;
        this.botanistDashboardService = botanistDashboardService;
        this.plantService = plantService;
        this.locationService = locationService;
        this.varietyService = varietyService;
    }

    @GetMapping("/sites")
    public ResponseEntity<List<NurserySiteResponse>> getSites() {
        return ResponseEntity.ok(dashboardService.getSites());
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<WorkerStatsResponse> getStats(
            @RequestParam(required = false) Long siteId
    ) {
        return ResponseEntity.ok(dashboardService.getStats(siteId));
    }

    @GetMapping("/dashboard/stock-by-variety")
    public ResponseEntity<List<StockByVarietyPoint>> getStockByVariety(
            @RequestParam(required = false) Long siteId
    ) {
        return ResponseEntity.ok(dashboardService.getStockByVariety(siteId));
    }

    @GetMapping("/dashboard/plant-count")
    public ResponseEntity<List<PlantCountByUnitPoint>> getPlantCountByUnit(
            @RequestParam(required = false) Long siteId
    ) {
        return ResponseEntity.ok(dashboardService.getPlantCountByUnit(siteId));
    }

    @GetMapping("/dashboard/relocation-logs")
    public ResponseEntity<List<RelocationLogEntry>> getRelocationLogs(
            @RequestParam(required = false) Long siteId
    ) {
        return ResponseEntity.ok(dashboardService.getRelocationLogs(siteId));
    }

    @GetMapping("/plants/all")
    public ResponseEntity<List<PlantDetailResponse>> getPlants() {
        return ResponseEntity.ok(plantService.getPlants());
    }

    @PutMapping("/plants/{id}")
    public ResponseEntity<PlantDetailResponse> updatePlant(@PathVariable Long id, @RequestBody UpdatePlantRequest request) {
        return ResponseEntity.ok(plantService.updatePlant(id, request));
    }

    @DeleteMapping("/plants/{id}")
    public ResponseEntity<Void> deletePlant(@PathVariable Long id, @RequestBody DeletePlantRequest request) {
        plantService.deletePlant(id, request.reason());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard/deletion-reasons")
    public ResponseEntity<List<DeletionReasonStatsPoint>> getDeletionReasonStats() {
        return ResponseEntity.ok(dashboardService.getDeletionReasonStats());
    }

    @GetMapping("/dashboard/condition-distribution")
    public ResponseEntity<List<ConditionDistributionPoint>> getConditionDistribution(@RequestParam(required = false) Long siteId) {
        return ResponseEntity.ok(botanistDashboardService.getConditionDistribution(siteId));
    }

    @GetMapping("/dashboard/plants-needing-attention")
    public ResponseEntity<List<ConditionLogEntry>> getPlantsNeedingAttention(@RequestParam(required = false) Long siteId) {
        return ResponseEntity.ok(botanistDashboardService.getPlantsNeedingAttention(siteId));
    }

    @GetMapping("/plants/{id}/health-logs")
    public ResponseEntity<List<PlantHealthLogEntry>> getPlantHealthLogs(@PathVariable Long id) {
        return ResponseEntity.ok(plantService.getPlantHealthLogs(id));
    }

    @GetMapping("/plants/{id}/relocation-history")
    public ResponseEntity<List<PlantRelocationEntry>> getPlantRelocationHistory(@PathVariable Long id) {
        return ResponseEntity.ok(plantService.getPlantRelocationHistory(id));
    }

    @PutMapping("/plants/{id}/condition")
    public ResponseEntity<PlantDetailResponse> updatePlantCondition(@PathVariable Long id, @RequestBody UpdatePlantConditionRequest request) {
        return ResponseEntity.ok(plantService.updatePlantCondition(id, request));
    }

    @GetMapping("/plants/{id}/condition-logs")
    public ResponseEntity<List<PlantConditionLogEntry>> getPlantConditionLogs(@PathVariable Long id) {
        return ResponseEntity.ok(plantService.getPlantConditionLogs(id));
    }

    @GetMapping("/plants/deletion-log")
    public ResponseEntity<List<DeletionLogEntry>> getDeletionLog() {
        return ResponseEntity.ok(plantService.getAllDeletionLogs());
    }

    @GetMapping("/plants/deletion-log/{id}/condition-history")
    public ResponseEntity<List<PlantConditionLogEntry>> getDeletionLogConditionHistory(@PathVariable Long id) {
        return ResponseEntity.ok(plantService.getConditionHistoryForDeletionLog(id));
    }

    @PostMapping("/plants/{id}/relocate")
    public ResponseEntity<PlantRelocationLogEntry> startRelocation(@PathVariable Long id, @RequestBody StartRelocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(plantService.startRelocation(id, request));
    }

    @PutMapping("/relocations/{id}/state")
    public ResponseEntity<PlantRelocationLogEntry> updateRelocationState(@PathVariable Long id, @RequestBody UpdateRelocationStateRequest request) {
        return ResponseEntity.ok(plantService.updateRelocationState(id, request));
    }

    @GetMapping("/plants/{id}/relocation-logs")
    public ResponseEntity<List<PlantRelocationLogEntry>> getPlantRelocationLogs(@PathVariable Long id) {
        return ResponseEntity.ok(plantService.getPlantRelocationLogs(id));
    }

    @GetMapping("/relocations/active")
    public ResponseEntity<List<PlantRelocationLogEntry>> getActiveRelocations() {
        return ResponseEntity.ok(plantService.getActiveRelocations());
    }

    @GetMapping("/plants/varieties")
    public ResponseEntity<List<VarietyResponse>> getVarieties() {
        return ResponseEntity.ok(plantService.getVarieties());
    }

    @GetMapping("/plants/compatible-storage-spaces")
    public ResponseEntity<List<StorageSpaceResponse>> getCompatibleStorageSpaces(@RequestParam Long varietyId) {
        return ResponseEntity.ok(plantService.getCompatibleStorageSpaces(varietyId));
    }

    @GetMapping("/plants/sectors")
    public ResponseEntity<List<SectorSummaryResponse>> getSectors(@RequestParam Long storageSpaceId) {
        return ResponseEntity.ok(plantService.getSectors(storageSpaceId));
    }

    @PostMapping("/plants")
    public ResponseEntity<PlantLotResponse> addPlantLot(@RequestBody AddPlantLotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(plantService.addPlantLot(request));
    }

    @GetMapping("/storage-space-types")
    public ResponseEntity<List<StorageSpaceTypeResponse>> getSpaceTypes() {
        return ResponseEntity.ok(locationService.getSpaceTypes());
    }

    @PostMapping("/storage-space-types")
    public ResponseEntity<StorageSpaceTypeResponse> createSpaceType(@RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createSpaceType(body.get("name")));
    }

    @PostMapping("/nursery-sites")
    public ResponseEntity<StorageSpaceDetailResponse> createNurserySite(@RequestBody CreateNurserySiteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createNurserySite(request));
    }

    @DeleteMapping("/nursery-sites/{id}")
    public ResponseEntity<Void> deleteNurserySite(@PathVariable Long id) {
        locationService.deleteNurserySite(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/locations")
    public ResponseEntity<List<StorageSpaceDetailResponse>> getLocations(@RequestParam(required = false) Long siteId) {
        return ResponseEntity.ok(locationService.getAll(siteId));
    }

    @GetMapping("/locations/{id}")
    public ResponseEntity<StorageSpaceDetailResponse> getLocation(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getById(id));
    }

    @PostMapping("/locations")
    public ResponseEntity<StorageSpaceDetailResponse> createLocation(@RequestBody CreateStorageSpaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.create(request));
    }

    @PutMapping("/locations/{id}")
    public ResponseEntity<StorageSpaceDetailResponse> updateLocation(@PathVariable Long id, @RequestBody UpdateStorageSpaceRequest request) {
        return ResponseEntity.ok(locationService.update(id, request));
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/locations/{id}/sectors")
    public ResponseEntity<StorageSpaceDetailResponse> addSector(@PathVariable Long id, @RequestBody CreateSectorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.addSector(id, request));
    }

    @PutMapping("/sectors/{id}")
    public ResponseEntity<StorageSpaceDetailResponse> updateSector(@PathVariable Long id, @RequestBody UpdateSectorRequest request) {
        return ResponseEntity.ok(locationService.updateSector(id, request));
    }

    @DeleteMapping("/sectors/{id}")
    public ResponseEntity<Void> deleteSector(@PathVariable Long id) {
        locationService.deleteSector(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/varieties")
    public ResponseEntity<List<VarietyResponse>> getVarietiesList() {
        return ResponseEntity.ok(varietyService.getAllVarieties());
    }

    @PostMapping("/varieties")
    public ResponseEntity<VarietyResponse> addVariety(@RequestBody AddVarietyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(varietyService.addVariety(request));
    }

    @PutMapping("/varieties/{id}")
    public ResponseEntity<VarietyResponse> updateVariety(@PathVariable Long id, @RequestBody UpdateVarietyRequest request) {
        return ResponseEntity.ok(varietyService.updateVariety(id, request));
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
    public ResponseEntity<List<StorageSpaceTypeResponse>> getTaxonomyStorageSpaces() {
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
