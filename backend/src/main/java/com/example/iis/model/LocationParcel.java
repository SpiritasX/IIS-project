package com.example.iis.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class LocationParcel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    private Long capacity;

    @OneToMany(mappedBy = "parcel", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<LocationUnit> units = new ArrayList<>();

    @OneToMany(mappedBy = "locationParcel")
    private List<RelocationHistory> relocationHistory = new ArrayList<>();

    public LocationParcel() {
    }

    public LocationParcel(String name, String type, Long capacity) {
        this.name = name;
        this.type = type;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Long getCapacity() {
        return capacity;
    }

    public void setCapacity(Long capacity) {
        this.capacity = capacity;
    }

    public List<LocationUnit> getUnits() {
        return units;
    }

    public void addUnit(LocationUnit unit) {
        units.add(unit);
        unit.setParcel(this);
    }

    public void removeUnit(LocationUnit unit) {
        units.remove(unit);
        unit.setParcel(null);
    }

    public List<RelocationHistory> getRelocationHistory() {
        return relocationHistory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationParcel that = (LocationParcel) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
