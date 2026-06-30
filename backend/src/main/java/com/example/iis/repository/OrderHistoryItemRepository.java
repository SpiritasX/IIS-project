package com.example.iis.repository;

import com.example.iis.model.OrderHistoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderHistoryItemRepository extends JpaRepository<OrderHistoryItem, Long> {
    List<OrderHistoryItem> findByPlantPrice_Plant_IdAndOrderHistory_Offer_Status_Name(Long plantId, String statusName);
}
