package com.example.iis.repository;

import com.example.iis.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    @Query("""
            select distinct offer from Offer offer
            join offer.phases phase
            join phase.process proc
            where proc.customer.id = :customerId
            order by offer.createdAt desc
            """)
    List<Offer> findOrdersForCustomer(@Param("customerId") Long customerId);

    @Query("""
            select distinct offer from Offer offer
            join offer.phases phase
            join phase.process proc
            where offer.id = :orderId and proc.customer.id = :customerId
            """)
    Optional<Offer> findOrderForCustomer(
            @Param("orderId") Long orderId,
            @Param("customerId") Long customerId
    );
}
