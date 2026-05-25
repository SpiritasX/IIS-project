package com.example.iis.model;

import jakarta.persistence.Entity;

@Entity
public class Customer extends Account {
    private String address;

    public Customer() {
        super();
    }

    public Customer(String username, String password, String firstName, String lastName, String email) {
        super(username, password, firstName, lastName, email);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
