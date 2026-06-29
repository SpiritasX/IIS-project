package com.example.iis.service;

import com.example.iis.dto.CreateNurserySiteRequest;
import com.example.iis.dto.CreateSectorRequest;
import com.example.iis.dto.CreateStorageSpaceRequest;
import com.example.iis.dto.SectorDetailResponse;
import com.example.iis.dto.UpdateSectorRequest;
import com.example.iis.dto.StorageSpaceDetailResponse;
import com.example.iis.dto.StorageSpaceTypeResponse;
import com.example.iis.dto.UpdateStorageSpaceRequest;
import com.example.iis.model.NurserySite;
import com.example.iis.model.Sector;
import com.example.iis.model.StorageSpace;
import com.example.iis.model.StorageSpaceType;
import com.example.iis.repository.NurserySiteRepository;
import com.example.iis.repository.PlantVarietyRepository;
import com.example.iis.repository.RelocationHistoryRepository;
import com.example.iis.repository.SectorRepository;
import com.example.iis.repository.StorageSpaceRepository;
import com.example.iis.repository.StorageSpaceTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class LocationManagementService {

    private final StorageSpaceRepository storageSpaceRepository;
    private final StorageSpaceTypeRepository storageSpaceTypeRepository;
    private final SectorRepository sectorRepository;
    private final NurserySiteRepository nurserySiteRepository;
    private final RelocationHistoryRepository relocationHistoryRepository;
    private final PlantVarietyRepository plantVarietyRepository;

    public LocationManagementService(
            StorageSpaceRepository storageSpaceRepository,
            StorageSpaceTypeRepository storageSpaceTypeRepository,
            SectorRepository sectorRepository,
            NurserySiteRepository nurserySiteRepository,
            RelocationHistoryRepository relocationHistoryRepository,
            PlantVarietyRepository plantVarietyRepository
    ) {
        this.storageSpaceRepository = storageSpaceRepository;
        this.storageSpaceTypeRepository = storageSpaceTypeRepository;
        this.sectorRepository = sectorRepository;
        this.nurserySiteRepository = nurserySiteRepository;
        this.relocationHistoryRepository = relocationHistoryRepository;
        this.plantVarietyRepository = plantVarietyRepository;
    }

    @Transactional(readOnly = true)
    public List<StorageSpaceTypeResponse> getSpaceTypes() {
        return storageSpaceTypeRepository.findAll().stream()
                .map(t -> new StorageSpaceTypeResponse(t.getId(), t.getName()))
                .toList();
    }

    public StorageSpaceTypeResponse createSpaceType(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type name is required");
        }
        if (storageSpaceTypeRepository.existsByName(name.trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This storage space type already exists");
        }
        StorageSpaceType saved = storageSpaceTypeRepository.save(new StorageSpaceType(name.trim()));
        return new StorageSpaceTypeResponse(saved.getId(), saved.getName());
    }

    @Transactional(readOnly = true)
    public List<StorageSpaceDetailResponse> getAll(Long siteId) {
        List<StorageSpace> spaces = siteId != null
                ? storageSpaceRepository.findByNurserySite_Id(siteId)
                : storageSpaceRepository.findAll();
        return spaces.stream().map(this::toDetail).toList();
    }

    @Transactional(readOnly = true)
    public StorageSpaceDetailResponse getById(Long id) {
        return toDetail(findOrThrow(id));
    }

    public StorageSpaceDetailResponse createNurserySite(CreateNurserySiteRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nursery site name is required");
        }
        if (request.address() == null || request.address().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address is required");
        }
        if (request.spaceName() == null || request.spaceName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Storage space name is required");
        }
        if (request.spaceType() == null || request.spaceType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Storage space type is required");
        }
        if (nurserySiteRepository.existsByName(request.name().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A nursery site with this name already exists");
        }
        guardSpaceType(request.spaceType());
        NurserySite site = new NurserySite(request.name().trim(), 0.0, 0.0);
        site.setAddress(request.address().trim());
        site = nurserySiteRepository.save(site);

        StorageSpace space = new StorageSpace(request.spaceName().trim(), request.spaceType().trim());
        space.setNurserySite(site);

        if (request.sectorName() != null && !request.sectorName().isBlank()) {
            space.addSector(new Sector(request.sectorName().trim(), request.sectorCapacity()));
        }

        return toDetail(storageSpaceRepository.save(space));
    }

    public void deleteNurserySite(Long id) {
        nurserySiteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nursery site not found"));
        if (storageSpaceRepository.existsByNurserySite_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete: this nursery site has associated storage spaces");
        }
        nurserySiteRepository.deleteById(id);
    }

    public StorageSpaceDetailResponse create(CreateStorageSpaceRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        if (request.type() == null || request.type().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type is required");
        }
        if (request.nurserySiteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nursery site is required");
        }
        guardSpaceType(request.type());
        NurserySite site = nurserySiteRepository.findById(request.nurserySiteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nursery site not found"));
        StorageSpace storageSpace = new StorageSpace(request.name().trim(), request.type().trim());
        storageSpace.setNurserySite(site);
        return toDetail(storageSpaceRepository.save(storageSpace));
    }

    public StorageSpaceDetailResponse update(Long id, UpdateStorageSpaceRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        if (request.type() == null || request.type().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type is required");
        }
        guardSpaceType(request.type());
        StorageSpace space = findOrThrow(id);
        space.setName(request.name().trim());
        space.setType(request.type().trim());
        return toDetail(storageSpaceRepository.save(space));
    }

    public void delete(Long id) {
        StorageSpace space = findOrThrow(id);
        if (plantVarietyRepository.existsByStorageSpaceType_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete: this storage space is used as a type by one or more plant varieties");
        }
        for (Sector sector : space.getSectors()) {
            guardSectorDeletion(sector.getId());
        }
        storageSpaceRepository.deleteById(id);
    }

    public StorageSpaceDetailResponse addSector(Long storageSpaceId, CreateSectorRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sector name is required");
        }
        StorageSpace space = findOrThrow(storageSpaceId);
        Sector sector = new Sector(request.name().trim(), request.capacity());
        space.addSector(sector);
        return toDetail(storageSpaceRepository.save(space));
    }

    public StorageSpaceDetailResponse updateSector(Long sectorId, UpdateSectorRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sector name is required");
        }
        Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sector not found"));
        sector.setName(request.name().trim());
        sector.setCapacity(request.capacity());
        sectorRepository.save(sector);
        return toDetail(sector.getStorageSpace());
    }

    public void deleteSector(Long sectorId) {
        sectorRepository.findById(sectorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sector not found"));
        guardSectorDeletion(sectorId);
        sectorRepository.deleteById(sectorId);
    }

    private void guardSpaceType(String type) {
        if (type == null || type.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Storage space type is required");
        }
        if (!storageSpaceTypeRepository.existsByName(type.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown storage space type: " + type.trim());
        }
    }

    private void guardSectorDeletion(Long sectorId) {
        if (relocationHistoryRepository.existsBySector_Id(sectorId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete: this sector has associated relocation history");
        }
    }

    private StorageSpace findOrThrow(Long id) {
        return storageSpaceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Storage space not found"));
    }

    private StorageSpaceDetailResponse toDetail(StorageSpace ss) {
        List<SectorDetailResponse> sectors = ss.getSectors().stream()
                .map(s -> new SectorDetailResponse(s.getId(), s.getName(), s.getCapacity()))
                .toList();
        return new StorageSpaceDetailResponse(ss.getId(), ss.getName(), ss.getType(), sectors);
    }
}
