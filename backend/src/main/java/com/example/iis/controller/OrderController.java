package com.example.iis.controller;

import com.example.iis.dto.CreateOrderRequest;
import com.example.iis.dto.OrderResponse;
import com.example.iis.security.CustomerPrincipal;
import com.example.iis.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public OrderResponse createOrder(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @RequestBody CreateOrderRequest request
    ) {
        return orderService.createOrder(principal.getCustomerId(), request);
    }

    @GetMapping("/orders")
    public List<OrderResponse> getCustomerOrders(@AuthenticationPrincipal CustomerPrincipal principal) {
        return orderService.getOrdersForCustomer(principal.getCustomerId());
    }

    @GetMapping("/orders/{orderId}")
    public OrderResponse getOrder(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @PathVariable Long orderId
    ) {
        return orderService.getOrderForCustomer(principal.getCustomerId(), orderId);
    }
}
