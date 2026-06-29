package com.example.iis.repository;

import com.example.iis.model.HealthLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface HealthLogRepository extends JpaRepository<HealthLog, Long> {
    List<HealthLog> findTop20ByPlant_IdInOrderByTimestampDesc(Collection<Long> plantIds);

    List<HealthLog> findTop20ByOrderByTimestampDesc();
}
