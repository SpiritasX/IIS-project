package com.example.iis.service;

import com.example.iis.dto.PlantDemandResponse;
import com.example.iis.dto.PlantPriceResponse;
import com.example.iis.model.Account;
import com.example.iis.model.OrderHistoryItem;
import com.example.iis.model.Plant;
import com.example.iis.model.PlantPrice;
import com.example.iis.repository.AccountRepository;
import com.example.iis.repository.OrderHistoryItemRepository;
import com.example.iis.repository.PlantPriceRepository;
import com.example.iis.repository.PlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class PlantPriceService {
    private PlantRepository plantRepository;
    private PlantPriceRepository plantPriceRepository;
    private AccountRepository accountRepository;
    private OrderHistoryItemRepository orderHistoryItemRepository;

    public PlantPriceService(PlantRepository plantRepository, PlantPriceRepository plantPriceRepository, AccountRepository accountRepository, OrderHistoryItemRepository orderHistoryItemRepository) {
        this.plantRepository = plantRepository;
        this.plantPriceRepository = plantPriceRepository;
        this.accountRepository = accountRepository;
        this.orderHistoryItemRepository = orderHistoryItemRepository;
    }

    //public Optional<PlantPrice> getPlantPrice(int id) {}
    @Transactional
    public PlantPrice setNewPlantPrice(Long plantId, BigDecimal price, Long changedById) {
        Account changedBy = null;

        if(changedById != null) {
            changedBy = accountRepository.findById(changedById).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be greater than 0");
        }

        Optional<Plant> temp = plantRepository.findById(plantId);
        if(temp.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found.");
        }

        Date currentDate = new Date();
        Optional<PlantPrice> oldPriceOpt = plantPriceRepository.findByPlant_IdAndEndTimeIsNull(plantId);
        if (oldPriceOpt.isPresent()) {
            PlantPrice oldPrice = oldPriceOpt.get();
            oldPrice.setEndTime(currentDate);
            plantPriceRepository.save(oldPrice);
        }

        PlantPrice newPrice = new PlantPrice(price, temp.get());
        newPrice.setChangedBy(changedBy);
        newPrice.setStartTime(currentDate);
        return plantPriceRepository.save(newPrice);
    }

    public PlantPrice rollbackPlantPrice(Long plantId, Long changedById){
        Optional<PlantPrice> pp = plantPriceRepository.findTopByPlant_IdAndEndTimeIsNotNullOrderByEndTimeDesc(plantId);
        if(pp.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant price to rollback not found.");
        }

        return setNewPlantPrice(plantId, pp.get().getPrice(), changedById);
    }

    public List<PlantPriceResponse> priceHistoryView(Long plantId){
        List<PlantPrice> lista = plantPriceRepository.findByPlant_IdOrderByStartTimeAsc(plantId);
        List<PlantPriceResponse> listDto = new ArrayList<>();
        for(PlantPrice p : lista){
            PlantPriceResponse temp = PlantPriceResponse.from(p);
            listDto.add(temp);
        }
        return listDto;
    }

    public List<PlantDemandResponse> plantDemandView(Long plantId) {
        List<OrderHistoryItem> items = orderHistoryItemRepository.findByPlantPrice_Plant_IdAndOrderHistory_Offer_Status_Name(plantId, "Rezervacija");

        Map<String, Long> sumaPoMesecu = new TreeMap<>();

        for (OrderHistoryItem item : items) {
            Date changedAt = item.getOrderHistory().getChangedAt();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(changedAt);

            int year = calendar.get(Calendar.YEAR);
            int monthNumber = calendar.get(Calendar.MONTH) + 1;

            String month = year + "-" + String.format("%02d", monthNumber);

            Long currentSum = sumaPoMesecu.get(month);
            if (currentSum == null) {
                currentSum = 0L;
            }

            sumaPoMesecu.put(month, currentSum + item.getQuantity());
        }

        List<PlantDemandResponse> agregirani = new ArrayList<>();

        for (String month : sumaPoMesecu.keySet()) {
            PlantDemandResponse response = new PlantDemandResponse(month, sumaPoMesecu.get(month));
            agregirani.add(response);
        }

        return agregirani;
    }
}