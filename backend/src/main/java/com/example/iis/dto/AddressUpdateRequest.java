package com.example.iis.dto;

public record AddressUpdateRequest(
        String country,
        String city,
        String address,
        String zipCode
) {
}
