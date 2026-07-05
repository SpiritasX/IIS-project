package com.example.iis.dto;

import java.util.List;

public record CategoryTreeItem(Long id, String name, List<TypeTreeItem> types) {}
