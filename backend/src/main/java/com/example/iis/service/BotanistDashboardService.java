package com.example.iis.service;

import com.example.iis.dto.DashboardLogEntry;
import com.example.iis.dto.DashboardStatsResponse;
import com.example.iis.dto.NurserySiteResponse;
import com.example.iis.dto.RelocationDataPoint;
import com.example.iis.dto.StockDataPoint;
import com.example.iis.model.HealthLog;
import com.example.iis.model.RelocationHistory;
import com.example.iis.model.RemovalLog;
import com.example.iis.repository.HealthLogRepository;
import com.example.iis.repository.NurserySiteRepository;
import com.example.iis.repository.RelocationHistoryRepository;
import com.example.iis.repository.RemovalLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BotanistDashboardService {

    private final NurserySiteRepository nurserySiteRepository;
    private final RelocationHistoryRepository relocationHistoryRepository;
    private final HealthLogRepository healthLogRepository;
    private final RemovalLogRepository removalLogRepository;

    public BotanistDashboardService(
            NurserySiteRepository nurserySiteRepository,
            RelocationHistoryRepository relocationHistoryRepository,
            HealthLogRepository healthLogRepository,
            RemovalLogRepository removalLogRepository
    ) {
        this.nurserySiteRepository = nurserySiteRepository;
        this.relocationHistoryRepository = relocationHistoryRepository;
        this.healthLogRepository = healthLogRepository;
        this.removalLogRepository = removalLogRepository;
    }

    public List<NurserySiteResponse> getSites() {
        return nurserySiteRepository.findAll().stream()
                .map(s -> new NurserySiteResponse(s.getId(), s.getName(), s.getAddress()))
                .toList();
    }

    public DashboardStatsResponse getStats(Long siteId) {
        long varietiesCount;
        long totalPlants;
        long activeRelocations;
        long nurserySitesCount;

        if (siteId != null) {
            List<RelocationHistory> active = relocationHistoryRepository.findByNurserySite_IdAndEndTimeIsNull(siteId);
            activeRelocations = active.size();
            totalPlants = active.stream().map(rh -> rh.getPlant().getId()).distinct().count();
            varietiesCount = active.stream().map(rh -> rh.getPlant().getVariety().getId()).distinct().count();
            nurserySitesCount = 1;
        } else {
            List<RelocationHistory> active = relocationHistoryRepository.findByEndTimeIsNull();
            activeRelocations = active.size();
            totalPlants = active.stream().map(rh -> rh.getPlant().getId()).distinct().count();
            varietiesCount = active.stream().map(rh -> rh.getPlant().getVariety().getId()).distinct().count();
            nurserySitesCount = nurserySiteRepository.count();
        }

        return new DashboardStatsResponse(varietiesCount, nurserySitesCount, totalPlants, activeRelocations);
    }

    public List<StockDataPoint> getStockData(Long siteId) {
        List<RelocationHistory> active = (siteId != null)
                ? relocationHistoryRepository.findByNurserySite_IdAndEndTimeIsNull(siteId)
                : relocationHistoryRepository.findByEndTimeIsNull();

        return active.stream()
                .map(rh -> new StockDataPoint(
                        rh.getPlant().getName(),
                        rh.getPlant().getVariety().getName(),
                        rh.getInStock()
                ))
                .toList();
    }

    public List<RelocationDataPoint> getRelocationTimeline(Long siteId) {
        List<RelocationHistory> all = (siteId != null)
                ? relocationHistoryRepository.findByNurserySite_Id(siteId)
                : relocationHistoryRepository.findAll();

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Long> grouped = all.stream()
                .collect(Collectors.groupingBy(
                        rh -> fmt.format(rh.getStartTime()),
                        Collectors.counting()
                ));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new RelocationDataPoint(e.getKey(), e.getValue()))
                .toList();
    }

    public List<DashboardLogEntry> getRecentLogs(Long siteId) {
        List<Long> plantIds = (siteId != null)
                ? relocationHistoryRepository.findActivePlantIdsBySite(siteId)
                : relocationHistoryRepository.findAllActivePlantIds();

        List<DashboardLogEntry> logs = new ArrayList<>();

        if (!plantIds.isEmpty()) {
            for (HealthLog hl : healthLogRepository.findTop20ByPlant_IdInOrderByTimestampDesc(plantIds)) {
                logs.add(new DashboardLogEntry(hl.getTimestamp(), "HEALTH", hl.getPlant().getName(), hl.getHealthGrade()));
            }
            for (RemovalLog rl : removalLogRepository.findByPlant_IdIn(plantIds)) {
                logs.add(new DashboardLogEntry(rl.getTimestamp(), "REMOVAL", rl.getPlant().getName(), rl.getReason()));
            }
        } else {
            for (HealthLog hl : healthLogRepository.findTop20ByOrderByTimestampDesc()) {
                logs.add(new DashboardLogEntry(hl.getTimestamp(), "HEALTH", hl.getPlant().getName(), hl.getHealthGrade()));
            }
        }

        return logs.stream()
                .sorted(Comparator.comparing(DashboardLogEntry::timestamp).reversed())
                .limit(20)
                .toList();
    }
}
