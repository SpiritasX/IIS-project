package com.example.iis.repository;

import com.example.iis.model.PlantType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlantTypeRepository extends JpaRepository<PlantType, Long> {
    Optional<PlantType> findByNameIgnoreCase(String name);
}
