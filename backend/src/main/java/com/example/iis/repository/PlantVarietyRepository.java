package com.example.iis.repository;

import com.example.iis.model.PlantVariety;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantVarietyRepository extends JpaRepository<PlantVariety, Long> {
    boolean existsByStorageSpaceType_Id(Long storageSpaceId);
    boolean existsBySpecies_Id(Long speciesId);
    boolean existsBySpecies_Type_Id(Long typeId);
    boolean existsBySpecies_Type_Category_Id(Long categoryId);
}
