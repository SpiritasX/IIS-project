package com.example.iis.dto.search;

public record CustomerCreate(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email
) {
}
