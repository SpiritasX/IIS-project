package com.example.iis.repository;

import com.example.iis.model.OrderHistoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
