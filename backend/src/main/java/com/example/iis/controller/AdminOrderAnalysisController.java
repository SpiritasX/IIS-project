package com.example.iis.controller;

import com.example.iis.dto.OrderAnalysisResponse;
import com.example.iis.service.OrderAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/order-analysis")
public class AdminOrderAnalysisController {
    private final OrderAnalysisService orderAnalysisService;

    public AdminOrderAnalysisController(OrderAnalysisService orderAnalysisService) {
        this.orderAnalysisService = orderAnalysisService;
    }

    @GetMapping
    public OrderAnalysisResponse getOrderAnalysis() {
        return orderAnalysisService.getOrderAnalysis();
    }
}
