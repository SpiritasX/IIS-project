package com.example.iis.service;

import com.example.iis.dto.AddVarietyRequest;
import com.example.iis.dto.CategoryResponse;
import com.example.iis.dto.UpdateVarietyRequest;
import com.example.iis.dto.CategoryTreeItem;
import com.example.iis.dto.SpeciesResponse;
import com.example.iis.dto.SpeciesTreeItem;
import com.example.iis.dto.StorageSpaceTypeResponse;
import com.example.iis.dto.TypeResponse;
import com.example.iis.dto.TypeTreeItem;
import com.example.iis.dto.VarietyResponse;
import com.example.iis.model.PlantCategory;
import com.example.iis.model.PlantSpecies;
import com.example.iis.model.PlantType;
import com.example.iis.model.PlantVariety;
import com.example.iis.model.StorageSpaceType;
import com.example.iis.repository.PlantCategoryRepository;
import com.example.iis.repository.PlantSpeciesRepository;
import com.example.iis.repository.PlantTypeRepository;
import com.example.iis.repository.PlantVarietyRepository;
import com.example.iis.repository.StorageSpaceTypeRepository;
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
    private final StorageSpaceTypeRepository storageSpaceTypeRepository;

    public PlantVarietyService(
            PlantCategoryRepository categoryRepository,
            PlantTypeRepository typeRepository,
            PlantSpeciesRepository speciesRepository,
            PlantVarietyRepository varietyRepository,
            StorageSpaceTypeRepository storageSpaceTypeRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.typeRepository = typeRepository;
        this.speciesRepository = speciesRepository;
        this.varietyRepository = varietyRepository;
        this.storageSpaceTypeRepository = storageSpaceTypeRepository;
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

    public List<StorageSpaceTypeResponse> getStorageSpaces() {
        return storageSpaceTypeRepository.findAll().stream()
                .map(t -> new StorageSpaceTypeResponse(t.getId(), t.getName()))
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

        StorageSpaceType storageSpaceType = storageSpaceTypeRepository.findById(request.storageSpaceTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Storage space type not found"));

        PlantVariety variety = new PlantVariety(
                request.name(),
                request.humidity(),
                request.soil(),
                request.instructions(),
                species,
                storageSpaceType
        );
        variety.setLatinName(request.latinName());

        return toResponse(varietyRepository.save(variety));
    }

    @Transactional
    public VarietyResponse updateVariety(Long id, UpdateVarietyRequest request) {
        PlantVariety variety = varietyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variety not found"));
        if (request.name() != null && !request.name().isBlank()) {
            variety.setName(request.name().trim());
        }
        variety.setLatinName(request.latinName());
        variety.setHumidity(request.humidity());
        variety.setSoil(request.soil());
        variety.setInstructions(request.instructions());
        if (request.storageSpaceTypeId() != null) {
            StorageSpaceType storageSpaceType = storageSpaceTypeRepository.findById(request.storageSpaceTypeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Storage space type not found"));
            variety.setStorageSpaceType(storageSpaceType);
        }
        return toResponse(varietyRepository.save(variety));
    }

    private VarietyResponse toResponse(PlantVariety v) {
        PlantSpecies species = v.getSpecies();
        StorageSpaceType storageSpaceType = v.getStorageSpaceType();
        return new VarietyResponse(
                v.getId(),
                v.getName(),
                v.getLatinName(),
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
                storageSpaceType.getName()
        );
    }

    public List<CategoryTreeItem> getCategoryTree() {
        return categoryRepository.findAll().stream()
                .map(c -> {
                    List<TypeTreeItem> types = typeRepository.findByCategory_Id(c.getId()).stream()
                            .map(t -> {
                                List<SpeciesTreeItem> species = speciesRepository.findByType_Id(t.getId()).stream()
                                        .map(s -> new SpeciesTreeItem(s.getId(), s.getName(), s.getVarieties().size()))
                                        .toList();
                                return new TypeTreeItem(t.getId(), t.getName(), species);
                            })
                            .toList();
                    return new CategoryTreeItem(c.getId(), c.getName(), types);
                })
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(String name) {
        if (name == null || name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        if (categoryRepository.existsByName(name.trim())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
        PlantCategory saved = categoryRepository.save(new PlantCategory(name.trim()));
        return new CategoryResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public CategoryResponse renameCategory(Long id, String name) {
        if (name == null || name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        PlantCategory cat = categoryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        if (!cat.getName().equals(name.trim()) && categoryRepository.existsByName(name.trim())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already taken");
        cat.setName(name.trim());
        PlantCategory saved = categoryRepository.save(cat);
        return new CategoryResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public void deleteCategory(Long id) {
        PlantCategory cat = categoryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        if (varietyRepository.existsBySpecies_Type_Category_Id(id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete: varieties exist in this category");
        List<PlantType> types = typeRepository.findByCategory_Id(id);
        List<Long> typeIds = types.stream().map(PlantType::getId).toList();
        if (!typeIds.isEmpty()) {
            List<PlantSpecies> species = speciesRepository.findByType_IdIn(typeIds);
            speciesRepository.deleteAll(species);
            typeRepository.deleteAll(types);
        }
        categoryRepository.delete(cat);
    }

    @Transactional
    public TypeResponse createType(Long categoryId, String name) {
        if (name == null || name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        PlantCategory cat = categoryRepository.findById(categoryId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        if (typeRepository.existsByName(name.trim())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Subcategory name already exists");
        PlantType saved = typeRepository.save(new PlantType(name.trim(), cat));
        return new TypeResponse(saved.getId(), saved.getName(), saved.getCategory().getId());
    }

    @Transactional
    public TypeResponse renameType(Long id, String name) {
        if (name == null || name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        PlantType type = typeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Type not found"));
        if (!type.getName().equals(name.trim()) && typeRepository.existsByName(name.trim())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Subcategory name already taken");
        type.setName(name.trim());
        PlantType saved = typeRepository.save(type);
        return new TypeResponse(saved.getId(), saved.getName(), saved.getCategory().getId());
    }

    @Transactional
    public void deleteType(Long id) {
        PlantType type = typeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Type not found"));
        if (varietyRepository.existsBySpecies_Type_Id(id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete: varieties exist in this subcategory");
        List<PlantSpecies> species = speciesRepository.findByType_Id(id);
        speciesRepository.deleteAll(species);
        typeRepository.delete(type);
    }

    @Transactional
    public SpeciesResponse createSpecies(Long typeId, String name) {
        if (name == null || name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        PlantType type = typeRepository.findById(typeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Type not found"));
        if (speciesRepository.existsByName(name.trim())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Species name already exists");
        PlantSpecies saved = speciesRepository.save(new PlantSpecies(name.trim(), type));
        return new SpeciesResponse(saved.getId(), saved.getName(), saved.getType().getId());
    }

    @Transactional
    public SpeciesResponse renameSpecies(Long id, String name) {
        if (name == null || name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        PlantSpecies species = speciesRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Species not found"));
        if (!species.getName().equals(name.trim()) && speciesRepository.existsByName(name.trim())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Species name already taken");
        species.setName(name.trim());
        PlantSpecies saved = speciesRepository.save(species);
        return new SpeciesResponse(saved.getId(), saved.getName(), saved.getType().getId());
    }

    @Transactional
    public void deleteSpecies(Long id) {
        PlantSpecies species = speciesRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Species not found"));
        if (varietyRepository.existsBySpecies_Id(id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete: varieties exist for this species");
        speciesRepository.delete(species);
    }
}
