package com.example.iis.service;

import com.example.iis.dto.AddPlantLotRequest;
import com.example.iis.dto.LocationParcelSummaryResponse;
import com.example.iis.dto.LocationTypeResponse;
import com.example.iis.dto.NurserySiteResponse;
import com.example.iis.dto.PlantLotResponse;
import com.example.iis.dto.VarietyResponse;
import com.example.iis.model.LocationParcel;
import com.example.iis.model.LocationUnit;
import com.example.iis.model.NurserySite;
import com.example.iis.model.Plant;
import com.example.iis.model.PlantVariety;
import com.example.iis.model.RelocationHistory;
import com.example.iis.repository.LocationParcelRepository;
import com.example.iis.repository.LocationUnitRepository;
import com.example.iis.repository.NurserySiteRepository;
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
    private final LocationUnitRepository locationUnitRepository;
    private final LocationParcelRepository locationParcelRepository;
    private final NurserySiteRepository nurserySiteRepository;
    private final PlantRepository plantRepository;
    private final RelocationHistoryRepository relocationHistoryRepository;

    public WorkerPlantService(
            PlantVarietyRepository varietyRepository,
            LocationUnitRepository locationUnitRepository,
            LocationParcelRepository locationParcelRepository,
            NurserySiteRepository nurserySiteRepository,
            PlantRepository plantRepository,
            RelocationHistoryRepository relocationHistoryRepository
    ) {
        this.varietyRepository = varietyRepository;
        this.locationUnitRepository = locationUnitRepository;
        this.locationParcelRepository = locationParcelRepository;
        this.nurserySiteRepository = nurserySiteRepository;
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
                        v.getLocationType().getId(), v.getLocationType().getType()
                ))
                .toList();
    }

    public List<LocationTypeResponse> getCompatibleUnits(Long varietyId) {
        PlantVariety variety = varietyRepository.findById(varietyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variety not found"));
        String requiredType = variety.getLocationType().getType();
        return locationUnitRepository.findByType(requiredType).stream()
                .map(u -> new LocationTypeResponse(u.getId(), u.getName(), u.getType()))
                .toList();
    }

    public List<LocationParcelSummaryResponse> getLocationParcels(Long unitId) {
        return locationParcelRepository.findByUnit_Id(unitId).stream()
                .map(p -> new LocationParcelSummaryResponse(p.getId(), p.getName(), p.getCapacity(), unitId))
                .toList();
    }

    public List<NurserySiteResponse> getNurserySites(Long parcelId) {
        return nurserySiteRepository.findByParcel_Id(parcelId).stream()
                .map(s -> new NurserySiteResponse(
                        s.getId(), s.getName(),
                        s.getParcel().getUnit().getName(),
                        s.getParcel().getName()
                ))
                .toList();
    }

    @Transactional
    public PlantLotResponse addPlantLot(AddPlantLotRequest request) {
        PlantVariety variety = varietyRepository.findById(request.varietyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Variety not found"));
        LocationUnit unit = locationUnitRepository.findById(request.locationUnitId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location unit not found"));
        LocationParcel parcel = locationParcelRepository.findById(request.locationParcelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location parcel not found"));

        if (!parcel.getUnit().getId().equals(unit.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parcel does not belong to the selected unit");
        }

        NurserySite site = null;
        if (request.nurserySiteId() != null) {
            site = nurserySiteRepository.findById(request.nurserySiteId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nursery site not found"));
            if (!site.getParcel().getId().equals(parcel.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Site does not belong to the selected parcel");
            }
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

        RelocationHistory rh = new RelocationHistory("Added to nursery", plant, parcel,
                request.quantity() != null ? request.quantity() : 0L);
        if (site != null) rh.setNurserySite(site);
        relocationHistoryRepository.save(rh);

        Optional<Long> activeStock = relocationHistoryRepository
                .findActiveStockByPlantIds(List.of(plant.getId()))
                .stream().findFirst().map(v -> v.getAvailableQuantity());

        return new PlantLotResponse(
                plant.getId(), plant.getName(),
                variety.getName(),
                unit.getName(), parcel.getName(),
                activeStock.orElse(request.quantity()),
                plant.getPropagationMethod(),
                plant.getHatchingDate(), plant.getColor(), plant.getHeight(), plant.getState()
        );
    }
}
