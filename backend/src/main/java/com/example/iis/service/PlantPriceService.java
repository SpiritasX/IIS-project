package com.example.iis.service;

import com.example.iis.model.Plant;
import com.example.iis.model.PlantPrice;
import com.example.iis.repository.PlantPriceRepository;
import com.example.iis.repository.PlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

@Service
public class PlantPriceService {
    private PlantRepository plantRepository;
    private PlantPriceRepository plantPriceRepository;

    public PlantPriceService(PlantPriceRepository plantPriceRepository) {
        this.plantPriceRepository = plantPriceRepository;
    }

    //public Optional<PlantPrice> getPlantPrice(int id) {}
    @Transactional
    public PlantPrice setNewPlantPrice(Long plantId, BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be greater than 0");
        }

        Optional<Plant> temp = plantRepository.findById(plantId);
        if(temp.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found.");
        }

        Optional<PlantPrice> oldPriceOpt = plantPriceRepository.findByPlant_IdAndEndTimeIsNull(plantId);
        if (oldPriceOpt.isPresent()) {
            PlantPrice oldPrice = oldPriceOpt.get();
            oldPrice.setEndTime(new Date());
            plantPriceRepository.save(oldPrice);
        }

        PlantPrice newPrice = new PlantPrice(price, temp.get());
        return plantPriceRepository.save(newPrice);
    }

    //public PlantPrice rollbackLastPrice(Long plantId){}
}
