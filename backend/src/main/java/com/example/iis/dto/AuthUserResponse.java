package com.example.iis.dto;

public record AuthUserResponse(
        Long id,
        String email,
        String username,
        String name,
        String address,
        String firstName,
        String lastName,
        Integer age,
        String phoneNumber,
        String addressLine,
        String city,
        String country,
        String zipCode,
        String role
) {
}
