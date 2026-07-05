package com.example.iis.repository;

import com.example.iis.model.PlantRelocationLog;
import com.example.iis.model.RelocationState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantRelocationLogRepository extends JpaRepository<PlantRelocationLog, Long> {
    List<PlantRelocationLog> findByPlant_IdOrderByStartedAtDesc(Long plantId);
    List<PlantRelocationLog> findByStateInOrderByStartedAtDesc(List<RelocationState> states);
    void deleteByPlant_Id(Long plantId);
}
