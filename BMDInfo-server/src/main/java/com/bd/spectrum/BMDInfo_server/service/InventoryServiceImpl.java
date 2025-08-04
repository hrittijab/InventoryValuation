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
import java.util.stream.Collectors;

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

    private List<StockTransaction> getTransactionsBySKUAndLocation(String sku, String location, boolean onlyInTransactions) {
        List<UUID> matchingItemIds = itemRepo.findAll().stream()
                .filter(i -> i.getSku().equals(sku))
                .map(InventoryItem::getId)
                .collect(Collectors.toList());

        return transactionRepo.findAll().stream()
                .filter(tx -> matchingItemIds.contains(tx.getItem().getId())
                        && tx.getLocation().equals(location)
                        && (!onlyInTransactions || "IN".equalsIgnoreCase(tx.getType())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BigDecimal calculateFIFO(UUID itemId, String location) {
        InventoryItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

        List<StockTransaction> transactions = getTransactionsBySKUAndLocation(item.getSku(), location, false);

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

        InventoryValuation valuation = valuationRepo
                .findByItemIdAndLocationAndValuationMethod(itemId, location, "FIFO")
                .orElseGet(() -> {
                    InventoryValuation newVal = new InventoryValuation();
                    newVal.setItem(item);
                    newVal.setLocation(location);
                    newVal.setValuationMethod("FIFO");
                    return newVal;
                });

        valuation.setTotalValue(totalCost);
        valuation.setLastUpdated(LocalDate.now());
        valuationRepo.save(valuation);

        return totalCost;
    }

    @Override
    @Transactional
    public BigDecimal calculateLIFO(UUID itemId, String location) {
        InventoryItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

        List<StockTransaction> transactions = getTransactionsBySKUAndLocation(item.getSku(), location, true)
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

        InventoryValuation valuation = valuationRepo
                .findByItemIdAndLocationAndValuationMethod(itemId, location, "LIFO")
                .orElseGet(() -> {
                    InventoryValuation newVal = new InventoryValuation();
                    newVal.setItem(item);
                    newVal.setLocation(location);
                    newVal.setValuationMethod("LIFO");
                    return newVal;
                });

        valuation.setTotalValue(totalCost);
        valuation.setLastUpdated(LocalDate.now());
        valuationRepo.save(valuation);

        return totalCost;
    }

    @Override
    @Transactional
    public BigDecimal calculateWeightedAverage(UUID itemId, String location) {
        InventoryItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

        String sku = item.getSku();
        List<UUID> relatedItemIds = itemRepo.findAll().stream()
                .filter(i -> i.getSku().equals(sku))
                .map(InventoryItem::getId)
                .collect(Collectors.toList());

        List<StockTransaction> transactions = transactionRepo.findAll().stream()
                .filter(tx -> relatedItemIds.contains(tx.getItem().getId())
                        && tx.getLocation().equals(location)
                        && "IN".equalsIgnoreCase(tx.getType()))
                .collect(Collectors.toList());

        if (transactions.isEmpty()) {
            throw new RuntimeException("No stock transactions found for this item at " + location);
        }

        int totalInQty = transactions.stream().mapToInt(StockTransaction::getQuantity).sum();

        List<StockTransaction> allTransactions = transactionRepo.findAll().stream()
                .filter(tx -> relatedItemIds.contains(tx.getItem().getId())
                        && tx.getLocation().equals(location))
                .collect(Collectors.toList());

        int totalOutQty = allTransactions.stream()
                .filter(tx -> "OUT".equalsIgnoreCase(tx.getType()))
                .mapToInt(StockTransaction::getQuantity)
                .sum();

        int remainingQty = totalInQty - totalOutQty;

        BigDecimal totalCost = transactions.stream()
                .map(tx -> tx.getPricePerUnit().multiply(BigDecimal.valueOf(tx.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weightedAvg = totalCost.divide(BigDecimal.valueOf(totalInQty), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal totalValue = weightedAvg.multiply(BigDecimal.valueOf(remainingQty));

        InventoryValuation valuation = valuationRepo
                .findByItemIdAndLocationAndValuationMethod(itemId, location, "Weighted Average")
                .orElseGet(() -> {
                    InventoryValuation newVal = new InventoryValuation();
                    newVal.setItem(item);
                    newVal.setLocation(location);
                    newVal.setValuationMethod("Weighted Average");
                    return newVal;
                });

        valuation.setTotalValue(totalValue);
        valuation.setLastUpdated(LocalDate.now());
        valuationRepo.save(valuation);

        return totalValue;
    }


@Override
@Transactional
public void transferStock(UUID itemId, String fromLocation, String toLocation, int quantity) {
    InventoryItem fromItem = itemRepo.findAll().stream()
            .filter(i -> i.getId().equals(itemId) && i.getLocation().equals(fromLocation))
            .findFirst()
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

    InventoryItem toItem = itemRepo.findAll().stream()
            .filter(i -> i.getName().equals(fromItem.getName())
                    && i.getLocation().equals(toLocation))
            .findFirst()
            .orElseGet(() -> {
                InventoryItem newItem = new InventoryItem();
                newItem.setName(fromItem.getName());
                newItem.setCategory(fromItem.getCategory());
                newItem.setSku(fromItem.getSku());
                newItem.setLocation(toLocation);
                newItem.setUnitPrice(fromItem.getUnitPrice());
                newItem.setQuantity(0);
                return newItem;
            });

    toItem.setQuantity(toItem.getQuantity() + quantity);
    toItem = itemRepo.save(toItem); 

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



    @Override
    public byte[] exportValuationAsCSV() throws IOException {
        List<InventoryValuation> valuations = valuationRepo.findAll();

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
    public byte[] exportValuationAsPDF() throws IOException {
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

        for (InventoryValuation val : valuationRepo.findAll()) {
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

    // Step 4: Save the new transaction
    transactionRepo.save(newTransaction);
}





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




}
