package com.example.iis.service;

import com.example.iis.dto.PlantPriceResponse;
import com.example.iis.model.Account;
import com.example.iis.model.Plant;
import com.example.iis.model.PlantPrice;
import com.example.iis.repository.AccountRepository;
import com.example.iis.repository.PlantPriceRepository;
import com.example.iis.repository.PlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PlantPriceService {
    private PlantRepository plantRepository;
    private PlantPriceRepository plantPriceRepository;
    private AccountRepository accountRepository;

    public PlantPriceService(PlantRepository plantRepository, PlantPriceRepository plantPriceRepository, AccountRepository accountRepository) {
        this.plantRepository = plantRepository;
        this.plantPriceRepository = plantPriceRepository;
        this.accountRepository = accountRepository;
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
}
