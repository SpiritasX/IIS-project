package com.example.iis.repository;

import com.example.iis.model.PlantConditionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlantConditionLogRepository extends JpaRepository<PlantConditionLog, Long> {
    List<PlantConditionLog> findByPlant_IdOrderByChangedAtDesc(Long plantId);
    List<PlantConditionLog> findByOriginalPlantIdOrderByChangedAtDesc(Long originalPlantId);

    @Query("""
            SELECT pcl FROM PlantConditionLog pcl
            WHERE pcl.plant.id IN :plantIds
            AND pcl.changedAt = (
                SELECT MAX(pcl2.changedAt) FROM PlantConditionLog pcl2
                WHERE pcl2.plant.id = pcl.plant.id
            )
            """)
    List<PlantConditionLog> findLatestPerPlants(@Param("plantIds") List<Long> plantIds);

    List<PlantConditionLog> findTop20ByPlant_IdInOrderByChangedAtDesc(List<Long> plantIds);

    @Modifying
    @Query("UPDATE PlantConditionLog pcl SET pcl.originalPlantId = :plantId WHERE pcl.plant.id = :plantId AND pcl.originalPlantId IS NULL")
    void backfillOriginalPlantId(@Param("plantId") Long plantId);

    @Modifying
    @Query("UPDATE PlantConditionLog pcl SET pcl.plant = null WHERE pcl.plant.id = :plantId")
    void nullifyPlantReference(@Param("plantId") Long plantId);
}
