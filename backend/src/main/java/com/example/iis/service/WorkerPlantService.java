package com.example.iis.service;

import com.example.iis.dto.AddPlantLotRequest;
import com.example.iis.dto.DeletionLogEntry;
import com.example.iis.dto.PlantConditionLogEntry;
import com.example.iis.dto.PlantDetailResponse;
import com.example.iis.dto.PlantHealthLogEntry;
import com.example.iis.dto.PlantRelocationEntry;
import com.example.iis.dto.PlantRelocationLogEntry;
import com.example.iis.dto.SectorSummaryResponse;
import com.example.iis.dto.StartRelocationRequest;
import com.example.iis.dto.UpdatePlantConditionRequest;
import com.example.iis.dto.UpdatePlantRequest;
import com.example.iis.dto.UpdateRelocationStateRequest;
import com.example.iis.dto.StorageSpaceResponse;
import com.example.iis.dto.PlantLotResponse;
import com.example.iis.dto.VarietyResponse;
import com.example.iis.model.DeletionReason;
import com.example.iis.model.NurserySite;
import com.example.iis.model.PlantConditionLog;
import com.example.iis.model.PlantDeletionLog;
import com.example.iis.model.PlantRelocationLog;
import com.example.iis.model.RelocationState;
import com.example.iis.model.Sector;
import com.example.iis.model.StorageSpace;
import com.example.iis.model.Plant;
import com.example.iis.model.PlantSpecies;
import com.example.iis.model.PlantVariety;
import com.example.iis.model.RelocationHistory;
import com.example.iis.repository.HealthLogRepository;
import com.example.iis.repository.PlantConditionLogRepository;
import com.example.iis.repository.PlantDeletionLogRepository;
import com.example.iis.repository.PlantPriceRepository;
import com.example.iis.repository.PlantRelocationLogRepository;
import com.example.iis.repository.RemovalLogRepository;
import com.example.iis.repository.SectorRepository;
import com.example.iis.repository.StorageSpaceRepository;
import com.example.iis.repository.PlantRepository;
import com.example.iis.repository.PlantVarietyRepository;
import com.example.iis.repository.RelocationHistoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class WorkerPlantService {

    private final PlantVarietyRepository varietyRepository;
    private final StorageSpaceRepository storageSpaceRepository;
    private final SectorRepository sectorRepository;
    private final PlantRepository plantRepository;
    private final RelocationHistoryRepository relocationHistoryRepository;
    private final PlantPriceRepository plantPriceRepository;
    private final RemovalLogRepository removalLogRepository;
    private final PlantDeletionLogRepository plantDeletionLogRepository;
    private final HealthLogRepository healthLogRepository;
    private final PlantConditionLogRepository plantConditionLogRepository;
    private final PlantRelocationLogRepository plantRelocationLogRepository;

    public WorkerPlantService(
            PlantVarietyRepository varietyRepository,
            StorageSpaceRepository storageSpaceRepository,
            SectorRepository sectorRepository,
            PlantRepository plantRepository,
            RelocationHistoryRepository relocationHistoryRepository,
            PlantPriceRepository plantPriceRepository,
            RemovalLogRepository removalLogRepository,
            PlantDeletionLogRepository plantDeletionLogRepository,
            HealthLogRepository healthLogRepository,
            PlantConditionLogRepository plantConditionLogRepository,
            PlantRelocationLogRepository plantRelocationLogRepository
    ) {
        this.varietyRepository = varietyRepository;
        this.storageSpaceRepository = storageSpaceRepository;
        this.sectorRepository = sectorRepository;
        this.plantRepository = plantRepository;
        this.relocationHistoryRepository = relocationHistoryRepository;
        this.plantPriceRepository = plantPriceRepository;
        this.removalLogRepository = removalLogRepository;
        this.plantDeletionLogRepository = plantDeletionLogRepository;
        this.healthLogRepository = healthLogRepository;
        this.plantConditionLogRepository = plantConditionLogRepository;
        this.plantRelocationLogRepository = plantRelocationLogRepository;
    }

    public List<PlantDetailResponse> getPlants() {
        List<Plant> plants = plantRepository.findAll();
        Map<Long, RelocationHistory> activeByPlant = relocationHistoryRepository.findByEndTimeIsNull()
                .stream()
                .collect(Collectors.toMap(
                        rh -> rh.getPlant().getId(),
                        rh -> rh,
                        (a, b) -> a.getStartTime().after(b.getStartTime()) ? a : b
                ));
        return plants.stream().map(p -> {
            PlantVariety v = p.getVariety();
            PlantSpecies sp = v.getSpecies();
            RelocationHistory rh = activeByPlant.get(p.getId());
            Sector sector = rh != null ? rh.getSector() : null;
            return new PlantDetailResponse(
                    p.getId(), p.getName(), p.getDescription(), p.getState(), p.getConditionDescription(),
                    p.getPropagationMethod(), p.getHatchingDate(), p.getColor(), p.getHeight(),
                    v.getId(), v.getName(), v.getLatinName(),
                    v.getHumidity(), v.getSoil(), v.getInstructions(),
                    v.getStorageSpaceType() != null ? v.getStorageSpaceType().getName() : null,
                    sp.getName(), sp.getType().getName(), sp.getType().getCategory().getName(),
                    sector != null ? sector.getName() : null,
                    sector != null ? sector.getStorageSpace().getName() : null,
                    rh != null && rh.getNurserySite() != null ? rh.getNurserySite().getName() : null,
                    rh != null ? rh.getInStock() : null
            );
        }).toList();
    }

    public List<VarietyResponse> getVarieties() {
        return varietyRepository.findAll().stream()
                .map(v -> new VarietyResponse(
                        v.getId(), v.getName(), v.getLatinName(), v.getHumidity(), v.getSoil(), v.getInstructions(),
                        v.getSpecies().getId(), v.getSpecies().getName(),
                        v.getSpecies().getType().getId(), v.getSpecies().getType().getName(),
                        v.getSpecies().getType().getCategory().getId(), v.getSpecies().getType().getCategory().getName(),
                        v.getStorageSpaceType().getId(), v.getStorageSpaceType().getName()
                ))
                .toList();
    }

    public List<StorageSpaceResponse> getCompatibleStorageSpaces(Long varietyId) {
        PlantVariety variety = varietyRepository.findById(varietyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variety not found"));
        String requiredType = variety.getStorageSpaceType().getName();
        return storageSpaceRepository.findByType(requiredType).stream()
                .map(u -> new StorageSpaceResponse(
                        u.getId(), u.getName(), u.getType(),
                        u.getNurserySite() != null ? u.getNurserySite().getId() : null,
                        u.getNurserySite() != null ? u.getNurserySite().getName() : null
                ))
                .toList();
    }

    public List<SectorSummaryResponse> getSectors(Long storageSpaceId) {
        return sectorRepository.findByStorageSpace_Id(storageSpaceId).stream()
                .map(s -> new SectorSummaryResponse(s.getId(), s.getName(), s.getCapacity(), storageSpaceId))
                .toList();
    }

    @Transactional
    public PlantLotResponse addPlantLot(AddPlantLotRequest request) {
        PlantVariety variety = varietyRepository.findById(request.varietyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Variety not found"));
        StorageSpace storageSpace = storageSpaceRepository.findById(request.storageSpaceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Storage space not found"));
        Sector sector = sectorRepository.findById(request.sectorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sector not found"));

        if (!sector.getStorageSpace().getId().equals(storageSpace.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sector does not belong to the selected storage space");
        }

        if (sector.getCapacity() != null && request.quantity() != null) {
            long occupied = relocationHistoryRepository.sumActiveStockBySector(sector.getId());
            if (occupied + request.quantity() > sector.getCapacity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Quantity exceeds sector capacity (capacity: " + sector.getCapacity()
                        + ", currently occupied: " + occupied + ")");
            }
        }

        String plantName = (request.name() != null && !request.name().isBlank())
                ? request.name()
                : variety.getName() + " lot";

        Plant plant = new Plant(plantName, null,
                request.propagationMethod() != null ? request.propagationMethod() : null,
                "Available", variety);
        if (request.state() != null && (request.state() < 1 || request.state() > 5)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "State must be between 1 and 5");
        }
        plant.setHatchingDate(request.hatchingDate());
        plant.setColor(request.color());
        plant.setHeight(request.height());
        plant.setState(request.state());
        plant.setConditionDescription(request.conditionDescription());
        plant = plantRepository.save(plant);

        RelocationHistory rh = new RelocationHistory("Added to nursery", plant, sector,
                request.quantity() != null ? request.quantity() : 0L);
        NurserySite site = sector.getStorageSpace().getNurserySite();
        if (site != null) rh.setNurserySite(site);
        relocationHistoryRepository.save(rh);

        Optional<Long> activeStock = relocationHistoryRepository
                .findActiveStockByPlantIds(List.of(plant.getId()))
                .stream().findFirst().map(v -> v.getAvailableQuantity());

        return new PlantLotResponse(
                plant.getId(), plant.getName(),
                variety.getName(),
                storageSpace.getName(), sector.getName(),
                activeStock.orElse(request.quantity()),
                plant.getPropagationMethod(),
                plant.getHatchingDate(), plant.getColor(), plant.getHeight(),
                plant.getState(), plant.getConditionDescription()
        );
    }

    @Transactional
    public PlantDetailResponse updatePlant(Long id, UpdatePlantRequest request) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found"));
        if (request.state() != null && (request.state() < 1 || request.state() > 5)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "State must be between 1 and 5");
        }
        if (request.name() != null && !request.name().isBlank()) {
            plant.setName(request.name().trim());
        }
        plant.setPropagationMethod(request.propagationMethod());
        plant.setHatchingDate(request.hatchingDate());
        plant.setColor(request.color());
        plant.setHeight(request.height());
        plant.setState(request.state());
        plant.setConditionDescription(request.conditionDescription());
        plant = plantRepository.save(plant);

        Long updatedQuantity = null;

        if (request.quantity() != null) {
            if (request.quantity() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity cannot be negative");
            }
            List<RelocationHistory> active = relocationHistoryRepository
                    .findByPlant_IdAndEndTimeIsNullOrderByStartTimeAsc(id);
            if (!active.isEmpty()) {
                RelocationHistory activeRh = active.get(active.size() - 1);
                Sector sec = activeRh.getSector();
                if (sec != null && sec.getCapacity() != null) {
                    long occupied = relocationHistoryRepository.sumActiveStockBySector(sec.getId()) - activeRh.getInStock();
                    if (occupied + request.quantity() > sec.getCapacity()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Quantity exceeds sector capacity (capacity: " + sec.getCapacity()
                                + ", currently occupied by others: " + occupied + ")");
                    }
                }
                activeRh.setInStock(request.quantity());
                relocationHistoryRepository.save(activeRh);
                updatedQuantity = request.quantity();
            }
        }

        Map<Long, RelocationHistory> activeByPlant = relocationHistoryRepository.findByEndTimeIsNull()
                .stream()
                .collect(Collectors.toMap(
                        rh -> rh.getPlant().getId(),
                        rh -> rh,
                        (a, b) -> a.getStartTime().after(b.getStartTime()) ? a : b
                ));
        PlantVariety v = plant.getVariety();
        PlantSpecies sp = v.getSpecies();
        RelocationHistory rh = activeByPlant.get(plant.getId());
        Sector sector = rh != null ? rh.getSector() : null;
        Long responseQuantity = updatedQuantity != null ? updatedQuantity : (rh != null ? rh.getInStock() : null);
        return new PlantDetailResponse(
                plant.getId(), plant.getName(), plant.getDescription(), plant.getState(), plant.getConditionDescription(),
                plant.getPropagationMethod(), plant.getHatchingDate(), plant.getColor(), plant.getHeight(),
                v.getId(), v.getName(), v.getLatinName(),
                v.getHumidity(), v.getSoil(), v.getInstructions(),
                v.getStorageSpaceType() != null ? v.getStorageSpaceType().getName() : null,
                sp.getName(), sp.getType().getName(), sp.getType().getCategory().getName(),
                sector != null ? sector.getName() : null,
                sector != null ? sector.getStorageSpace().getName() : null,
                rh != null && rh.getNurserySite() != null ? rh.getNurserySite().getName() : null,
                responseQuantity
        );
    }

    public List<PlantHealthLogEntry> getPlantHealthLogs(Long plantId) {
        return healthLogRepository.findByPlant_IdOrderByTimestampDesc(plantId).stream()
                .map(hl -> new PlantHealthLogEntry(
                        hl.getId(),
                        hl.getLifeStage(),
                        hl.getHealthGrade(),
                        hl.getDescription(),
                        hl.getTimestamp() != null ? hl.getTimestamp().toInstant().toString() : null,
                        hl.getBotanist().getFirstName() + " " + hl.getBotanist().getLastName()
                ))
                .toList();
    }

    public List<PlantRelocationEntry> getPlantRelocationHistory(Long plantId) {
        return relocationHistoryRepository.findByPlant_IdOrderByStartTimeDesc(plantId).stream()
                .map(rh -> new PlantRelocationEntry(
                        rh.getId(),
                        rh.getReason(),
                        rh.getSector() != null ? rh.getSector().getName() : null,
                        rh.getSector() != null ? rh.getSector().getStorageSpace().getName() : null,
                        rh.getNurserySite() != null ? rh.getNurserySite().getName() : null,
                        rh.getInStock(),
                        rh.getStartTime() != null ? rh.getStartTime().toInstant().toString() : null,
                        rh.getEndTime() != null ? rh.getEndTime().toInstant().toString() : null,
                        rh.getWorker() != null ? rh.getWorker().getFirstName() + " " + rh.getWorker().getLastName() : null
                ))
                .toList();
    }

    @Transactional
    public PlantDetailResponse updatePlantCondition(Long id, UpdatePlantConditionRequest request) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found"));
        if (request.state() != null && (request.state() < 1 || request.state() > 5)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "State must be between 1 and 5");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        plant.setState(request.state());
        plant.setConditionDescription(request.conditionDescription());
        plant.setColor(request.color());
        plant.setHeight(request.height());
        plant = plantRepository.save(plant);
        plantConditionLogRepository.save(new PlantConditionLog(
                plant, request.state(), request.conditionDescription(),
                request.color(), request.height(), username
        ));

        Map<Long, RelocationHistory> activeByPlant = relocationHistoryRepository.findByEndTimeIsNull()
                .stream()
                .collect(Collectors.toMap(
                        rh -> rh.getPlant().getId(),
                        rh -> rh,
                        (a, b) -> a.getStartTime().after(b.getStartTime()) ? a : b
                ));
        PlantVariety v = plant.getVariety();
        PlantSpecies sp = v.getSpecies();
        RelocationHistory rh = activeByPlant.get(plant.getId());
        Sector sector = rh != null ? rh.getSector() : null;
        return new PlantDetailResponse(
                plant.getId(), plant.getName(), plant.getDescription(), plant.getState(), plant.getConditionDescription(),
                plant.getPropagationMethod(), plant.getHatchingDate(), plant.getColor(), plant.getHeight(),
                v.getId(), v.getName(), v.getLatinName(),
                v.getHumidity(), v.getSoil(), v.getInstructions(),
                v.getStorageSpaceType() != null ? v.getStorageSpaceType().getName() : null,
                sp.getName(), sp.getType().getName(), sp.getType().getCategory().getName(),
                sector != null ? sector.getName() : null,
                sector != null ? sector.getStorageSpace().getName() : null,
                rh != null && rh.getNurserySite() != null ? rh.getNurserySite().getName() : null,
                rh != null ? rh.getInStock() : null
        );
    }

    public List<PlantConditionLogEntry> getPlantConditionLogs(Long plantId) {
        return plantConditionLogRepository.findByPlant_IdOrderByChangedAtDesc(plantId).stream()
                .map(l -> new PlantConditionLogEntry(
                        l.getId(), l.getConditionState(), l.getConditionDescription(),
                        l.getColor(), l.getHeight(),
                        l.getChangedAt().toString(), l.getChangedBy()
                ))
                .toList();
    }

    public List<DeletionLogEntry> getAllDeletionLogs() {
        return plantDeletionLogRepository.findAllByOrderByDeletedAtDesc().stream()
                .map(l -> new DeletionLogEntry(
                        l.getId(), l.getPlantName(), l.getVarietyName(),
                        l.getReason().getLabel(),
                        l.getDeletedAt().toInstant().toString(),
                        l.getDeletedBy()
                ))
                .toList();
    }

    @Transactional
    public void deletePlant(Long id, String reason) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found"));

        DeletionReason deletionReason;
        try {
            deletionReason = DeletionReason.valueOf(reason);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid deletion reason");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        plantDeletionLogRepository.save(
                new PlantDeletionLog(plant.getName(), plant.getVariety().getName(), deletionReason, username)
        );

        removalLogRepository.findByPlant_IdIn(List.of(id)).forEach(removalLogRepository::delete);
        plantPriceRepository.deleteByPlant_Id(id);
        plantRepository.delete(plant);
    }

    @Transactional
    public PlantRelocationLogEntry startRelocation(Long plantId, StartRelocationRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found"));
        Sector toSector = sectorRepository.findById(request.sectorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sector not found"));
        Sector fromSector = relocationHistoryRepository.findFirstByPlant_IdAndEndTimeIsNull(plantId)
                .map(RelocationHistory::getSector)
                .orElse(null);
        PlantRelocationLog log = plantRelocationLogRepository.save(
                new PlantRelocationLog(plant, fromSector, toSector, username)
        );
        return toRelocationLogEntry(log);
    }

    @Transactional
    public PlantRelocationLogEntry updateRelocationState(Long logId, UpdateRelocationStateRequest request) {
        PlantRelocationLog log = plantRelocationLogRepository.findById(logId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relocation log not found"));
        if (log.getState() == RelocationState.FINISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Relocation is already finished");
        }
        RelocationState newState;
        try {
            newState = RelocationState.valueOf(request.state().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state: " + request.state());
        }
        if (newState == RelocationState.FINISHED) {
            log.setFinishedAt(java.time.Instant.now());
            Long plantId = log.getPlant().getId();
            relocationHistoryRepository.findFirstByPlant_IdAndEndTimeIsNull(plantId).ifPresent(activeRh -> {
                activeRh.setEndTime(new java.util.Date());
                Long inStock = activeRh.getInStock();
                relocationHistoryRepository.save(activeRh);
                RelocationHistory newRh = new RelocationHistory("Relocation");
                newRh.setPlant(log.getPlant());
                newRh.setSector(log.getToSector());
                newRh.setNurserySite(log.getToSector().getStorageSpace().getNurserySite());
                newRh.setInStock(inStock != null ? inStock : 0L);
                relocationHistoryRepository.save(newRh);
            });
        }
        log.setState(newState);
        plantRelocationLogRepository.save(log);
        return toRelocationLogEntry(log);
    }

    public List<PlantRelocationLogEntry> getPlantRelocationLogs(Long plantId) {
        return plantRelocationLogRepository.findByPlant_IdOrderByStartedAtDesc(plantId)
                .stream().map(this::toRelocationLogEntry).toList();
    }

    public List<PlantRelocationLogEntry> getActiveRelocations() {
        return plantRelocationLogRepository.findByStateInOrderByStartedAtDesc(
                List.of(RelocationState.WAITING, RelocationState.TRANSPORTING)
        ).stream().map(this::toRelocationLogEntry).toList();
    }

    private PlantRelocationLogEntry toRelocationLogEntry(PlantRelocationLog log) {
        Sector from = log.getFromSector();
        Sector to = log.getToSector();
        StorageSpace fromSS = from != null ? from.getStorageSpace() : null;
        StorageSpace toSS = to.getStorageSpace();
        NurserySite fromSite = fromSS != null ? fromSS.getNurserySite() : null;
        NurserySite toSite = toSS != null ? toSS.getNurserySite() : null;
        return new PlantRelocationLogEntry(
                log.getId(), log.getPlant().getId(), log.getPlant().getName(),
                from != null ? from.getName() : null,
                fromSS != null ? fromSS.getName() : null,
                fromSite != null ? fromSite.getName() : null,
                to.getName(), toSS != null ? toSS.getName() : null,
                toSite != null ? toSite.getName() : null,
                log.getState().name(),
                log.getStartedAt().toString(),
                log.getFinishedAt() != null ? log.getFinishedAt().toString() : null,
                log.getInitiatedBy()
        );
    }
}
