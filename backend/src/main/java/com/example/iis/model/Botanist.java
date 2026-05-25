package com.example.iis.model;

import jakarta.persistence.Entity;

@Entity
public class Botanist extends Account {

    public Botanist() {
        super();
    }

    public Botanist(String username, String password, String firstName, String lastName, String email) {
        super(username, password, firstName, lastName, email);
    }
}
