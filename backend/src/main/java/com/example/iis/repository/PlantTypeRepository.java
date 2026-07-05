package com.example.iis.repository;

import com.example.iis.model.PlantType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantTypeRepository extends JpaRepository<PlantType, Long> {
    List<PlantType> findByCategory_Id(Long categoryId);
    boolean existsByName(String name);
    boolean existsByCategory_Id(Long categoryId);
}
