package com.example.iis.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Date createdAt;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private OfferStatus status;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private final List<PlantPrice> price = new ArrayList<>();

    public Offer() {
    }

    public Offer(OfferStatus status, List<PlantPrice> price) {
        this.createdAt = new Date(System.currentTimeMillis());
        this.status = status;
        this.price.addAll(price);
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

    public List<PlantPrice> getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Offer offer)) return false;
        return Objects.equals(id, offer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
