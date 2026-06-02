package com.example.iis.dto;

public record PersonalDataUpdateRequest(
        String firstName,
        String lastName,
        String phoneNumber
) {
}
