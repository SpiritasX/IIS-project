package com.example.iis.model;

import jakarta.persistence.*;

@Entity
public class StorageSpaceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public StorageSpaceType() {
    }

    public StorageSpaceType(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
