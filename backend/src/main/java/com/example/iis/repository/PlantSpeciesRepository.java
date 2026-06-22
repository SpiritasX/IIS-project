package com.example.iis.repository;

import com.example.iis.model.PlantSpecies;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantSpeciesRepository extends JpaRepository<PlantSpecies, Long> {
    List<PlantSpecies> findByType_Id(Long typeId);
}
