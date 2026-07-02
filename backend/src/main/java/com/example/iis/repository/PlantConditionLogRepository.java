package com.example.iis.repository;

import com.example.iis.model.PlantConditionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantConditionLogRepository extends JpaRepository<PlantConditionLog, Long> {
    List<PlantConditionLog> findByPlant_IdOrderByChangedAtDesc(Long plantId);
}
