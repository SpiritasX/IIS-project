package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;

@Entity
public class PlantPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Double price;
    @Column(nullable = false)
    private Date startTime;
    private Date endTime;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Plant plant;

    public PlantPrice() {
    }

    public PlantPrice(Double price, Plant plant) {
        this.price = price;
        this.startTime = new Date(System.currentTimeMillis());
        this.plant = plant;
    }

    public Long getId() {
        return id;
    }

    public Double getPrice() {
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlantPrice that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
