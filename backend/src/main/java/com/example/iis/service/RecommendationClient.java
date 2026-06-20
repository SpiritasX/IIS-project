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

    public NoSqlWriteResult createCustomer(Long customerId, String firstName, String lastName) {
        return restClient.post()
                .uri("/customer")
                .body(new CustomerCreate(customerId, firstName, lastName))
                .retrieve()
                .body(NoSqlWriteResult.class);
    }

    public void deleteCustomer(Long customerId) {
        restClient.delete()
                .uri("/customer/{customerId}", customerId)
                .retrieve()
                .toBodilessEntity();
    }

    public NoSqlWriteResult createPlant(Long plantId, String name, Long plantVarietyId) {
        return restClient.post()
                .uri("/plant")
                .body(new PlantCreate(plantId, name, plantVarietyId))
                .retrieve()
                .body(NoSqlWriteResult.class);
    }

    public void deletePlant(Long plantId) {
        restClient.delete()
                .uri("/plant/{plantId}", plantId)
                .retrieve()
                .toBodilessEntity();
    }

    public NoSqlWriteResult createPlantVariety(Long plantVarietyId, String name, String season) {
        return restClient.post()
                .uri("/plant/variety")
                .body(new PlantVarietyCreate(plantVarietyId, name, season))
                .retrieve()
                .body(NoSqlWriteResult.class);
    }

    public void deletePlantVariety(Long plantVarietyId) {
        restClient.delete()
                .uri("/plant/variety/{plantVarietyId}", plantVarietyId)
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
