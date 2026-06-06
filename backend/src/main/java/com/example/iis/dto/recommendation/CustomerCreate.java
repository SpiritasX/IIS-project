package com.example.iis.dto.recommendation;

public class CustomerCreate {
    public Long id;
    public String first_name;
    public String last_name;

    public CustomerCreate(Long id, String firstName, String lastName) {
        this.id = id;
        this.first_name = firstName;
        this.last_name = lastName;
    }
}
