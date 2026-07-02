package com.example.iis.service;

import com.example.iis.dto.DeletionReasonStatsPoint;
import com.example.iis.dto.NurserySiteResponse;
import com.example.iis.dto.PlantCountByUnitPoint;
import com.example.iis.dto.RelocationLogEntry;
import com.example.iis.dto.StockByVarietyPoint;
import com.example.iis.dto.WorkerStatsResponse;
import com.example.iis.model.DeletionReason;
import com.example.iis.model.RelocationHistory;
import com.example.iis.repository.PlantDeletionLogRepository;
import com.example.iis.repository.StorageSpaceRepository;
import com.example.iis.repository.NurserySiteRepository;
import com.example.iis.repository.PlantRepository;
import com.example.iis.repository.PlantVarietyRepository;
import com.example.iis.repository.RelocationHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class WorkerDashboardService {

    private final NurserySiteRepository nurserySiteRepository;
    private final RelocationHistoryRepository relocationHistoryRepository;
    private final PlantVarietyRepository plantVarietyRepository;
    private final PlantRepository plantRepository;
    private final StorageSpaceRepository storageSpaceRepository;
    private final PlantDeletionLogRepository plantDeletionLogRepository;

    public WorkerDashboardService(
            NurserySiteRepository nurserySiteRepository,
            RelocationHistoryRepository relocationHistoryRepository,
            PlantVarietyRepository plantVarietyRepository,
            PlantRepository plantRepository,
            StorageSpaceRepository storageSpaceRepository,
            PlantDeletionLogRepository plantDeletionLogRepository
    ) {
        this.nurserySiteRepository = nurserySiteRepository;
        this.relocationHistoryRepository = relocationHistoryRepository;
        this.plantVarietyRepository = plantVarietyRepository;
        this.plantRepository = plantRepository;
        this.storageSpaceRepository = storageSpaceRepository;
        this.plantDeletionLogRepository = plantDeletionLogRepository;
    }

    public List<NurserySiteResponse> getSites() {
        return nurserySiteRepository.findAll().stream()
                .map(s -> new NurserySiteResponse(s.getId(), s.getName(), s.getAddress(),
                        storageSpaceRepository.countByNurserySite_Id(s.getId())))
                .toList();
    }

    public WorkerStatsResponse getStats(Long siteId) {
        if (siteId != null) {
            List<RelocationHistory> active = relocationHistoryRepository.findByNurserySite_IdAndEndTimeIsNull(siteId);
            long varietiesCount = active.stream().map(rh -> rh.getPlant().getVariety().getId()).distinct().count();
            long plantsCount = active.stream().map(rh -> rh.getPlant().getId()).distinct().count();
            long activeRelocations = active.size();
            return new WorkerStatsResponse(varietiesCount, storageSpaceRepository.count(), plantsCount, activeRelocations);
        }

        return new WorkerStatsResponse(
                plantVarietyRepository.count(),
                storageSpaceRepository.count(),
                plantRepository.count(),
                relocationHistoryRepository.countByEndTimeIsNull()
        );
    }

    public List<StockByVarietyPoint> getStockByVariety(Long siteId) {
        List<RelocationHistory> active = siteId != null
                ? relocationHistoryRepository.findByNurserySite_IdAndEndTimeIsNull(siteId)
                : relocationHistoryRepository.findByEndTimeIsNull();

        Map<String, Long> byVariety = active.stream()
                .collect(Collectors.groupingBy(
                        rh -> rh.getPlant().getVariety().getName(),
                        Collectors.summingLong(rh -> rh.getInStock() != null ? rh.getInStock() : 0)
                ));

        return byVariety.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new StockByVarietyPoint(e.getKey(), e.getValue()))
                .toList();
    }

    public List<PlantCountByUnitPoint> getPlantCountByUnit(Long siteId) {
        List<RelocationHistory> active = siteId != null
                ? relocationHistoryRepository.findByNurserySite_IdAndEndTimeIsNull(siteId)
                : relocationHistoryRepository.findByEndTimeIsNull();

        Map<String, Set<Long>> plantsByUnit = new HashMap<>();
        for (RelocationHistory rh : active) {
            String unitName = rh.getSector().getStorageSpace().getName();
            plantsByUnit.computeIfAbsent(unitName, k -> new HashSet<>()).add(rh.getPlant().getId());
        }

        return plantsByUnit.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new PlantCountByUnitPoint(e.getKey(), (long) e.getValue().size()))
                .toList();
    }

    public List<DeletionReasonStatsPoint> getDeletionReasonStats() {
        Map<DeletionReason, Long> counts = new EnumMap<>(DeletionReason.class);
        for (Object[] row : plantDeletionLogRepository.countGroupedByReason()) {
            counts.put((DeletionReason) row[0], (Long) row[1]);
        }
        return Arrays.stream(DeletionReason.values())
                .map(r -> new DeletionReasonStatsPoint(r.getLabel(), counts.getOrDefault(r, 0L)))
                .toList();
    }

    public List<RelocationLogEntry> getRelocationLogs(Long siteId) {
        List<RelocationHistory> logs = siteId != null
                ? relocationHistoryRepository.findTop20ByNurserySite_IdOrderByStartTimeDesc(siteId)
                : relocationHistoryRepository.findTop20ByOrderByStartTimeDesc();

        return logs.stream()
                .map(rh -> new RelocationLogEntry(
                        rh.getStartTime(),
                        rh.getEndTime(),
                        rh.getPlant().getName(),
                        rh.getSector().getName(),
                        rh.getReason(),
                        rh.getInStock()
                ))
                .toList();
    }
}
