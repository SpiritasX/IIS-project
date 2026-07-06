package com.example.iis.config;

import com.example.iis.model.Admin;
import com.example.iis.model.Botanist;
import com.example.iis.model.CancellationReason;
import com.example.iis.model.NurserySite;
import com.example.iis.model.Offer;
import com.example.iis.model.Sector;
import com.example.iis.model.StorageSpace;
import com.example.iis.model.StorageSpaceType;
import com.example.iis.model.OfferStatus;
import com.example.iis.model.OrderHistory;
import com.example.iis.model.OrderHistoryItem;
import com.example.iis.model.OrderItem;
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
import com.example.iis.repository.CancellationReasonRepository;
import com.example.iis.repository.NurserySiteRepository;
import com.example.iis.repository.OfferRepository;
import com.example.iis.repository.OrderHistoryItemRepository;
import com.example.iis.repository.StorageSpaceRepository;
import com.example.iis.repository.StorageSpaceTypeRepository;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(
            AccountRepository accountRepository,
            CancellationReasonRepository cancellationReasonRepository,
            OfferStatusRepository offerStatusRepository,
            PhaseTypeRepository phaseTypeRepository,
            PlantCategoryRepository plantCategoryRepository,
            StorageSpaceRepository storageSpaceRepository,
            StorageSpaceTypeRepository storageSpaceTypeRepository,
            NurserySiteRepository nurserySiteRepository,
            PlantTypeRepository plantTypeRepository,
            PlantSpeciesRepository plantSpeciesRepository,
            PlantVarietyRepository plantVarietyRepository,
            PlantRepository plantRepository,
            PlantPriceRepository plantPriceRepository,
            RelocationHistoryRepository relocationHistoryRepository,
            PasswordEncoder passwordEncoder,
            OfferRepository offerRepository,
            OrderHistoryItemRepository orderHistoryItemRepository
    ) {
        return args -> {
            seedStaffAccounts(accountRepository, passwordEncoder);
            seedOrderStatuses(offerStatusRepository);
            seedPhaseTypes(phaseTypeRepository);
            seedCancellationReasons(cancellationReasonRepository);
            seedStorageSpaceTypes(storageSpaceTypeRepository);

            StorageSpace defaultStorageSpace = ensureDefaultStorageSpace(storageSpaceRepository, nurserySiteRepository);
            Sector defaultSector = defaultStorageSpace.getSectors().get(0);
            NurserySite defaultSite = defaultStorageSpace.getNurserySite();

            StorageSpaceType defaultSpaceType = storageSpaceTypeRepository.findAll().stream()
                    .filter(t -> "Staklenik".equals(t.getName()))
                    .findFirst()
                    .orElseGet(() -> storageSpaceTypeRepository.save(new StorageSpaceType("Staklenik")));

            if (plantPriceRepository.count() == 0) {
                seedCatalog(
                        plantCategoryRepository,
                        plantTypeRepository,
                        plantSpeciesRepository,
                        plantVarietyRepository,
                        plantRepository,
                        plantPriceRepository,
                        defaultSpaceType
                );
            }

            assignDefaultStorageSpaceToExistingVarieties(plantVarietyRepository, defaultSpaceType);
            seedInitialStock(plantRepository, defaultSector, defaultSite, relocationHistoryRepository);
            seedBasilDemandTestData(
                    plantRepository,
                    plantPriceRepository,
                    offerStatusRepository,
                    offerRepository,
                    orderHistoryItemRepository
            );
        };
    }

    private void seedBasilDemandTestData(
            PlantRepository plantRepository,
            PlantPriceRepository plantPriceRepository,
            OfferStatusRepository offerStatusRepository,
            OfferRepository offerRepository,
            OrderHistoryItemRepository orderHistoryItemRepository
    ) {
        Plant basil = plantRepository.findAll().stream()
                .filter(plant -> "Basil seedling".equals(plant.getName()))
                .findFirst()
                .orElse(null);

        if (basil == null) {
            return;
        }

        PlantPrice basilPrice = plantPriceRepository
                .findByPlant_IdAndEndTimeIsNull(basil.getId())
                .orElse(null);

        if (basilPrice == null) {
            return;
        }

        saveDemandSnapshot(offerRepository, offerStatusRepository, orderHistoryItemRepository,
                basilPrice, "Rezervacija", LocalDate.of(2026, 1, 12), 4);
        saveDemandSnapshot(offerRepository, offerStatusRepository, orderHistoryItemRepository,
                basilPrice, "Rezervacija", LocalDate.of(2026, 2, 9), 7);
        saveDemandSnapshot(offerRepository, offerStatusRepository, orderHistoryItemRepository,
                basilPrice, "Spremno", LocalDate.of(2026, 3, 15), 11);
        saveDemandSnapshot(offerRepository, offerStatusRepository, orderHistoryItemRepository,
                basilPrice, "Isporuka", LocalDate.of(2026, 4, 18), 15);
        saveDemandSnapshot(offerRepository, offerStatusRepository, orderHistoryItemRepository,
                basilPrice, "Rezervacija", LocalDate.of(2026, 5, 21), 9);
    }

    private void saveDemandSnapshot(
            OfferRepository offerRepository,
            OfferStatusRepository offerStatusRepository,
            OrderHistoryItemRepository orderHistoryItemRepository,
            PlantPrice plantPrice,
            String statusName,
            LocalDate date,
            int quantity
    ) {
        Date changedAt = dateAtStartOfDay(date);

        if (orderHistoryItemRepository
                .existsByPlantPrice_Plant_IdAndOrderHistory_ChangedAtAndOrderHistory_Offer_Status_Name(
                        plantPrice.getPlant().getId(),
                        changedAt,
                        statusName
                )) {
            return;
        }

        OfferStatus status = offerStatusRepository.findByName(statusName)
                .orElseThrow();

        Offer offer = new Offer(status, List.of(plantPrice));
        offer.addItem(new OrderItem(plantPrice, quantity, quantity, quantity));

        OrderHistory history = new OrderHistory(offer, changedAt);
        history.addItem(new OrderHistoryItem(plantPrice, quantity));
        offer.addOrderHistory(history);

        offerRepository.save(offer);
    }

    private Date dateAtStartOfDay(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private StorageSpace ensureDefaultStorageSpace(StorageSpaceRepository storageSpaceRepository,
                                                    NurserySiteRepository nurserySiteRepository) {
        NurserySite site = defaultNurserySite(nurserySiteRepository);

        return storageSpaceRepository.findAll().stream()
                .filter(u -> u.getName().equals("Main Storage Space"))
                .map(storageSpace -> {
                    if (storageSpace.getNurserySite() == null) {
                        storageSpace.setNurserySite(site);
                    }
                    if (storageSpace.getSectors().isEmpty()) {
                        storageSpace.addSector(new Sector("Default sector", 1000L));
                    }
                    return storageSpaceRepository.save(storageSpace);
                })
                .findFirst()
                .orElseGet(() -> {
                    StorageSpace storageSpace = new StorageSpace("Main Storage Space", "Staklenik");
                    storageSpace.setNurserySite(site);
                    Sector sector = new Sector("Default sector", 1000L);
                    storageSpace.addSector(sector);
                    return storageSpaceRepository.save(storageSpace);
                });
    }

    private NurserySite defaultNurserySite(NurserySiteRepository nurserySiteRepository) {
        List<NurserySite> sites = nurserySiteRepository.findAll();

        for (NurserySite site : sites) {
            if ("Novi Sad".equals(site.getName())) {
                return site;
            }
        }

        if (!sites.isEmpty()) {
            return sites.get(0);
        }

        NurserySite seed = new NurserySite("Novi Sad", 45.2671, 19.8335);
        return nurserySiteRepository.save(seed);
    }

    private void seedCatalog(
            PlantCategoryRepository plantCategoryRepository,
            PlantTypeRepository plantTypeRepository,
            PlantSpeciesRepository plantSpeciesRepository,
            PlantVarietyRepository plantVarietyRepository,
            PlantRepository plantRepository,
            PlantPriceRepository plantPriceRepository,
            StorageSpaceType defaultStorageSpaceType
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
                "Keep in full sun and water sparingly.", lavenderSpecies, defaultStorageSpaceType));
        PlantVariety basil = plantVarietyRepository.save(new PlantVariety(
                "Genovese basil", 60.0, "Rich, moist soil",
                "Pinch top leaves often to encourage growth.", basilSpecies, defaultStorageSpaceType));
        PlantVariety olive = plantVarietyRepository.save(new PlantVariety(
                "Arbequina olive", 40.0, "Sandy loam",
                "Place in a warm bright spot and avoid overwatering.", oliveSpecies, defaultStorageSpaceType));
        PlantVariety mint = plantVarietyRepository.save(new PlantVariety(
                "Spearmint", 65.0, "Moist garden soil",
                "Trim runners and keep soil evenly moist.", mintSpecies, defaultStorageSpaceType));
        PlantVariety rose = plantVarietyRepository.save(new PlantVariety(
                "Garden rose", 55.0, "Loamy soil",
                "Prune spent blooms and water at the base.", roseSpecies, defaultStorageSpaceType));

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

    private void seedStorageSpaceTypes(StorageSpaceTypeRepository storageSpaceTypeRepository) {
        List.of("Staklenik", "Plastenik", "Unutrašnje skladište", "Hangar", "Dvorište")
                .forEach(name -> {
                    if (!storageSpaceTypeRepository.existsByName(name)) {
                        storageSpaceTypeRepository.save(new StorageSpaceType(name));
                    }
                });
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

    private void seedCancellationReasons(CancellationReasonRepository cancellationReasonRepository) {
        List.of(
                "Customer cancellation",
                "Staff cancellation"
        ).forEach(name -> saveCancellationReasonIfMissing(cancellationReasonRepository, name));
    }

    private void saveCancellationReasonIfMissing(
            CancellationReasonRepository cancellationReasonRepository,
            String name
    ) {
        if (cancellationReasonRepository.findByName(name).isEmpty()) {
            cancellationReasonRepository.save(new CancellationReason(name));
        }
    }

    private void assignDefaultStorageSpaceToExistingVarieties(
            PlantVarietyRepository plantVarietyRepository,
            StorageSpaceType defaultSpaceType
    ) {
        List<PlantVariety> varieties = plantVarietyRepository.findAll();
        boolean changed = false;

        for (PlantVariety variety : varieties) {
            if (variety.getStorageSpaceType() == null) {
                variety.setStorageSpaceType(defaultSpaceType);
                changed = true;
            }
        }

        if (changed) {
            plantVarietyRepository.saveAll(varieties);
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
            Sector sector,
            NurserySite defaultSite,
            RelocationHistoryRepository relocationHistoryRepository
    ) {
        List<RelocationHistory> histories = relocationHistoryRepository.findAll();
        boolean changed = false;

        for (RelocationHistory rh : histories) {
            if (rh.getNurserySite() == null) {
                rh.setNurserySite(defaultSite);
                changed = true;
            }

            if (rh.getSector() == null) {
                rh.setSector(sector);
                changed = true;
            }
        }

        if (changed) {
            relocationHistoryRepository.saveAll(histories);
        }

        for (Plant plant : plantRepository.findAll()) {
            if (relocationHistoryRepository.countByPlant_IdAndEndTimeIsNull(plant.getId()) == 0) {
                RelocationHistory rh = new RelocationHistory("Initial stock", plant, sector, 20L);
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
