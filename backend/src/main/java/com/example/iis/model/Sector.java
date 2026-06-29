package com.example.iis.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Sector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Long capacity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private StorageSpace storageSpace;

    @OneToMany(mappedBy = "sector")
    private List<RelocationHistory> relocationHistory = new ArrayList<>();

    public Sector() {
    }

    public Sector(String name, Long capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCapacity() {
        return capacity;
    }

    public void setCapacity(Long capacity) {
        this.capacity = capacity;
    }

    public StorageSpace getStorageSpace() {
        return storageSpace;
    }

    public void setStorageSpace(StorageSpace storageSpace) {
        this.storageSpace = storageSpace;
    }

    public List<RelocationHistory> getRelocationHistory() {
        return relocationHistory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sector that = (Sector) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
