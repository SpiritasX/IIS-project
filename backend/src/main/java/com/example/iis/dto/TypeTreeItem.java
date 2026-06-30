package com.example.iis.dto;

import java.util.List;

public record TypeTreeItem(Long id, String name, List<SpeciesTreeItem> species) {}
