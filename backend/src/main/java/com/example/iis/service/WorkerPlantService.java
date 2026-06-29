package com.example.iis.service;

import com.example.iis.dto.AddPlantLotRequest;
import com.example.iis.dto.SectorSummaryResponse;
import com.example.iis.dto.StorageSpaceResponse;
import com.example.iis.dto.PlantLotResponse;
import com.example.iis.dto.VarietyResponse;
import com.example.iis.model.NurserySite;
import com.example.iis.model.Sector;
import com.example.iis.model.StorageSpace;
import com.example.iis.model.Plant;
import com.example.iis.model.PlantVariety;
import com.example.iis.model.RelocationHistory;
import com.example.iis.repository.SectorRepository;
import com.example.iis.repository.StorageSpaceRepository;
import com.example.iis.repository.PlantRepository;
import com.example.iis.repository.PlantVarietyRepository;
import com.example.iis.repository.RelocationHistoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class WorkerPlantService {

    private final PlantVarietyRepository varietyRepository;
    private final StorageSpaceRepository storageSpaceRepository;
    private final SectorRepository sectorRepository;
    private final PlantRepository plantRepository;
    private final RelocationHistoryRepository relocationHistoryRepository;

    public WorkerPlantService(
            PlantVarietyRepository varietyRepository,
            StorageSpaceRepository storageSpaceRepository,
            SectorRepository sectorRepository,
            PlantRepository plantRepository,
            RelocationHistoryRepository relocationHistoryRepository
    ) {
        this.varietyRepository = varietyRepository;
        this.storageSpaceRepository = storageSpaceRepository;
        this.sectorRepository = sectorRepository;
        this.plantRepository = plantRepository;
        this.relocationHistoryRepository = relocationHistoryRepository;
    }

    public List<VarietyResponse> getVarieties() {
        return varietyRepository.findAll().stream()
                .map(v -> new VarietyResponse(
                        v.getId(), v.getName(), v.getHumidity(), v.getSoil(), v.getInstructions(),
                        v.getSpecies().getId(), v.getSpecies().getName(),
                        v.getSpecies().getType().getId(), v.getSpecies().getType().getName(),
                        v.getSpecies().getType().getCategory().getId(), v.getSpecies().getType().getCategory().getName(),
                        v.getStorageSpaceType().getId(), v.getStorageSpaceType().getType()
                ))
                .toList();
    }

    public List<StorageSpaceResponse> getCompatibleStorageSpaces(Long varietyId) {
        PlantVariety variety = varietyRepository.findById(varietyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variety not found"));
        String requiredType = variety.getStorageSpaceType().getType();
        return storageSpaceRepository.findByType(requiredType).stream()
                .map(u -> new StorageSpaceResponse(u.getId(), u.getName(), u.getType()))
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

        String plantName = (request.name() != null && !request.name().isBlank())
                ? request.name()
                : variety.getName() + " lot";

        Plant plant = new Plant(plantName, null,
                request.propagationMethod() != null ? request.propagationMethod() : null,
                "Available", variety);
        plant.setHatchingDate(request.hatchingDate());
        plant.setColor(request.color());
        plant.setHeight(request.height());
        plant.setState(request.state());
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
                plant.getHatchingDate(), plant.getColor(), plant.getHeight(), plant.getState()
        );
    }
}
