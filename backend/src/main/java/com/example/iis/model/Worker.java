package com.example.iis.model;

import jakarta.persistence.Entity;

@Entity
public class Worker extends Account {
    public Worker() {
        super();
    }

    public Worker(String username, String password, String firstName, String lastName, String email) {
        super(username, password, firstName, lastName, email);
    }
}
