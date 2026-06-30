package com.example.iis.controller;

import com.example.iis.dto.CancelRequest;
import com.example.iis.dto.CreateOrderRequest;
import com.example.iis.dto.OrderResponse;
import com.example.iis.dto.UpdateOrderRequest;
import com.example.iis.security.AccountPrincipal;
import com.example.iis.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
            @AuthenticationPrincipal AccountPrincipal principal,
            @RequestBody CreateOrderRequest request
    ) {
        return orderService.createOrder(principal.getAccountId(), request);
    }

    @GetMapping("/orders")
    public List<OrderResponse> getCustomerOrders(@AuthenticationPrincipal AccountPrincipal principal) {
        return orderService.getOrdersForCustomer(principal.getAccountId());
    }

    @GetMapping("/orders/{orderId}")
    public OrderResponse getOrder(
            @AuthenticationPrincipal AccountPrincipal principal,
            @PathVariable Long orderId
    ) {
        return orderService.getOrderForCustomer(principal.getAccountId(), orderId);
    }

    @PutMapping("/orders/{orderId}")
    public OrderResponse updateOrder(
            @AuthenticationPrincipal AccountPrincipal principal,
            @PathVariable Long orderId,
            @RequestBody UpdateOrderRequest request
    ) {
        return orderService.updateOrder(principal.getAccountId(), orderId, request);
    }

    @PostMapping("/orders/{orderId}/accept")
    public OrderResponse acceptOffer(
            @AuthenticationPrincipal AccountPrincipal principal,
            @PathVariable Long orderId
    ) {
        return orderService.acceptOffer(principal.getAccountId(), orderId);
    }

    @PostMapping("/orders/{orderId}/reject")
    public OrderResponse rejectOffer(
            @AuthenticationPrincipal AccountPrincipal principal,
            @PathVariable Long orderId
    ) {
        return orderService.rejectOffer(principal.getAccountId(), orderId);
    }

    @PostMapping("/orders/{orderId}/cancel")
    public OrderResponse cancelOrder(
            @AuthenticationPrincipal AccountPrincipal principal,
            @PathVariable Long orderId,
            @RequestBody CancelRequest request
    ) {
        return orderService.cancelOrder(principal.getAccountId(), orderId, request);
    }
}
