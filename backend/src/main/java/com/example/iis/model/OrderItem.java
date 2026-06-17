package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "plant_price_id", nullable = false)
    private PlantPrice plantPrice;

    @Column(nullable = false)
    private Integer quantity;

    private Integer requestedQuantity;

    private Integer offeredQuantity;

    public OrderItem() {
    }

    public OrderItem(PlantPrice plantPrice, Integer quantity) {
        this.plantPrice = plantPrice;
        this.quantity = quantity;
        this.requestedQuantity = quantity;
        this.offeredQuantity = quantity;
    }

    public OrderItem(PlantPrice plantPrice, Integer requestedQuantity, Integer offeredQuantity, Integer reservedQuantity) {
        this.plantPrice = plantPrice;
        this.requestedQuantity = requestedQuantity;
        this.offeredQuantity = offeredQuantity;
        this.quantity = reservedQuantity;
    }

    public Long getId() {
        return id;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public PlantPrice getPlantPrice() {
        return plantPrice;
    }

    public void setPlantPrice(PlantPrice plantPrice) {
        this.plantPrice = plantPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(Integer requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public Integer getOfferedQuantity() {
        return offeredQuantity;
    }

    public void setOfferedQuantity(Integer offeredQuantity) {
        this.offeredQuantity = offeredQuantity;
    }

    @PrePersist
    @PreUpdate
    void normalizeQuantities() {
        if (requestedQuantity == null) {
            requestedQuantity = quantity;
        }

        if (offeredQuantity == null) {
            offeredQuantity = quantity;
        }

        if (quantity == null) {
            quantity = 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        if (id == null || orderItem.id == null) return false;
        return Objects.equals(id, orderItem.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
