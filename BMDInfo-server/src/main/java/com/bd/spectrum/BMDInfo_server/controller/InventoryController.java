package com.bd.spectrum.BMDInfo_server.controller;

import com.bd.spectrum.BMDInfo_server.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/calculate-fifo")
    public ResponseEntity<BigDecimal> calculateFifo(
            @RequestParam UUID itemId,
            @RequestParam String location) {
        BigDecimal value = inventoryService.calculateFIFO(itemId, location);
        return ResponseEntity.ok(value);
    }

    @GetMapping("/calculate-lifo")
    public ResponseEntity<BigDecimal> calculateLifo(
            @RequestParam UUID itemId,
            @RequestParam String location) {
        BigDecimal value = inventoryService.calculateLIFO(itemId, location);
        return ResponseEntity.ok(value);
    }

    @GetMapping("/calculate-weighted")
    public ResponseEntity<BigDecimal> calculateWeightedAvg(
            @RequestParam UUID itemId,
            @RequestParam String location) {
        BigDecimal value = inventoryService.calculateWeightedAverage(itemId, location);
        return ResponseEntity.ok(value);
    }

}
