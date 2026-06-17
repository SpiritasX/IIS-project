package com.example.iis.repository;

import com.example.iis.model.RelocationHistory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<RelocationHistory> findByPlant_IdAndEndTimeIsNullOrderByStartTimeAsc(Long plantId);
}
