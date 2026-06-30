package com.example.iis.service;

import com.example.iis.dto.AveragePhaseDurationResponse;
import com.example.iis.dto.LongestPhaseOrderResponse;
import com.example.iis.dto.OrderAnalysisResponse;
import com.example.iis.dto.PhaseCountResponse;
import com.example.iis.dto.TransitionRateResponse;
import com.example.iis.repository.ProcessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderAnalysisService {
    private final ProcessRepository processRepository;

    public OrderAnalysisService(ProcessRepository processRepository) {
        this.processRepository = processRepository;
    }

    @Transactional(readOnly = true)
    public OrderAnalysisResponse getOrderAnalysis() {
        return new OrderAnalysisResponse(
                processRepository.findActivePhaseCounts()
                        .stream()
                        .map(this::toPhaseCountResponse)
                        .toList(),
                processRepository.findTransitionRates()
                        .stream()
                        .map(this::toTransitionRateResponse)
                        .toList(),
                processRepository.findAveragePhaseDurations()
                        .stream()
                        .map(this::toAveragePhaseDurationResponse)
                        .toList(),
                processRepository.findLongestActiveOrdersByPhase()
                        .stream()
                        .map(this::toLongestPhaseOrderResponse)
                        .toList()
        );
    }

    private PhaseCountResponse toPhaseCountResponse(ProcessRepository.PhaseCountView view) {
        return new PhaseCountResponse(
                view.getPhaseName(),
                valueOrZero(view.getOrderCount())
        );
    }

    private TransitionRateResponse toTransitionRateResponse(ProcessRepository.TransitionRateView view) {
        long reachedCount = valueOrZero(view.getReachedCount());
        long transitionedCount = valueOrZero(view.getTransitionedCount());

        return new TransitionRateResponse(
                view.getFromPhase(),
                view.getToPhase(),
                reachedCount,
                transitionedCount,
                valueOrZero(view.getRatePercent())
        );
    }

    private AveragePhaseDurationResponse toAveragePhaseDurationResponse(ProcessRepository.AverageDurationView view) {
        return new AveragePhaseDurationResponse(
                view.getPhaseName(),
                valueOrZero(view.getAverageDurationSeconds()),
                valueOrZero(view.getSampleCount())
        );
    }

    private LongestPhaseOrderResponse toLongestPhaseOrderResponse(ProcessRepository.LongestActiveOrderView view) {
        return new LongestPhaseOrderResponse(
                view.getPhaseName(),
                view.getPhaseRank(),
                view.getProcessId(),
                view.getOfferId(),
                view.getCustomerId(),
                view.getCustomerName(),
                view.getCustomerEmail(),
                formatDateTime(view.getPhaseStartTime()),
                valueOrZero(view.getDurationSeconds())
        );
    }

    private long valueOrZero(Long value) {
        return value == null ? 0 : value;
    }

    private double valueOrZero(Double value) {
        return value == null ? 0 : value;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime.atOffset(ZoneOffset.UTC));
    }
}
