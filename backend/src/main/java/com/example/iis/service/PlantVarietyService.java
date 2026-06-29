package com.example.iis.service;

import com.example.iis.dto.AddVarietyRequest;
import com.example.iis.dto.CategoryResponse;
import com.example.iis.dto.StorageSpaceResponse;
import com.example.iis.dto.SpeciesResponse;
import com.example.iis.dto.TypeResponse;
import com.example.iis.dto.VarietyResponse;
import com.example.iis.model.StorageSpace;
import com.example.iis.model.PlantSpecies;
import com.example.iis.model.PlantVariety;
import com.example.iis.repository.StorageSpaceRepository;
import com.example.iis.repository.PlantCategoryRepository;
import com.example.iis.repository.PlantSpeciesRepository;
import com.example.iis.repository.PlantTypeRepository;
import com.example.iis.repository.PlantVarietyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlantVarietyService {

    private final PlantCategoryRepository categoryRepository;
    private final PlantTypeRepository typeRepository;
    private final PlantSpeciesRepository speciesRepository;
    private final PlantVarietyRepository varietyRepository;
    private final StorageSpaceRepository storageSpaceRepository;

    public PlantVarietyService(
            PlantCategoryRepository categoryRepository,
            PlantTypeRepository typeRepository,
            PlantSpeciesRepository speciesRepository,
            PlantVarietyRepository varietyRepository,
            StorageSpaceRepository storageSpaceRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.typeRepository = typeRepository;
        this.speciesRepository = speciesRepository;
        this.varietyRepository = varietyRepository;
        this.storageSpaceRepository = storageSpaceRepository;
    }

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    public List<TypeResponse> getTypesByCategory(Long categoryId) {
        return typeRepository.findByCategory_Id(categoryId).stream()
                .map(t -> new TypeResponse(t.getId(), t.getName(), t.getCategory().getId()))
                .toList();
    }

    public List<SpeciesResponse> getSpeciesByType(Long typeId) {
        return speciesRepository.findByType_Id(typeId).stream()
                .map(s -> new SpeciesResponse(s.getId(), s.getName(), s.getType().getId()))
                .toList();
    }

    public List<StorageSpaceResponse> getStorageSpaces() {
        return storageSpaceRepository.findAll().stream()
                .map(u -> new StorageSpaceResponse(u.getId(), u.getName(), u.getType()))
                .toList();
    }

    public List<VarietyResponse> getAllVarieties() {
        return varietyRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VarietyResponse addVariety(AddVarietyRequest request) {
        PlantSpecies species = speciesRepository.findById(request.speciesId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Species not found"));

        StorageSpace storageSpaceType = storageSpaceRepository.findById(request.storageSpaceTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Storage space type not found"));

        PlantVariety variety = new PlantVariety(
                request.name(),
                request.humidity(),
                request.soil(),
                request.instructions(),
                species,
                storageSpaceType
        );

        return toResponse(varietyRepository.save(variety));
    }

    private VarietyResponse toResponse(PlantVariety v) {
        PlantSpecies species = v.getSpecies();
        StorageSpace storageSpaceType = v.getStorageSpaceType();
        return new VarietyResponse(
                v.getId(),
                v.getName(),
                v.getHumidity(),
                v.getSoil(),
                v.getInstructions(),
                species.getId(),
                species.getName(),
                species.getType().getId(),
                species.getType().getName(),
                species.getType().getCategory().getId(),
                species.getType().getCategory().getName(),
                storageSpaceType.getId(),
                storageSpaceType.getType()
        );
    }
}
