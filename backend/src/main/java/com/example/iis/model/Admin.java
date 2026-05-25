package com.example.iis.model;

import jakarta.persistence.Entity;

@Entity
public class Admin extends Account {
    public Admin() {
        super();
    }
    
    public Admin(String username, String password, String firstName, String lastName, String email) {
        super(username, password, firstName, lastName, email);
    }
}
