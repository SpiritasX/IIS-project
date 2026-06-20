package com.example.iis.service;

public record NoSqlWriteResult(String status) {
    public boolean created() {
        return "created".equalsIgnoreCase(status);
    }
}
