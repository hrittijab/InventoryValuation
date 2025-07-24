package com.bd.spectrum.BMDInfo_server.service;

import com.bd.spectrum.BMDInfo_server.model.InventoryItem;
import com.bd.spectrum.BMDInfo_server.model.InventoryValuation;
import com.bd.spectrum.BMDInfo_server.model.StockMovementLog;
import com.bd.spectrum.BMDInfo_server.model.StockTransaction;
import com.bd.spectrum.BMDInfo_server.repository.InventoryItemRepository;
import com.bd.spectrum.BMDInfo_server.repository.InventoryValuationRepository;
import com.bd.spectrum.BMDInfo_server.repository.StockMovementLogRepository;
import com.bd.spectrum.BMDInfo_server.repository.StockTransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryItemRepository itemRepo;
    private final StockTransactionRepository transactionRepo;
    private final InventoryValuationRepository valuationRepo;
    private final StockMovementLogRepository movementRepo;

    public InventoryService(InventoryItemRepository itemRepo,
                            StockTransactionRepository transactionRepo,
                            InventoryValuationRepository valuationRepo,
                            StockMovementLogRepository movementRepo) {
        this.itemRepo = itemRepo;
        this.transactionRepo = transactionRepo;
        this.valuationRepo = valuationRepo;
        this.movementRepo = movementRepo;
    }

    @Transactional
    public BigDecimal calculateFIFO(UUID itemId, String location) {
        List<StockTransaction> transactions = transactionRepo.findAll()
                .stream()
                .filter(tx -> tx.getItem().getId().equals(itemId)
                        && tx.getLocation().equals(location)
                        && "IN".equalsIgnoreCase(tx.getType()))
                .sorted(Comparator.comparing(StockTransaction::getDate)) // oldest first
                .toList();

        InventoryItem item = itemRepo.findById(itemId).orElseThrow();
        int remainingQty = item.getQuantity();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (StockTransaction tx : transactions) {
            if (remainingQty <= 0) break;
            int usedQty = Math.min(remainingQty, tx.getQuantity());
            totalCost = totalCost.add(tx.getPricePerUnit().multiply(BigDecimal.valueOf(usedQty)));
            remainingQty -= usedQty;
        }

        InventoryValuation valuation = new InventoryValuation();
        valuation.setItem(item);
        valuation.setLocation(location);
        valuation.setValuationMethod("FIFO");
        valuation.setTotalValue(totalCost);
        valuation.setLastUpdated(LocalDate.now());
        valuationRepo.save(valuation);

        return totalCost;
    }

    @Transactional
    public BigDecimal calculateLIFO(UUID itemId, String location) {
        List<StockTransaction> transactions = transactionRepo.findAll()
                .stream()
                .filter(tx -> tx.getItem().getId().equals(itemId)
                        && tx.getLocation().equals(location)
                        && "IN".equalsIgnoreCase(tx.getType()))
                .sorted(Comparator.comparing(StockTransaction::getDate).reversed()) // newest first
                .toList();

        InventoryItem item = itemRepo.findById(itemId).orElseThrow();
        int remainingQty = item.getQuantity();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (StockTransaction tx : transactions) {
            if (remainingQty <= 0) break;
            int usedQty = Math.min(remainingQty, tx.getQuantity());
            totalCost = totalCost.add(tx.getPricePerUnit().multiply(BigDecimal.valueOf(usedQty)));
            remainingQty -= usedQty;
        }

        InventoryValuation valuation = new InventoryValuation();
        valuation.setItem(item);
        valuation.setLocation(location);
        valuation.setValuationMethod("LIFO");
        valuation.setTotalValue(totalCost);
        valuation.setLastUpdated(LocalDate.now());
        valuationRepo.save(valuation);

        return totalCost;
    }

    @Transactional
    public BigDecimal calculateWeightedAverage(UUID itemId, String location) {
        List<StockTransaction> transactions = transactionRepo.findAll()
                .stream()
                .filter(tx -> tx.getItem().getId().equals(itemId)
                        && tx.getLocation().equals(location)
                        && "IN".equalsIgnoreCase(tx.getType()))
                .toList();

        InventoryItem item = itemRepo.findById(itemId).orElseThrow();
        int remainingQty = item.getQuantity();

        int totalQty = transactions.stream().mapToInt(StockTransaction::getQuantity).sum();
        if (totalQty == 0) return BigDecimal.ZERO;

        BigDecimal totalCost = transactions.stream()
                .map(tx -> tx.getPricePerUnit().multiply(BigDecimal.valueOf(tx.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weightedAvg = totalCost.divide(BigDecimal.valueOf(totalQty), BigDecimal.ROUND_HALF_UP);
        BigDecimal totalValue = weightedAvg.multiply(BigDecimal.valueOf(remainingQty));

        InventoryValuation valuation = new InventoryValuation();
        valuation.setItem(item);
        valuation.setLocation(location);
        valuation.setValuationMethod("Weighted Average");
        valuation.setTotalValue(totalValue);
        valuation.setLastUpdated(LocalDate.now());
        valuationRepo.save(valuation);

        return totalValue;
    }

    @Transactional
    public void transferStock(UUID itemId, String fromLocation, String toLocation, int quantity) {
        InventoryItem item = itemRepo.findById(itemId).orElseThrow();

        if (item.getLocation().equals(fromLocation)) {
            int currentQty = item.getQuantity();
            if (currentQty < quantity) throw new IllegalArgumentException("Not enough stock to transfer");
            item.setQuantity(currentQty - quantity);
            itemRepo.save(item);
        }

        InventoryItem destItem = itemRepo.findAll().stream()
                .filter(i -> i.getName().equals(item.getName())
                        && i.getLocation().equals(toLocation))
                .findFirst()
                .orElseGet(() -> {
                    InventoryItem newItem = new InventoryItem();
                    newItem.setName(item.getName());
                    newItem.setCategory(item.getCategory());
                    newItem.setSku(item.getSku());
                    newItem.setLocation(toLocation);
                    newItem.setUnitPrice(item.getUnitPrice());
                    newItem.setQuantity(0);
                    return newItem;
                });

        destItem.setQuantity(destItem.getQuantity() + quantity);
        itemRepo.save(destItem);

        StockMovementLog log = new StockMovementLog();
        log.setItem(item);
        log.setFromLocation(fromLocation);
        log.setToLocation(toLocation);
        log.setQuantity(quantity);
        log.setDate(LocalDate.now());
        movementRepo.save(log);
    }
}
