package com.example.iis.repository;

import com.example.iis.model.PlantDeletionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlantDeletionLogRepository extends JpaRepository<PlantDeletionLog, Long> {
    @Query("SELECT l.reason, COUNT(l) FROM PlantDeletionLog l GROUP BY l.reason")
    List<Object[]> countGroupedByReason();

    List<PlantDeletionLog> findAllByOrderByDeletedAtDesc();
}
