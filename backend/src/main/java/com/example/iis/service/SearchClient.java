package com.example.iis.service;

import com.example.iis.dto.search.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class SearchClient {

    private final RestClient restClient;

    public SearchClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://search:8001/api/search")
                .build();
    }

    public void createCustomer(Long customerId, String username, String firstName, String lastName, String email) {
        restClient.post()
                .uri("/users")
                .body(new CustomerCreate(customerId, username, firstName, lastName, email))
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteCustomer(Long customerId) {
        restClient.delete()
                .uri("/users/{customerId}", customerId)
                .retrieve()
                .toBodilessEntity();
    }

    public void createPlant(
            Long id,
            String name,
            String description,
            Long varietyId,
            String varietyName,
            Long speciesId,
            String speciesName,
            Long plantTypeId,
            String plantTypeName,
            BigDecimal price
    ) {
        restClient.post()
                .uri("/plants")
                .body(new PlantCreate(id, name, description, varietyId, varietyName, speciesId, speciesName, plantTypeId, plantTypeName, price, new Date(System.currentTimeMillis())))
                .retrieve()
                .toBodilessEntity();
    }

    public void deletePlant(Long plantId) {
        restClient.delete()
                .uri("/plants/{plantId}", plantId)
                .retrieve()
                .toBodilessEntity();
    }
}
