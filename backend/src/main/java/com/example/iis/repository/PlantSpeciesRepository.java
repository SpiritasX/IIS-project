package com.example.iis.repository;

import com.example.iis.model.PlantSpecies;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlantSpeciesRepository extends JpaRepository<PlantSpecies, Long> {
    Optional<PlantSpecies> findByNameIgnoreCase(String name);
}
