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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository itemRepo;
    private final StockTransactionRepository transactionRepo;
    private final InventoryValuationRepository valuationRepo;
    private final StockMovementLogRepository movementRepo;

    public InventoryServiceImpl(InventoryItemRepository itemRepo,
                                StockTransactionRepository transactionRepo,
                                InventoryValuationRepository valuationRepo,
                                StockMovementLogRepository movementRepo) {
        this.itemRepo = itemRepo;
        this.transactionRepo = transactionRepo;
        this.valuationRepo = valuationRepo;
        this.movementRepo = movementRepo;
    }

    @Override
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

        InventoryValuation valuation = valuationRepo.findAll().stream()
        .filter(v -> v.getItem().getId().equals(itemId)
                && v.getLocation().equals(location)
                && v.getValuationMethod().equalsIgnoreCase("FIFO"))
        .findFirst()
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

        InventoryValuation valuation = valuationRepo.findAll().stream()
        .filter(v -> v.getItem().getId().equals(itemId)
                && v.getLocation().equals(location)
                && v.getValuationMethod().equalsIgnoreCase("LIFO"))
        .findFirst()
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

        InventoryValuation valuation = valuationRepo.findAll().stream()
        .filter(v -> v.getItem().getId().equals(itemId)
                && v.getLocation().equals(location)
                && v.getValuationMethod().equalsIgnoreCase("Weighted Average"))
        .findFirst()
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

        // Title without TextAlignment
        Paragraph title = new Paragraph("Inventory Valuation Report")
                .setBold()
                .setFontSize(16);
        doc.add(title);

        // Generation date
        doc.add(new Paragraph("Generated on: " + LocalDate.now()).setFontSize(10));

        // Table for data
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

}
