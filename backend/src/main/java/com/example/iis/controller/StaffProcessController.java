package com.example.iis.controller;

import com.example.iis.dto.CancelRequest;
import com.example.iis.dto.StaffProcessResponse;
import com.example.iis.dto.StaffTransitionRequest;
import com.example.iis.security.AccountPrincipal;
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
@RequestMapping("/api/staff/processes")
public class StaffProcessController {
    private final OrderService orderService;

    public StaffProcessController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<StaffProcessResponse> getProcesses(@AuthenticationPrincipal AccountPrincipal principal) {
        return orderService.getStaffProcesses(principal.getRole());
    }

    @GetMapping("/{processId}")
    public StaffProcessResponse getProcess(
            @AuthenticationPrincipal AccountPrincipal principal,
            @PathVariable Long processId
    ) {
        return orderService.getStaffProcess(processId, principal.getRole());
    }

    @PostMapping("/{processId}/transition")
    public StaffProcessResponse transitionProcess(
            @AuthenticationPrincipal AccountPrincipal principal,
            @PathVariable Long processId,
            @RequestBody StaffTransitionRequest request
    ) {
        return orderService.transitionStaffProcess(processId, request, principal.getRole());
    }

    @PostMapping("/{processId}/cancel")
    public StaffProcessResponse cancelProcess(
            @AuthenticationPrincipal AccountPrincipal principal,
            @PathVariable Long processId,
            @RequestBody CancelRequest request
    ) {
        return orderService.cancelStaffProcess(processId, request, principal.getRole());
    }
}
