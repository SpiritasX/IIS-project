package com.example.iis.repository;

import com.example.iis.model.RemovalLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RemovalLogRepository extends JpaRepository<RemovalLog, Long> {
    List<RemovalLog> findByPlant_IdIn(Collection<Long> plantIds);
}
