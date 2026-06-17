package com.example.iis.repository;

import com.example.iis.model.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProcessRepository extends JpaRepository<Process, Long> {
    List<Process> findAllByOrderByStartTimeDesc();

    @Query("""
            select distinct proc from Process proc
            join proc.phases phase
            where phase.offer.id = :offerId and proc.customer.id = :customerId
            """)
    Optional<Process> findByOfferIdAndCustomerId(
            @Param("offerId") Long offerId,
            @Param("customerId") Long customerId
    );
}
