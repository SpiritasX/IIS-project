package com.example.iis.repository;

import com.example.iis.model.RelocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface RelocationHistoryRepository extends JpaRepository<RelocationHistory, Long> {
    interface PlantStockView {
        Long getPlantId();

        Long getAvailableQuantity();
    }

    long countByPlant_IdAndEndTimeIsNull(Long plantId);

    @Query("""
            select history.plant.id as plantId, coalesce(sum(history.inStock), 0) as availableQuantity
            from RelocationHistory history
            where history.endTime is null and history.plant.id in :plantIds
            group by history.plant.id
            """)
    List<PlantStockView> findActiveStockByPlantIds(@Param("plantIds") Collection<Long> plantIds);

    List<RelocationHistory> findByNurserySiteIsNull();

    List<RelocationHistory> findByNurserySite_IdAndEndTimeIsNull(Long siteId);

    List<RelocationHistory> findByEndTimeIsNull();

    List<RelocationHistory> findByNurserySite_Id(Long siteId);

    @Query("SELECT rh.plant.id FROM RelocationHistory rh WHERE rh.nurserySite.id = :siteId AND rh.endTime IS NULL")
    List<Long> findActivePlantIdsBySite(@Param("siteId") Long siteId);

    @Query("SELECT rh.plant.id FROM RelocationHistory rh WHERE rh.endTime IS NULL")
    List<Long> findAllActivePlantIds();

    long countByEndTimeIsNull();

    List<RelocationHistory> findByNurserySite_Sector_IdAndEndTimeIsNull(Long sectorId);

    List<RelocationHistory> findTop20ByNurserySite_IdOrderByStartTimeDesc(Long siteId);

    List<RelocationHistory> findTop20ByOrderByStartTimeDesc();
}
