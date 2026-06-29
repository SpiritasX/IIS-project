package com.example.iis.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class StorageSpace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nursery_site_id")
    private NurserySite nurserySite;

    @OneToMany(mappedBy = "storageSpace", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Sector> sectors = new ArrayList<>();

    public StorageSpace() {
    }

    public StorageSpace(String name, String type) {
        this.name = name;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public NurserySite getNurserySite() {
        return nurserySite;
    }

    public void setNurserySite(NurserySite nurserySite) {
        this.nurserySite = nurserySite;
    }

    public List<Sector> getSectors() {
        return sectors;
    }

    public void addSector(Sector sector) {
        sectors.add(sector);
        sector.setStorageSpace(this);
    }

    public void removeSector(Sector sector) {
        sectors.remove(sector);
        sector.setStorageSpace(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageSpace that = (StorageSpace) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
