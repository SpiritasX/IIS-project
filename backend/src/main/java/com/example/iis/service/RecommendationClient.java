package com.example.iis.service;

import com.example.iis.dto.recommendation.CustomerCreate;
import com.example.iis.dto.recommendation.PlantCreate;
import com.example.iis.dto.recommendation.PlantVarietyCreate;
import com.example.iis.dto.recommendation.PurchaseCreate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RecommendationClient {

    private final RestClient restClient;

    public RecommendationClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://recommendations:8001/api/recommendations")
                .build();
    }

    public void createCustomer(Long customerId, String firstName, String lastName) {
        restClient.post()
                .uri("/customer")
                .body(new CustomerCreate(customerId, firstName, lastName))
                .retrieve()
                .toBodilessEntity();
    }

    public void createPlant(Long plantId, String name, Long plantVarietyId) {
        restClient.post()
                .uri("/plant")
                .body(new PlantCreate(plantId, name, plantVarietyId))
                .retrieve()
                .toBodilessEntity();
    }

    public void createPlantVariety(Long plantVarietyId, String name, String season) {
        restClient.post()
                .uri("/plant/variety")
                .body(new PlantVarietyCreate(plantVarietyId, name, season))
                .retrieve()
                .toBodilessEntity();
    }

    public void createPurchase(Long customerId, Long plantId, Integer quantity) {
        restClient.post()
                .uri("/purchase")
                .body(new PurchaseCreate(customerId, plantId, quantity))
                .retrieve()
                .toBodilessEntity();
    }
}