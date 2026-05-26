package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;

@Entity
public class RelocationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Date startTime;

    private Date endTime;

    @Column(nullable = false)
    private String reason;

    private Long inStock;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Plant plant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private LocationParcel locationParcel;

    @ManyToOne(fetch = FetchType.LAZY)
    private Worker worker;

    public RelocationHistory() {
    }

    public RelocationHistory(String reason) {
        this.startTime = new Date(System.currentTimeMillis());
        this.endTime = null;
        this.reason = reason;
    }

    public RelocationHistory(String reason, Plant plant, LocationParcel locationParcel, Long inStock) {
        this(reason);
        this.plant = plant;
        this.locationParcel = locationParcel;
        this.inStock = inStock;
    }

    public Long getId() {
        return id;
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

    public String getReason() {
        return reason;
    }

    public Long getInStock() {
        return inStock;
    }

    public void setInStock(Long inStock) {
        this.inStock = inStock;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public LocationParcel getLocationParcel() {
        return locationParcel;
    }

    public void setLocationParcel(LocationParcel locationParcel) {
        this.locationParcel = locationParcel;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
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
        RelocationHistory that = (RelocationHistory) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
