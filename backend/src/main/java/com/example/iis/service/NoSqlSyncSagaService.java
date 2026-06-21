package com.example.iis.service;

import com.example.iis.model.Customer;
import com.example.iis.model.Plant;
import com.example.iis.model.PlantSpecies;
import com.example.iis.model.PlantType;
import com.example.iis.model.PlantVariety;
import com.example.iis.model.PlantPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class NoSqlSyncSagaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoSqlSyncSagaService.class);

    private final RecommendationClient recommendationClient;
    private final SearchClient searchClient;

    public NoSqlSyncSagaService(RecommendationClient recommendationClient, SearchClient searchClient) {
        this.recommendationClient = recommendationClient;
        this.searchClient = searchClient;
    }

    public void syncCustomerCreated(Customer customer) {
        boolean graphCustomerCreated = false;

        try {
            NoSqlWriteResult graphResult = recommendationClient.createCustomer(
                    customer.getId(),
                    customer.getFirstName(),
                    customer.getLastName()
            );
            graphCustomerCreated = wasCreated(graphResult);

            searchClient.createCustomer(
                    customer.getId(),
                    customer.getUsername(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getEmail()
            );
        } catch (RuntimeException exception) {
            if (graphCustomerCreated) {
                compensate("delete Neo4j customer " + customer.getId(),
                        () -> recommendationClient.deleteCustomer(customer.getId()));
            }

            throw sagaFailure("Customer could not be synchronized with NoSQL stores", exception);
        }
    }

    public void syncCatalogItemCreated(Plant plant, PlantPrice price) {
        PlantVariety variety = plant.getVariety();
        PlantSpecies species = variety.getSpecies();
        PlantType type = species.getType();
        boolean graphVarietyCreated = false;
        boolean graphPlantCreated = false;

        try {
            NoSqlWriteResult varietyResult = recommendationClient.createPlantVariety(
                    variety.getId(),
                    variety.getName(),
                    variety.getSeason()
            );
            graphVarietyCreated = wasCreated(varietyResult);

            NoSqlWriteResult plantResult = recommendationClient.createPlant(
                    plant.getId(),
                    plant.getName(),
                    variety.getId()
            );
            graphPlantCreated = wasCreated(plantResult);

            searchClient.createPlant(
                    plant.getId(),
                    price.getId(),
                    plant.getName(),
                    plant.getDescription(),
                    variety.getId(),
                    variety.getName(),
                    species.getId(),
                    species.getName(),
                    type.getId(),
                    type.getName(),
                    price.getPrice()
            );
        } catch (RuntimeException exception) {
            if (graphPlantCreated) {
                compensate("delete Neo4j plant " + plant.getId(),
                        () -> recommendationClient.deletePlant(plant.getId()));
            }
            if (graphVarietyCreated) {
                compensate("delete Neo4j plant variety " + variety.getId(),
                        () -> recommendationClient.deletePlantVariety(variety.getId()));
            }

            throw sagaFailure("Catalog item could not be synchronized with NoSQL stores", exception);
        }
    }

    private boolean wasCreated(NoSqlWriteResult result) {
        return result != null && result.created();
    }

    private ResponseStatusException sagaFailure(String message, RuntimeException cause) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, message, cause);
    }

    private void compensate(String action, Runnable compensation) {
        try {
            compensation.run();
        } catch (RuntimeException exception) {
            LOGGER.warn("Saga compensation failed: {}", action, exception);
        }
    }
}
