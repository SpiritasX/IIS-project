package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Date createdAt;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private OfferStatus status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "offer_plant_price",
            joinColumns = @JoinColumn(name = "offer_id"),
            inverseJoinColumns = @JoinColumn(name = "plant_price_id")
    )
    private Set<PlantPrice> prices = new LinkedHashSet<>();

    @OneToMany(mappedBy = "offer")
    private Set<Report> reports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "offer")
    private Set<Phase> phases = new LinkedHashSet<>();

    public Offer() {
    }

    public Offer(OfferStatus status, Collection<PlantPrice> prices) {
        this.createdAt = new Date(System.currentTimeMillis());
        this.status = status;
        this.prices.addAll(prices);
    }

    public Long getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public Set<PlantPrice> getPrices() {
        return prices;
    }

    public Set<PlantPrice> getPrice() {
        return prices;
    }

    public void addPrice(PlantPrice price) {
        prices.add(price);
    }

    public void removePrice(PlantPrice price) {
        prices.remove(price);
    }

    public Set<Report> getReports() {
        return reports;
    }

    public Set<Phase> getPhases() {
        return phases;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = new Date(System.currentTimeMillis());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Offer offer = (Offer) o;
        if (id == null || offer.id == null) return false;
        return Objects.equals(id, offer.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
