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

    @Column(nullable = false)
    private String type;

    private Long capacity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private StorageSpace storageSpace;

    @OneToMany(mappedBy = "sector", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<NurserySite> sites = new ArrayList<>();

    @OneToMany(mappedBy = "sector")
    private List<RelocationHistory> relocationHistory = new ArrayList<>();

    public Sector() {
    }

    public Sector(String name, String type, Long capacity) {
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

    public StorageSpace getStorageSpace() {
        return storageSpace;
    }

    public void setStorageSpace(StorageSpace storageSpace) {
        this.storageSpace = storageSpace;
    }

    public List<NurserySite> getSites() {
        return sites;
    }

    public void addSite(NurserySite site) {
        sites.add(site);
        site.setSector(this);
    }

    public void removeSite(NurserySite site) {
        sites.remove(site);
        site.setSector(null);
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
