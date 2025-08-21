package com.bd.spectrum.BMDInfo_server.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

import com.bd.spectrum.BMDInfo_server.dto.AddInventoryItemRequest;
import com.bd.spectrum.BMDInfo_server.model.InventoryItem;
import com.bd.spectrum.BMDInfo_server.model.InventoryValuation;
import com.bd.spectrum.BMDInfo_server.model.StockMovementLog;

import java.util.List;
/**
 * Service interface for inventory management.
 * Defines operations for stock handling, valuation calculations,
 * exporting reports, and retrieving logs/history.
 */
public interface InventoryService {
    BigDecimal calculateFIFO(UUID itemId, String location);
    BigDecimal calculateLIFO(UUID itemId, String location);
    BigDecimal calculateWeightedAverage(UUID itemId, String location);

    void transferStock(UUID itemId, String fromLocation, String toLocation, int quantity);

    byte[] exportValuationAsCSV(UUID itemId, String location) throws IOException;
    byte[] exportValuationAsPDF(UUID itemId, String location) throws IOException;


    void addStock(UUID itemId, int quantity, BigDecimal pricePerUnit, String location);

    InventoryItem saveItem(AddInventoryItemRequest request);

    List<StockMovementLog> getAllMovements();
    List<InventoryValuation> getAllValuations();
    List<InventoryItem> getAllItems();

    List<InventoryValuation> getValuationHistory(UUID itemId, String location);
}
