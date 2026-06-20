package com.example.iis.repository;

import com.example.iis.model.PlantCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlantCategoryRepository extends JpaRepository<PlantCategory, Long> {
    Optional<PlantCategory> findByNameIgnoreCase(String name);
}
