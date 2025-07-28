package com.bd.spectrum.BMDInfo_server.controller;

import com.bd.spectrum.BMDInfo_server.service.InventoryService;
import org.springframework.http.ResponseEntity;
import com.bd.spectrum.BMDInfo_server.dto.TransferRequest;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    @PostMapping("/transfer-stock")
    public ResponseEntity<String> transferStock(@RequestBody TransferRequest request) {
        inventoryService.transferStock(
            request.getItemId(),
            request.getFromLocation(),
            request.getToLocation(),
            request.getQuantity()
        );
        return ResponseEntity.ok("Stock transferred successfully.");
    }
    @GetMapping("/export-valuation")
public ResponseEntity<?> exportValuation(@RequestParam String format) throws IOException {
    if (format.equalsIgnoreCase("csv")) {
        byte[] data = inventoryService.exportValuationAsCSV();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=valuation.csv")
                .header("Content-Type", "text/csv")
                .body(data);
    } else if (format.equalsIgnoreCase("pdf")) {
        byte[] data = inventoryService.exportValuationAsPDF();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=valuation.pdf")
                .header("Content-Type", "application/pdf")
                .body(data);
    } else {
        return ResponseEntity.badRequest().body("Invalid format. Use 'csv' or 'pdf'.");
    }
}



}
