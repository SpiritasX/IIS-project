package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class StockReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RelocationHistory relocationHistory;

    @Column(nullable = false)
    private Integer quantity;

    public StockReservation() {
    }

    public StockReservation(OrderItem orderItem, RelocationHistory relocationHistory, Integer quantity) {
        this.orderItem = orderItem;
        this.relocationHistory = relocationHistory;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public RelocationHistory getRelocationHistory() {
        return relocationHistory;
    }

    public void setRelocationHistory(RelocationHistory relocationHistory) {
        this.relocationHistory = relocationHistory;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockReservation that = (StockReservation) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
