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
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private final List<NurserySite> site = new ArrayList<>();

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

    public List<NurserySite> getSite() {
        return site;
    }

    public void addSite(NurserySite site) {
        this.site.add(site);
    }

    public void removeSite(NurserySite site) {
        this.site.remove(site);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LocationUnit that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
