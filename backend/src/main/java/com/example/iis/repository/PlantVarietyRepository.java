package com.example.iis.repository;

import com.example.iis.model.PlantVariety;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlantVarietyRepository extends JpaRepository<PlantVariety, Long> {
    Optional<PlantVariety> findByNameIgnoreCase(String name);
}
