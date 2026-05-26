package com.example.iis.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Entity
public class PlantPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Date startTime;

    private Date endTime;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Plant plant;

    public PlantPrice() {
    }

    public PlantPrice(BigDecimal price, Plant plant) {
        this.price = price;
        this.startTime = new Date(System.currentTimeMillis());
        this.plant = plant;
    }

    public PlantPrice(Double price, Plant plant) {
        this(BigDecimal.valueOf(price), plant);
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    @PrePersist
    void prePersist() {
        if (startTime == null) {
            startTime = new Date(System.currentTimeMillis());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlantPrice that = (PlantPrice) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
