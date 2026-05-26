package com.example.iis.dto;

public record LoginRequest(
        String email,
        String password
) {
}
