package com.bd.spectrum.BMDInfo_server.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

import com.bd.spectrum.BMDInfo_server.dto.AddInventoryItemRequest;
import com.bd.spectrum.BMDInfo_server.model.InventoryItem;
import com.bd.spectrum.BMDInfo_server.model.StockMovementLog;

import java.util.List;


public interface InventoryService {
    BigDecimal calculateFIFO(UUID itemId, String location);
    BigDecimal calculateLIFO(UUID itemId, String location);
    BigDecimal calculateWeightedAverage(UUID itemId, String location);
    void transferStock(UUID itemId, String fromLocation, String toLocation, int quantity);
    byte[] exportValuationAsCSV() throws IOException;
    byte[] exportValuationAsPDF() throws IOException;
    void addStock(UUID itemId, int quantity, BigDecimal pricePerUnit, String location);
InventoryItem saveItem(AddInventoryItemRequest request);
List<StockMovementLog> getAllMovements();

List<InventoryItem> getAllItems();


}
