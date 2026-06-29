package com.example.iis.dto;

import java.util.Date;

public record DashboardLogEntry(Date timestamp, String type, String plantName, String detail) {
}
