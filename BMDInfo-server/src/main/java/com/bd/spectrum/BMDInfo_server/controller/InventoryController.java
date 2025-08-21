package com.bd.spectrum.BMDInfo_server.controller;

import com.bd.spectrum.BMDInfo_server.service.InventoryService;
import com.bd.spectrum.BMDInfo_server.dto.TransferRequest;
import com.bd.spectrum.BMDInfo_server.dto.AddInventoryItemRequest;

import java.util.List;
import java.util.UUID;
import java.io.IOException;
import java.math.BigDecimal;

import com.bd.spectrum.BMDInfo_server.model.InventoryItem;
import com.bd.spectrum.BMDInfo_server.model.InventoryValuation;
import com.bd.spectrum.BMDInfo_server.model.StockMovementLog;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    /** Calculate FIFO value for an item at a given location */
    @GetMapping("/calculate-fifo")
    public ResponseEntity<BigDecimal> calculateFifo(
            @RequestParam UUID itemId,
            @RequestParam String location) {
        BigDecimal value = inventoryService.calculateFIFO(itemId, location);
        return ResponseEntity.ok(value);
    }

    /** Calculate LIFO value for an item at a given location */
    @GetMapping("/calculate-lifo")
    public ResponseEntity<BigDecimal> calculateLifo(
            @RequestParam UUID itemId,
            @RequestParam String location) {
        BigDecimal value = inventoryService.calculateLIFO(itemId, location);
        return ResponseEntity.ok(value);
    }
    /** Calculate WA value for an item at a given location */
    @GetMapping("/calculate-weighted")
    public ResponseEntity<BigDecimal> calculateWeightedAvg(
            @RequestParam UUID itemId,
            @RequestParam String location) {
        BigDecimal value = inventoryService.calculateWeightedAverage(itemId, location);
        return ResponseEntity.ok(value);
    }

    // Valuations
    @GetMapping("/valuations")
    public ResponseEntity<List<InventoryValuation>> getAllValuations() {
        return ResponseEntity.ok(inventoryService.getAllValuations());
    }
    /** Fetch valuation history for an item  */
    @GetMapping("/valuation-history")
    public ResponseEntity<List<InventoryValuation>> getValuationHistory(
            @RequestParam UUID itemId,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(inventoryService.getValuationHistory(itemId, location));
    }
    /** Handles stock transfer between locations */
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
    /** to add new item stock to inventory */
    @PostMapping("/add-stock")
    public ResponseEntity<String> addStock(@RequestParam UUID itemId,
                                           @RequestParam int quantity,
                                           @RequestParam BigDecimal pricePerUnit,
                                           @RequestParam String location) {
        inventoryService.addStock(itemId, quantity, pricePerUnit, location);
        return ResponseEntity.ok("Stock added and transaction logged.");
    }
     
    /** Adding an item to inventory */
    @PostMapping("/add-item")
    public ResponseEntity<String> addItem(@RequestBody AddInventoryItemRequest request) {
        InventoryItem savedItem = inventoryService.saveItem(request);
        return ResponseEntity.ok("Item added with ID: " + savedItem.getId());
    }

    /** for pdf and csv generation */
    @GetMapping("/export-valuation")
    public ResponseEntity<?> exportValuation(
            @RequestParam String format,
            @RequestParam(required = false) UUID itemId,
            @RequestParam(required = false) String location
    ) throws IOException {
        if (format.equalsIgnoreCase("csv")) {
            byte[] data = inventoryService.exportValuationAsCSV(itemId, location); 
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=valuation.csv")
                    .header("Content-Type", "text/csv")
                    .body(data);
        } else if (format.equalsIgnoreCase("pdf")) {
            byte[] data = inventoryService.exportValuationAsPDF(itemId, location); 
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=valuation.pdf")
                    .header("Content-Type", "application/pdf")
                    .body(data);
        } else {
            return ResponseEntity.badRequest().body("Invalid format. Use 'csv' or 'pdf'.");
        }
    }



    //Items & Movements 
    @GetMapping("/items")
    public ResponseEntity<List<InventoryItem>> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    // Adjustment logs (audit trail)
    @GetMapping("/movements")
    public ResponseEntity<List<StockMovementLog>> getAllMovements() {
        return ResponseEntity.ok(inventoryService.getAllMovements());
    }
}
