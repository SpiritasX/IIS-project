package com.example.iis.controller;

import com.example.iis.dto.AddPlantLotRequest;
import com.example.iis.dto.DeletePlantRequest;
import com.example.iis.dto.DeletionReasonStatsPoint;
import com.example.iis.dto.PlantDetailResponse;
import com.example.iis.dto.UpdatePlantRequest;
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
import com.example.iis.dto.VarietyResponse;
import com.example.iis.dto.WorkerStatsResponse;
import com.example.iis.service.LocationManagementService;
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

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final WorkerDashboardService dashboardService;
    private final WorkerPlantService plantService;
    private final LocationManagementService locationService;

    public AdminController(WorkerDashboardService dashboardService, WorkerPlantService plantService, LocationManagementService locationService) {
        this.dashboardService = dashboardService;
        this.plantService = plantService;
        this.locationService = locationService;
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
}
