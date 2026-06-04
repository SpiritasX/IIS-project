package com.example.iis.dto;

public record PasswordUpdateRequest(
        String oldPassword,
        String newPassword,
        String repeatedNewPassword
) {
}
