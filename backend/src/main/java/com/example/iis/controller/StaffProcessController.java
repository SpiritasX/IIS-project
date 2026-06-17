package com.example.iis.controller;

import com.example.iis.dto.CancelRequest;
import com.example.iis.dto.StaffProcessResponse;
import com.example.iis.dto.StaffTransitionRequest;
import com.example.iis.service.OrderService;
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
    public List<StaffProcessResponse> getProcesses() {
        return orderService.getStaffProcesses();
    }

    @GetMapping("/{processId}")
    public StaffProcessResponse getProcess(@PathVariable Long processId) {
        return orderService.getStaffProcess(processId);
    }

    @PostMapping("/{processId}/transition")
    public StaffProcessResponse transitionProcess(
            @PathVariable Long processId,
            @RequestBody StaffTransitionRequest request
    ) {
        return orderService.transitionStaffProcess(processId, request);
    }

    @PostMapping("/{processId}/cancel")
    public StaffProcessResponse cancelProcess(
            @PathVariable Long processId,
            @RequestBody CancelRequest request
    ) {
        return orderService.cancelStaffProcess(processId, request);
    }
}
