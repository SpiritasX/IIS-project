package com.example.iis.config;

import com.example.iis.model.*;
import com.example.iis.repository.*;
import com.example.iis.service.RecommendationClient;
import com.example.iis.service.SearchClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(
            AdminRepository adminRepository,
            OfferStatusRepository offerStatusRepository,
            PhaseTypeRepository phaseTypeRepository,
            PlantCategoryRepository plantCategoryRepository,
            PlantTypeRepository plantTypeRepository,
            PlantSpeciesRepository plantSpeciesRepository,
            PlantVarietyRepository plantVarietyRepository,
            PlantRepository plantRepository,
            PlantPriceRepository plantPriceRepository,
            RecommendationClient recommendationClient,
            SearchClient searchClient
    ) {
        return args -> {
            seedOrderStatuses(offerStatusRepository);
            seedPhaseTypes(phaseTypeRepository);

            if (plantPriceRepository.count() > 0) {
                return;
            }

            adminRepository.save(new Admin("admin", "admin", "Pera", "Peric", "admin@example.com"));

            PlantCategory flowers = plantCategoryRepository.save(new PlantCategory("Flowers"));
            PlantCategory herbs = plantCategoryRepository.save(new PlantCategory("Herbs"));
            PlantCategory trees = plantCategoryRepository.save(new PlantCategory("Trees"));

            PlantType floweringPlants = plantTypeRepository.save(new PlantType("Flowering plants", flowers));
            PlantType culinaryHerbs = plantTypeRepository.save(new PlantType("Culinary herbs", herbs));
            PlantType fruitTrees = plantTypeRepository.save(new PlantType("Fruit trees", trees));

            PlantSpecies lavenderSpecies = plantSpeciesRepository.save(new PlantSpecies("Lavender", floweringPlants));
            PlantSpecies roseSpecies = plantSpeciesRepository.save(new PlantSpecies("Rose", floweringPlants));
            PlantSpecies basilSpecies = plantSpeciesRepository.save(new PlantSpecies("Basil", culinaryHerbs));
            PlantSpecies mintSpecies = plantSpeciesRepository.save(new PlantSpecies("Mint", culinaryHerbs));
            PlantSpecies oliveSpecies = plantSpeciesRepository.save(new PlantSpecies("Olive", fruitTrees));

            PlantVariety lavender = plantVarietyRepository.save(new PlantVariety(
                    "English lavender",
                    45.0,
                    "Well-drained alkaline soil",
                    "Keep in full sun and water sparingly.",
                    "SUMMER",
                    lavenderSpecies
            ));
            PlantVariety basil = plantVarietyRepository.save(new PlantVariety(
                    "Genovese basil",
                    60.0,
                    "Rich, moist soil",
                    "Pinch top leaves often to encourage growth.",
                    "WINTER",
                    basilSpecies
            ));
            PlantVariety olive = plantVarietyRepository.save(new PlantVariety(
                    "Arbequina olive",
                    40.0,
                    "Sandy loam",
                    "Place in a warm bright spot and avoid overwatering.",
                    "SUMMER",
                    oliveSpecies
            ));
            PlantVariety mint = plantVarietyRepository.save(new PlantVariety(
                    "Spearmint",
                    65.0,
                    "Moist garden soil",
                    "Trim runners and keep soil evenly moist.",
                    "WINTER",
                    mintSpecies
            ));
            PlantVariety rose = plantVarietyRepository.save(new PlantVariety(
                    "Garden rose",
                    55.0,
                    "Loamy soil",
                    "Prune spent blooms and water at the base.",
                    "SUMMER",
                    roseSpecies
            ));

            recommendationClient.createPlantVariety(lavender.getId(), lavender.getName(), lavender.getSeason());
            recommendationClient.createPlantVariety(basil.getId(), basil.getName(), basil.getSeason());
            recommendationClient.createPlantVariety(olive.getId(), olive.getName(), olive.getSeason());
            recommendationClient.createPlantVariety(mint.getId(), mint.getName(), mint.getSeason());
            recommendationClient.createPlantVariety(rose.getId(), rose.getName(), rose.getSeason());

            savePlantWithPrice(
                    plantRepository,
                    plantPriceRepository,
                    recommendationClient,
                    searchClient,
                    new Plant(
                            "Lavender starter",
                            "Hardy young lavender plant with rich fragrance and strong roots.",
                            "Cuttings",
                            "Available",
                            lavender
                    ),
                    new BigDecimal("1000")
            );
            savePlantWithPrice(
                    plantRepository,
                    plantPriceRepository,
                    recommendationClient,
                    searchClient,
                    new Plant(
                            "Basil seedling",
                            "Fresh culinary basil seedling ready for a sunny kitchen window.",
                            "Seed",
                            "Available",
                            basil
                    ),
                    new BigDecimal("750")
            );
            savePlantWithPrice(
                    plantRepository,
                    plantPriceRepository,
                    recommendationClient,
                    searchClient,
                    new Plant(
                            "Olive sapling",
                            "Mediterranean olive sapling suited for patios and warm gardens.",
                            "Grafting",
                            "Available",
                            olive
                    ),
                    new BigDecimal("1800")
            );
            savePlantWithPrice(
                    plantRepository,
                    plantPriceRepository,
                    recommendationClient,
                    searchClient,
                    new Plant(
                            "Mint pot",
                            "Fast-growing mint in a compact nursery pot for easy transplanting.",
                            "Division",
                            "Available",
                            mint
                    ),
                    new BigDecimal("650")
            );
            savePlantWithPrice(
                    plantRepository,
                    plantPriceRepository,
                    recommendationClient,
                    searchClient,
                    new Plant(
                            "Rose bush",
                            "Classic rose bush with seasonal blooms and balanced growth.",
                            "Cuttings",
                            "Available",
                            rose
                    ),
                    new BigDecimal("1400")
            );
        };
    }

    private void seedOrderStatuses(OfferStatusRepository offerStatusRepository) {
        saveStatusIfMissing(offerStatusRepository, "Pending");
        saveStatusIfMissing(offerStatusRepository, "Delivered");
        saveStatusIfMissing(offerStatusRepository, "Cancelled");
    }

    private void seedPhaseTypes(PhaseTypeRepository phaseTypeRepository) {
        if (phaseTypeRepository.findByName("Order placed").isEmpty()) {
            phaseTypeRepository.save(new PhaseType("Order placed"));
        }
    }

    private void saveStatusIfMissing(OfferStatusRepository offerStatusRepository, String name) {
        if (offerStatusRepository.findByName(name).isEmpty()) {
            offerStatusRepository.save(new OfferStatus(name));
        }
    }

    private void savePlantWithPrice(
            PlantRepository plantRepository,
            PlantPriceRepository plantPriceRepository,
            RecommendationClient recommendationClient,
            SearchClient searchClient,
            Plant plant,
            BigDecimal price
    ) {
        Plant savedPlant = plantRepository.save(plant);
        recommendationClient.createPlant(savedPlant.getId(), savedPlant.getName());
        plantPriceRepository.save(new PlantPrice(price, savedPlant));
        searchClient.createPlant(
                savedPlant.getId(),
                savedPlant.getName(),
                savedPlant.getDescription(),
                savedPlant.getVariety().getId(),
                savedPlant.getVariety().getName(),
                savedPlant.getVariety().getSpecies().getId(),
                savedPlant.getVariety().getSpecies().getName(),
                savedPlant.getVariety().getSpecies().getType().getId(),
                savedPlant.getVariety().getSpecies().getType().getName(),
                price);
    }
}
