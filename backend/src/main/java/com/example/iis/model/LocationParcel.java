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
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private final List<LocationUnit> units = new ArrayList<>();

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
    }

    public void removeUnit(LocationUnit unit) {
        units.remove(unit);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LocationParcel that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
