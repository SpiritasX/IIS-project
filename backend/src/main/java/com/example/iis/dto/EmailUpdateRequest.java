package com.example.iis.dto;

public record EmailUpdateRequest(
        String newEmail,
        String repeatedNewEmail,
        String password
) {
}
