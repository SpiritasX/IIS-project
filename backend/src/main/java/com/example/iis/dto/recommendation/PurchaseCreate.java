package com.example.iis.dto.recommendation;

public class PurchaseCreate {
    public Long customer_id;
    public Long plant_id;
    public Integer quantity;

    public PurchaseCreate(Long customer_id, Long plant_id, Integer quantity) {
        this.customer_id = customer_id;
        this.plant_id = plant_id;
        this.quantity = quantity;
    }
}
