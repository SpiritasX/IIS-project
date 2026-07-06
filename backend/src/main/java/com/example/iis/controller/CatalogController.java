package com.example.iis.controller;

import com.example.iis.dto.ProductResponse;
import com.example.iis.security.AccountPrincipal;
import com.example.iis.service.CatalogService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plants")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<ProductResponse> getProducts(@AuthenticationPrincipal AccountPrincipal principal) {
        Long customerId = principal != null && principal.isCustomer() ? principal.getAccountId() : null;
        return catalogService.getProducts(customerId);
    }
}
