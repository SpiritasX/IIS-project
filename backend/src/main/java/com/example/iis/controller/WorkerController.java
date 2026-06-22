package com.example.iis.controller;

import com.example.iis.dto.AddPlantLotRequest;
import com.example.iis.dto.LocationParcelSummaryResponse;
import com.example.iis.dto.LocationTypeResponse;
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
@RequestMapping("/api/worker")
public class WorkerController {

    private final WorkerDashboardService service;
    private final WorkerPlantService plantService;

    public WorkerController(WorkerDashboardService service, WorkerPlantService plantService) {
        this.service = service;
        this.plantService = plantService;
    }

    @GetMapping("/sites")
    public ResponseEntity<List<NurserySiteResponse>> getSites() {
        return ResponseEntity.ok(service.getSites());
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<WorkerStatsResponse> getStats(
            @RequestParam(required = false) Long siteId
    ) {
        return ResponseEntity.ok(service.getStats(siteId));
    }

    @GetMapping("/dashboard/stock-by-variety")
    public ResponseEntity<List<StockByVarietyPoint>> getStockByVariety(
            @RequestParam(required = false) Long siteId
    ) {
        return ResponseEntity.ok(service.getStockByVariety(siteId));
    }

    @GetMapping("/dashboard/plant-count")
    public ResponseEntity<List<PlantCountByUnitPoint>> getPlantCountByUnit(
            @RequestParam(required = false) Long siteId
    ) {
        return ResponseEntity.ok(service.getPlantCountByUnit(siteId));
    }

    @GetMapping("/dashboard/relocation-logs")
    public ResponseEntity<List<RelocationLogEntry>> getRelocationLogs(
            @RequestParam(required = false) Long siteId
    ) {
        return ResponseEntity.ok(service.getRelocationLogs(siteId));
    }

    @GetMapping("/plants/varieties")
    public ResponseEntity<List<VarietyResponse>> getVarieties() {
        return ResponseEntity.ok(plantService.getVarieties());
    }

    @GetMapping("/plants/compatible-units")
    public ResponseEntity<List<LocationTypeResponse>> getCompatibleUnits(@RequestParam Long varietyId) {
        return ResponseEntity.ok(plantService.getCompatibleUnits(varietyId));
    }

    @GetMapping("/plants/location-parcels")
    public ResponseEntity<List<LocationParcelSummaryResponse>> getLocationParcels(@RequestParam Long unitId) {
        return ResponseEntity.ok(plantService.getLocationParcels(unitId));
    }

    @GetMapping("/plants/nursery-sites")
    public ResponseEntity<List<NurserySiteResponse>> getNurserySites(@RequestParam Long parcelId) {
        return ResponseEntity.ok(plantService.getNurserySites(parcelId));
    }

    @PostMapping("/plants")
    public ResponseEntity<PlantLotResponse> addPlantLot(@RequestBody AddPlantLotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(plantService.addPlantLot(request));
    }
}
