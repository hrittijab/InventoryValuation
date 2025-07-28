package com.bd.spectrum.BMDInfo_server.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

public interface InventoryService {
    BigDecimal calculateFIFO(UUID itemId, String location);
    BigDecimal calculateLIFO(UUID itemId, String location);
    BigDecimal calculateWeightedAverage(UUID itemId, String location);
    void transferStock(UUID itemId, String fromLocation, String toLocation, int quantity);
    byte[] exportValuationAsCSV() throws IOException;
    byte[] exportValuationAsPDF() throws IOException;
}
