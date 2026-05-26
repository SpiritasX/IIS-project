package com.example.iis.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Worker extends Account {
    @OneToMany(mappedBy = "worker")
    private List<RelocationHistory> relocationHistories = new ArrayList<>();

    public Worker() {
        super();
    }

    public Worker(String username, String password, String firstName, String lastName, String email) {
        super(username, password, firstName, lastName, email);
    }

    public List<RelocationHistory> getRelocationHistories() {
        return relocationHistories;
    }
}
