package com.example.iis.config;

import com.example.iis.model.Admin;
import com.example.iis.model.Botanist;
import com.example.iis.model.LocationParcel;
import com.example.iis.model.LocationUnit;
import com.example.iis.model.NurserySite;
import com.example.iis.model.OfferStatus;
import com.example.iis.model.PhaseType;
import com.example.iis.model.Plant;
import com.example.iis.model.PlantCategory;
import com.example.iis.model.PlantPrice;
import com.example.iis.model.PlantSpecies;
import com.example.iis.model.PlantType;
import com.example.iis.model.PlantVariety;
import com.example.iis.model.RelocationHistory;
import com.example.iis.model.Worker;
import com.example.iis.repository.AccountRepository;
import com.example.iis.repository.LocationUnitRepository;
import com.example.iis.repository.OfferStatusRepository;
import com.example.iis.repository.PhaseTypeRepository;
import com.example.iis.repository.PlantCategoryRepository;
import com.example.iis.repository.PlantPriceRepository;
import com.example.iis.repository.PlantRepository;
import com.example.iis.repository.PlantSpeciesRepository;
import com.example.iis.repository.PlantTypeRepository;
import com.example.iis.repository.PlantVarietyRepository;
import com.example.iis.repository.RelocationHistoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(
            AccountRepository accountRepository,
            OfferStatusRepository offerStatusRepository,
            PhaseTypeRepository phaseTypeRepository,
            PlantCategoryRepository plantCategoryRepository,
            LocationUnitRepository locationUnitRepository,
            PlantTypeRepository plantTypeRepository,
            PlantSpeciesRepository plantSpeciesRepository,
            PlantVarietyRepository plantVarietyRepository,
            PlantRepository plantRepository,
            PlantPriceRepository plantPriceRepository,
            RelocationHistoryRepository relocationHistoryRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            seedStaffAccounts(accountRepository, passwordEncoder);
            seedOrderStatuses(offerStatusRepository);
            seedPhaseTypes(phaseTypeRepository);

            LocationUnit defaultUnit = ensureDefaultUnit(locationUnitRepository);
            LocationParcel defaultParcel = defaultUnit.getParcels().get(0);
            NurserySite defaultSite = defaultParcel.getSites().get(0);

            if (plantPriceRepository.count() == 0) {
                seedCatalog(
                        plantCategoryRepository,
                        plantTypeRepository,
                        plantSpeciesRepository,
                        plantVarietyRepository,
                        plantRepository,
                        plantPriceRepository,
                        defaultUnit
                );
            }

            seedInitialStock(plantRepository, defaultParcel, defaultSite, relocationHistoryRepository);
        };
    }

    private LocationUnit ensureDefaultUnit(LocationUnitRepository locationUnitRepository) {
        return locationUnitRepository.findAll().stream()
                .filter(u -> u.getName().equals("Main Unit"))
                .findFirst()
                .orElseGet(() -> {
                    LocationUnit unit = new LocationUnit("Main Unit", "Greenhouse");
                    LocationParcel parcel = new LocationParcel("Default sales parcel", "Sales stock", 1000L);
                    NurserySite site = new NurserySite("Site A", 45.2671, 19.8335);
                    parcel.addSite(site);
                    unit.addParcel(parcel);
                    return locationUnitRepository.save(unit);
                });
    }

    private void seedCatalog(
            PlantCategoryRepository plantCategoryRepository,
            PlantTypeRepository plantTypeRepository,
            PlantSpeciesRepository plantSpeciesRepository,
            PlantVarietyRepository plantVarietyRepository,
            PlantRepository plantRepository,
            PlantPriceRepository plantPriceRepository,
            LocationUnit defaultUnit
    ) {
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
                "English lavender", 45.0, "Well-drained alkaline soil",
                "Keep in full sun and water sparingly.", lavenderSpecies, defaultUnit));
        PlantVariety basil = plantVarietyRepository.save(new PlantVariety(
                "Genovese basil", 60.0, "Rich, moist soil",
                "Pinch top leaves often to encourage growth.", basilSpecies, defaultUnit));
        PlantVariety olive = plantVarietyRepository.save(new PlantVariety(
                "Arbequina olive", 40.0, "Sandy loam",
                "Place in a warm bright spot and avoid overwatering.", oliveSpecies, defaultUnit));
        PlantVariety mint = plantVarietyRepository.save(new PlantVariety(
                "Spearmint", 65.0, "Moist garden soil",
                "Trim runners and keep soil evenly moist.", mintSpecies, defaultUnit));
        PlantVariety rose = plantVarietyRepository.save(new PlantVariety(
                "Garden rose", 55.0, "Loamy soil",
                "Prune spent blooms and water at the base.", roseSpecies, defaultUnit));

        savePlantWithPrice(plantRepository, plantPriceRepository,
                new Plant("Lavender starter", "Hardy young lavender plant with rich fragrance and strong roots.",
                        "Cuttings", "Available", lavender), new BigDecimal("1000"));
        savePlantWithPrice(plantRepository, plantPriceRepository,
                new Plant("Basil seedling", "Fresh culinary basil seedling ready for a sunny kitchen window.",
                        "Seed", "Available", basil), new BigDecimal("750"));
        savePlantWithPrice(plantRepository, plantPriceRepository,
                new Plant("Olive sapling", "Mediterranean olive sapling suited for patios and warm gardens.",
                        "Grafting", "Available", olive), new BigDecimal("1800"));
        savePlantWithPrice(plantRepository, plantPriceRepository,
                new Plant("Mint pot", "Fast-growing mint in a compact nursery pot for easy transplanting.",
                        "Division", "Available", mint), new BigDecimal("650"));
        savePlantWithPrice(plantRepository, plantPriceRepository,
                new Plant("Rose bush", "Classic rose bush with seasonal blooms and balanced growth.",
                        "Cuttings", "Available", rose), new BigDecimal("1400"));
    }

    private void seedOrderStatuses(OfferStatusRepository offerStatusRepository) {
        List.of("Pending", "Delivered", "Cancelled", "Ponuda", "Rezervacija",
                "Spremno", "Isporuka", "Isporuceno", "Odbijeno", "Isteklo", "Otkazano")
                .forEach(status -> saveStatusIfMissing(offerStatusRepository, status));
    }

    private void seedPhaseTypes(PhaseTypeRepository phaseTypeRepository) {
        List.of("Order placed", "Ponuda", "Rezervacija", "Spremno", "Isporuka")
                .forEach(name -> savePhaseTypeIfMissing(phaseTypeRepository, name));
    }

    private void saveStatusIfMissing(OfferStatusRepository offerStatusRepository, String name) {
        if (offerStatusRepository.findByName(name).isEmpty()) {
            offerStatusRepository.save(new OfferStatus(name));
        }
    }

    private void savePhaseTypeIfMissing(PhaseTypeRepository phaseTypeRepository, String name) {
        if (phaseTypeRepository.findByName(name).isEmpty()) {
            phaseTypeRepository.save(new PhaseType(name));
        }
    }

    private void seedStaffAccounts(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        if (!accountRepository.existsByEmail("admin@example.com")) {
            accountRepository.save(new Admin(uniqueUsername(accountRepository, "admin"),
                    passwordEncoder.encode("admin"), "Admin", "User", "admin@example.com"));
        }
        if (!accountRepository.existsByEmail("botanist@example.com")) {
            accountRepository.save(new Botanist(uniqueUsername(accountRepository, "botanist"),
                    passwordEncoder.encode("botanist"), "Botanist", "User", "botanist@example.com"));
        }
        if (!accountRepository.existsByEmail("worker@example.com")) {
            accountRepository.save(new Worker(uniqueUsername(accountRepository, "worker"),
                    passwordEncoder.encode("worker"), "Worker", "User", "worker@example.com"));
        }
    }

    private String uniqueUsername(AccountRepository accountRepository, String base) {
        String candidate = base;
        int suffix = 1;
        while (accountRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    private void seedInitialStock(
            PlantRepository plantRepository,
            LocationParcel parcel,
            NurserySite defaultSite,
            RelocationHistoryRepository relocationHistoryRepository
    ) {
        List<RelocationHistory> unassigned = relocationHistoryRepository.findByNurserySiteIsNull();
        if (!unassigned.isEmpty()) {
            for (RelocationHistory rh : unassigned) {
                rh.setNurserySite(defaultSite);
            }
            relocationHistoryRepository.saveAll(unassigned);
        }

        for (Plant plant : plantRepository.findAll()) {
            if (relocationHistoryRepository.countByPlant_IdAndEndTimeIsNull(plant.getId()) == 0) {
                RelocationHistory rh = new RelocationHistory("Initial stock", plant, parcel, 20L);
                rh.setNurserySite(defaultSite);
                relocationHistoryRepository.save(rh);
            }
        }
    }

    private void savePlantWithPrice(PlantRepository plantRepository, PlantPriceRepository plantPriceRepository,
                                    Plant plant, BigDecimal price) {
        Plant savedPlant = plantRepository.save(plant);
        plantPriceRepository.save(new PlantPrice(price, savedPlant));
    }
}
