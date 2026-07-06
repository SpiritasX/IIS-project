package com.example.iis.repository;

import com.example.iis.model.OrderHistoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface OrderHistoryItemRepository extends JpaRepository<OrderHistoryItem, Long> {
    List<OrderHistoryItem> findByPlantPrice_Plant_IdAndOrderHistory_Offer_Status_Name(Long plantId, String statusName);

    List<OrderHistoryItem> findByPlantPrice_Plant_IdAndOrderHistory_Offer_Status_NameIn(
            Long plantId,
            Collection<String> statusNames
    );

    boolean existsByPlantPrice_Plant_IdAndOrderHistory_ChangedAtAndOrderHistory_Offer_Status_Name(
            Long plantId,
            Date changedAt,
            String statusName
    );

    @Query("""
            select coalesce(sum(item.quantity), 0)
            from OrderHistoryItem item
            where item.plantPrice.plant.id = :plantId
              and item.orderHistory.changedAt >= :fromDate
              and item.orderHistory.offer.status.name in :statusNames
            """)
    Long sumDemandForPlantSince(
            @Param("plantId") Long plantId,
            @Param("fromDate") Date fromDate,
            @Param("statusNames") Collection<String> statusNames
    );
}
