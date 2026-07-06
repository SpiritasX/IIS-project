package com.example.iis.repository;

import com.example.iis.model.OrderItem;
import com.example.iis.model.PlantPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("""
            select item.plantPrice
            from OrderItem item
            join item.offer offer
            join offer.phases phase
            join phase.process proc
            where proc.customer.id = :customerId
              and item.plantPrice.plant.id = :plantId
              and offer.status.name in :activeStatuses
              and phase.endTime is null
            order by phase.startTime asc, item.id asc
            """)
    List<PlantPrice> findLockedPricesForCustomerAndPlant(
            @Param("customerId") Long customerId,
            @Param("plantId") Long plantId,
            @Param("activeStatuses") Collection<String> activeStatuses
    );
}
