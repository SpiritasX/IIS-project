package com.example.iis.repository;

import com.example.iis.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
    List<StockReservation> findByOrderItem_Offer_Id(Long offerId);

    void deleteByOrderItem_Offer_Id(Long offerId);
}
