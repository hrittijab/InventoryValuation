package com.bd.spectrum.BMDInfo_server.service;

import com.bd.spectrum.BMDInfo_server.dto.AddInventoryItemRequest;
import com.bd.spectrum.BMDInfo_server.model.InventoryItem;
import com.bd.spectrum.BMDInfo_server.model.InventoryValuation;
import com.bd.spectrum.BMDInfo_server.model.StockMovementLog;
import com.bd.spectrum.BMDInfo_server.model.StockTransaction;
import com.bd.spectrum.BMDInfo_server.repository.InventoryItemRepository;
import com.bd.spectrum.BMDInfo_server.repository.InventoryValuationRepository;
import com.bd.spectrum.BMDInfo_server.repository.StockMovementLogRepository;
import com.bd.spectrum.BMDInfo_server.repository.StockTransactionRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.geom.PageSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
/**
 * Provides business logic for inventory management including:
 * - Stock transfers, additions, and new item creation
 * - Inventory valuation (FIFO, LIFO, Weighted Average)
 * - Export of valuation reports (CSV / PDF)
 * - Audit trail through movement logs and transactions
 * - Scheduled automatic daily valuation snapshot
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository itemRepo;
    private final StockTransactionRepository transactionRepo;
    private final InventoryValuationRepository valuationRepo;
    private final StockMovementLogRepository movementRepo;

    @PersistenceContext
    private EntityManager entityManager;

    public InventoryServiceImpl(InventoryItemRepository itemRepo,
                                StockTransactionRepository transactionRepo,
                                InventoryValuationRepository valuationRepo,
                                StockMovementLogRepository movementRepo) {
        this.itemRepo = itemRepo;
        this.transactionRepo = transactionRepo;
        this.valuationRepo = valuationRepo;
        this.movementRepo = movementRepo;
    }
    /** Saves valuation snap for today if it doesnt exist already for that date */
    private void saveValuationSnapshot(InventoryItem item, String location, String method, BigDecimal value) {
        LocalDate today = LocalDate.now();

        boolean exists = valuationRepo.existsByItemIdAndLocationAndValuationMethodAndDate(
                item.getId(), location, method, today
        );

        if (exists) {
                return;
        }

        InventoryValuation valuation = new InventoryValuation();
        valuation.setItem(item);
        valuation.setLocation(location);
        valuation.setValuationMethod(method);
        valuation.setTotalValue(value);
        valuation.setLastUpdated(LocalDateTime.now());
        valuationRepo.save(valuation);
        }



    @Override
    @Transactional
    public BigDecimal calculateFIFO(UUID itemId, String location) {
        InventoryItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

        List<StockTransaction> transactions = transactionRepo.findByItemSkuAndLocation(item.getSku(), location);

        int totalIn = transactions.stream()
                .filter(tx -> "IN".equalsIgnoreCase(tx.getType()))
                .mapToInt(StockTransaction::getQuantity)
                .sum();

        int totalOut = transactions.stream()
                .filter(tx -> "OUT".equalsIgnoreCase(tx.getType()))
                .mapToInt(StockTransaction::getQuantity)
                .sum();

        int remainingQty = totalIn - totalOut;
        if (remainingQty <= 0) {
            throw new RuntimeException("No available quantity for item at " + location);
        }

        List<StockTransaction> fifoTransactions = transactions.stream()
                .filter(tx -> "IN".equalsIgnoreCase(tx.getType()))
                .sorted(Comparator.comparing(StockTransaction::getDate))
                .toList();

        BigDecimal totalCost = BigDecimal.ZERO;
        for (StockTransaction tx : fifoTransactions) {
            if (remainingQty <= 0) break;
            int usedQty = Math.min(remainingQty, tx.getQuantity());
            totalCost = totalCost.add(tx.getPricePerUnit().multiply(BigDecimal.valueOf(usedQty)));
            remainingQty -= usedQty;
        }

        saveValuationSnapshot(item, location, "FIFO", totalCost);
        return totalCost;
    }

    @Override
    @Transactional
    public BigDecimal calculateLIFO(UUID itemId, String location) {
        InventoryItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

        List<StockTransaction> transactions = transactionRepo
                .findByItemSkuAndLocationAndType(item.getSku(), location, "IN")
                .stream()
                .sorted(Comparator.comparing(StockTransaction::getDate).reversed())
                .toList();

        if (transactions.isEmpty()) {
            throw new RuntimeException("No stock transactions found for this item at " + location);
        }

        int remainingQty = item.getQuantity();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (StockTransaction tx : transactions) {
            if (remainingQty <= 0) break;
            int usedQty = Math.min(remainingQty, tx.getQuantity());
            totalCost = totalCost.add(tx.getPricePerUnit().multiply(BigDecimal.valueOf(usedQty)));
            remainingQty -= usedQty;
        }

        saveValuationSnapshot(item, location, "LIFO", totalCost);
        return totalCost;
    }

    @Override
    @Transactional
    public BigDecimal calculateWeightedAverage(UUID itemId, String location) {
        InventoryItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

        List<StockTransaction> inTransactions = transactionRepo
                .findByItemSkuAndLocationAndType(item.getSku(), location, "IN");

        if (inTransactions.isEmpty()) {
            throw new RuntimeException("No stock transactions found for this item at " + location);
        }

        int totalInQty = inTransactions.stream().mapToInt(StockTransaction::getQuantity).sum();

        List<StockTransaction> allTransactions = transactionRepo.findByItemSkuAndLocation(item.getSku(), location);

        int totalOutQty = allTransactions.stream()
                .filter(tx -> "OUT".equalsIgnoreCase(tx.getType()))
                .mapToInt(StockTransaction::getQuantity)
                .sum();

        int remainingQty = totalInQty - totalOutQty;

        BigDecimal totalCost = inTransactions.stream()
                .map(tx -> tx.getPricePerUnit().multiply(BigDecimal.valueOf(tx.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weightedAvg = totalCost.divide(BigDecimal.valueOf(totalInQty), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal totalValue = weightedAvg.multiply(BigDecimal.valueOf(remainingQty));

        saveValuationSnapshot(item, location, "Weighted Average", totalValue);
        return totalValue;
    }

    /** Transfer stock between locations and log the movement. */

    @Override
    @Transactional
    public void transferStock(UUID itemId, String fromLocation, String toLocation, int quantity) {
        InventoryItem fromItem = itemRepo.findByIdAndLocation(itemId, fromLocation)
                .orElseThrow(() -> new RuntimeException("Item not found at source location."));

        if (fromItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock to transfer.");
        }

        fromItem.setQuantity(fromItem.getQuantity() - quantity);
        itemRepo.save(fromItem);

        StockTransaction outTx = new StockTransaction();
        outTx.setItem(fromItem);
        outTx.setLocation(fromLocation);
        outTx.setQuantity(quantity);
        outTx.setPricePerUnit(fromItem.getUnitPrice());
        outTx.setDate(LocalDateTime.now());
        outTx.setType("OUT");
        transactionRepo.save(outTx);

        InventoryItem toItem = itemRepo.findByNameAndLocation(fromItem.getName(), toLocation)
                .orElseGet(() -> {
                    InventoryItem newItem = new InventoryItem();
                    newItem.setName(fromItem.getName());
                    newItem.setCategory(fromItem.getCategory());
                    newItem.setSku(fromItem.getSku());
                    newItem.setLocation(toLocation);
                    newItem.setUnitPrice(fromItem.getUnitPrice());
                    newItem.setQuantity(0);
                    return itemRepo.save(newItem);
                });

        toItem.setQuantity(toItem.getQuantity() + quantity);
        itemRepo.save(toItem);

        StockTransaction inTx = new StockTransaction();
        inTx.setItem(toItem);
        inTx.setLocation(toLocation);
        inTx.setQuantity(quantity);
        inTx.setPricePerUnit(fromItem.getUnitPrice());
        inTx.setDate(LocalDateTime.now());
        inTx.setType("IN");
        transactionRepo.save(inTx);

        StockMovementLog log = new StockMovementLog();
        log.setItem(toItem);
        log.setFromLocation(fromLocation);
        log.setToLocation(toLocation);
        log.setQuantity(quantity);
        log.setDate(LocalDate.now());
        movementRepo.save(log);
    }

    @Override
    public List<InventoryItem> getAllItems() {
        return itemRepo.findAll();
    }

    @Override
    public List<StockMovementLog> getAllMovements() {
        return movementRepo.findAll();
    }
    
    /** Export csv and pdf files for the valuations */
    @Override
        public byte[] exportValuationAsCSV(UUID itemId, String location) throws IOException {
        List<InventoryValuation> valuations;

        if (itemId != null && location != null) {
                valuations = valuationRepo.findByItemIdAndLocationOrderByLastUpdatedAsc(itemId, location);
        } else if (itemId != null) {
                valuations = valuationRepo.findByItemIdOrderByLastUpdatedAsc(itemId);
        } else {
                valuations = valuationRepo.findAll();
        }

        StringWriter writer = new StringWriter();
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Item", "Location", "Method", "Value", "Date"));

        for (InventoryValuation val : valuations) {
                csvPrinter.printRecord(
                        val.getItem().getName(),
                        val.getLocation(),
                        val.getValuationMethod(),
                        val.getTotalValue(),
                        val.getLastUpdated()
                );
        }

        csvPrinter.flush();
        return writer.toString().getBytes();
        }

        @Override
        public byte[] exportValuationAsPDF(UUID itemId, String location) throws IOException {
        List<InventoryValuation> valuations;

        if (itemId != null && location != null) {
                valuations = valuationRepo.findByItemIdAndLocationOrderByLastUpdatedAsc(itemId, location);
        } else if (itemId != null) {
                valuations = valuationRepo.findByItemIdOrderByLastUpdatedAsc(itemId);
        } else {
                valuations = valuationRepo.findAll();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);

        Paragraph title = new Paragraph("Inventory Valuation Report")
                .setBold()
                .setFontSize(16);
        doc.add(title);

        doc.add(new Paragraph("Generated on: " + LocalDate.now()).setFontSize(10));

        Table table = new Table(5);
        table.addHeaderCell("Item");
        table.addHeaderCell("Location");
        table.addHeaderCell("Method");
        table.addHeaderCell("Value");
        table.addHeaderCell("Date");

        for (InventoryValuation val : valuations) {
                table.addCell(val.getItem().getName());
                table.addCell(val.getLocation());
                table.addCell(val.getValuationMethod());
                table.addCell(val.getTotalValue().toString());
                table.addCell(val.getLastUpdated().toString());
        }

        doc.add(table);
        doc.close();
        return baos.toByteArray();
        }

    /** Add stock to an existing item (creates a transaction). */
    @Override
    @Transactional
    public void addStock(UUID itemId, int quantity, BigDecimal pricePerUnit, String location) {
        InventoryItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setQuantity(item.getQuantity() + quantity);
        itemRepo.save(item);

        entityManager.flush();
        entityManager.clear();

        StockTransaction newTransaction = new StockTransaction();
        newTransaction.setItem(item);
        newTransaction.setQuantity(quantity);
        newTransaction.setPricePerUnit(pricePerUnit);
        newTransaction.setLocation(location);
        newTransaction.setType("IN");
        newTransaction.setDate(LocalDateTime.now());

        transactionRepo.save(newTransaction);
    }
    
    /** Save a new item and record an initial transaction if stock > 0. */
    @Override
    @Transactional
    public InventoryItem saveItem(AddInventoryItemRequest request) {
        InventoryItem item = new InventoryItem();
        item.setName(request.getName());
        item.setCategory(request.getCategory());
        item.setSku(request.getSku());
        item.setLocation(request.getLocation());
        item.setUnitPrice(request.getUnitPrice());

        int initialQty = request.getQuantity();
        item.setQuantity(initialQty);

        InventoryItem savedItem = itemRepo.save(item);

        if (initialQty > 0) {
            StockTransaction tx = new StockTransaction();
            tx.setItem(savedItem);
            tx.setLocation(request.getLocation());
            tx.setQuantity(initialQty);
            tx.setPricePerUnit(request.getUnitPrice());
            tx.setDate(LocalDateTime.now());
            tx.setType("IN");

            transactionRepo.save(tx);
        }

        return savedItem;
    }

    @Override
    public List<InventoryValuation> getAllValuations() {
        return valuationRepo.findAll();
    }

    @Override
    public List<InventoryValuation> getValuationHistory(UUID itemId, String location) {
        if (location != null && !location.isBlank()) {
            return valuationRepo.findByItemIdAndLocationOrderByLastUpdatedAsc(itemId, location);
        }
        return valuationRepo.findByItemIdOrderByLastUpdatedAsc(itemId);
    }

    // Run automatically
    @Scheduled(cron = "0 0 0 * * ?") 
    @Transactional
    public void scheduledValuationUpdate() {
        System.out.println("Scheduled valuation update executed at " + LocalDateTime.now());
        List<InventoryItem> items = itemRepo.findAll();
        for (InventoryItem item : items) {
            try {
                calculateFIFO(item.getId(), item.getLocation());
                calculateLIFO(item.getId(), item.getLocation());
                calculateWeightedAverage(item.getId(), item.getLocation());
            } catch (Exception e) {
                System.err.println("Valuation failed for item " + item.getId() + ": " + e.getMessage());
            }
        }
    }
}
