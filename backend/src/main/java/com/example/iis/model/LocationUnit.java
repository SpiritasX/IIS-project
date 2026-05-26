package com.example.iis.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class LocationUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private LocationParcel parcel;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<NurserySite> sites = new ArrayList<>();

    public LocationUnit() {
    }

    public LocationUnit(String name, String type) {
        this.name = name;
        this.type = type;
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

    public LocationParcel getParcel() {
        return parcel;
    }

    public void setParcel(LocationParcel parcel) {
        this.parcel = parcel;
    }

    public List<NurserySite> getSites() {
        return sites;
    }

    public List<NurserySite> getSite() {
        return sites;
    }

    public void addSite(NurserySite site) {
        this.sites.add(site);
        site.setUnit(this);
    }

    public void removeSite(NurserySite site) {
        this.sites.remove(site);
        site.setUnit(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationUnit that = (LocationUnit) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
