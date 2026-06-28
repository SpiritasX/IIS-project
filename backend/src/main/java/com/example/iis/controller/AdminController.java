package com.example.iis.controller;

import com.example.iis.dto.AddPlantLotRequest;
import com.example.iis.dto.SectorSummaryResponse;
import com.example.iis.dto.StorageSpaceResponse;
import com.example.iis.dto.NurserySiteResponse;
import com.example.iis.dto.PlantCountByUnitPoint;
import com.example.iis.dto.PlantLotResponse;
import com.example.iis.dto.RelocationLogEntry;
import com.example.iis.dto.StockByVarietyPoint;
import com.example.iis.dto.VarietyResponse;
import com.example.iis.dto.WorkerStatsResponse;
import com.example.iis.service.WorkerDashboardService;
import com.example.iis.service.WorkerPlantService;
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
@RequestMapping("/api/admin")
public class AdminController {

    private final WorkerDashboardService dashboardService;
    private final WorkerPlantService plantService;

    public AdminController(WorkerDashboardService dashboardService, WorkerPlantService plantService) {
        this.dashboardService = dashboardService;
        this.plantService = plantService;
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

    @GetMapping("/plants/nursery-sites")
    public ResponseEntity<List<NurserySiteResponse>> getNurserySites(@RequestParam Long sectorId) {
        return ResponseEntity.ok(plantService.getNurserySites(sectorId));
    }

    @PostMapping("/plants")
    public ResponseEntity<PlantLotResponse> addPlantLot(@RequestBody AddPlantLotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(plantService.addPlantLot(request));
    }
}
