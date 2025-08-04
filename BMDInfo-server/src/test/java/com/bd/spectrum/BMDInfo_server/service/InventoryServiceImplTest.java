package com.bd.spectrum.BMDInfo_server.service;

import com.bd.spectrum.BMDInfo_server.dto.AddInventoryItemRequest;
import com.bd.spectrum.BMDInfo_server.model.InventoryItem;
import com.bd.spectrum.BMDInfo_server.model.StockTransaction;
import com.bd.spectrum.BMDInfo_server.repository.InventoryItemRepository;
import com.bd.spectrum.BMDInfo_server.repository.InventoryValuationRepository;
import com.bd.spectrum.BMDInfo_server.repository.StockMovementLogRepository;
import com.bd.spectrum.BMDInfo_server.repository.StockTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock private InventoryItemRepository itemRepo;
    @Mock private StockTransactionRepository transactionRepo;
    @Mock private InventoryValuationRepository valuationRepo;
    @Mock private StockMovementLogRepository movementRepo;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private InventoryItem item;
    private UUID itemId;
    private String location;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        location = "Chittagong";

        item = new InventoryItem();
        item.setId(itemId);
        item.setSku("TEST-001");
        item.setQuantity(7); 
    }

    private StockTransaction createTx(InventoryItem item, String type, String location, int qty, BigDecimal price, int daysAgo) {
        StockTransaction tx = new StockTransaction();
        tx.setItem(item);
        tx.setType(type);
        tx.setLocation(location);
        tx.setQuantity(qty);
        tx.setPricePerUnit(price);
        tx.setDate(LocalDateTime.now().minusDays(daysAgo));
        return tx;
    }

    @Test
    void testCalculateFIFO() {
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepo.findAll()).thenReturn(List.of(item));

        List<StockTransaction> txs = List.of(
                createTx(item, "IN", location, 3, new BigDecimal("1000"), 2),
                createTx(item, "IN", location, 3, new BigDecimal("1000"), 1)
        );

        when(transactionRepo.findAll()).thenReturn(txs);
        when(valuationRepo.findByItemIdAndLocationAndValuationMethod(itemId, location, "FIFO"))
                .thenReturn(Optional.empty());

        BigDecimal result = inventoryService.calculateFIFO(itemId, location);
        assertEquals(new BigDecimal("6000.00"), result.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void testCalculateLIFO() {
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepo.findAll()).thenReturn(List.of(item));

        List<StockTransaction> txs = List.of(
                createTx(item, "IN", location, 3, new BigDecimal("1000"), 2),
                createTx(item, "IN", location, 4, new BigDecimal("1000"), 1)
        );

        when(transactionRepo.findAll()).thenReturn(txs);
        when(valuationRepo.findByItemIdAndLocationAndValuationMethod(itemId, location, "LIFO"))
                .thenReturn(Optional.empty());

        BigDecimal result = inventoryService.calculateLIFO(itemId, location);
        assertEquals(new BigDecimal("7000.00"), result.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void testCalculateWeightedAverage() {
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepo.findAll()).thenReturn(List.of(item));

        List<StockTransaction> inTxs = List.of(
                createTx(item, "IN", location, 4, new BigDecimal("1000"), 2),
                createTx(item, "IN", location, 2, new BigDecimal("2000"), 1)
        );

        List<StockTransaction> allTxs = new ArrayList<>(inTxs);

        when(transactionRepo.findAll()).thenReturn(allTxs);
        when(valuationRepo.findByItemIdAndLocationAndValuationMethod(itemId, location, "Weighted Average"))
                .thenReturn(Optional.empty());

        BigDecimal result = inventoryService.calculateWeightedAverage(itemId, location);
        assertEquals(new BigDecimal("7999.98"), result.setScale(2, RoundingMode.HALF_UP)); // (4*1000 + 2*2000) = 8000, but rounding
    }

        @Test
    void testTransferStock() {
        InventoryItem fromItem = new InventoryItem();
        fromItem.setId(itemId);
        fromItem.setName("Test Item");
        fromItem.setCategory("Category");
        fromItem.setSku("SKU001");
        fromItem.setLocation(location);
        fromItem.setUnitPrice(new BigDecimal("1500"));
        fromItem.setQuantity(10);

        InventoryItem toItem = new InventoryItem();
        toItem.setId(UUID.randomUUID());
        toItem.setName("Test Item");
        toItem.setCategory("Category");
        toItem.setSku("SKU001");
        toItem.setLocation("Dhaka");
        toItem.setUnitPrice(new BigDecimal("1500"));
        toItem.setQuantity(5);

        when(itemRepo.findAll()).thenReturn(List.of(fromItem, toItem));

        when(itemRepo.save(fromItem)).thenReturn(fromItem);
        when(itemRepo.save(toItem)).thenReturn(toItem);
        when(transactionRepo.save(any())).thenReturn(null); 
        when(movementRepo.save(any())).thenReturn(null);

        inventoryService.transferStock(itemId, "Chittagong", "Dhaka", 3);

        assertEquals(7, fromItem.getQuantity()); // 10 - 3
        assertEquals(8, toItem.getQuantity());   // 5 + 3
    }
    @Test
    void testTransferStock_createsDestinationItemIfNotExists() {
        InventoryItem fromItem = new InventoryItem();
        fromItem.setId(itemId);
        fromItem.setName("Test");
        fromItem.setCategory("Cat");
        fromItem.setSku("SKU001");
        fromItem.setLocation("Chittagong");
        fromItem.setUnitPrice(new BigDecimal("1000"));
        fromItem.setQuantity(10);

        when(itemRepo.findAll()).thenReturn(List.of(fromItem));
        when(itemRepo.save(any())).thenAnswer(inv -> inv.getArguments()[0]);
        when(transactionRepo.save(any())).thenReturn(null);
        when(movementRepo.save(any())).thenReturn(null);

        inventoryService.transferStock(itemId, "Chittagong", "Dhaka", 4);

        assertEquals(6, fromItem.getQuantity());
    }
    @Test
    void testSaveItem_withQuantity_shouldCreateTransaction() {
        AddInventoryItemRequest req = new AddInventoryItemRequest();
        req.setName("Laptop");
        req.setCategory("Electronics");
        req.setSku("SKU123");
        req.setLocation("Dhaka");
        req.setUnitPrice(new BigDecimal("500"));
        req.setQuantity(5);

        InventoryItem savedItem = new InventoryItem();
        savedItem.setId(UUID.randomUUID());
        savedItem.setName("Laptop");
        savedItem.setQuantity(5);

        when(itemRepo.save(any())).thenReturn(savedItem);
        when(transactionRepo.save(any())).thenReturn(null);

        InventoryItem result = inventoryService.saveItem(req);

        assertEquals("Laptop", result.getName());
        assertEquals(5, result.getQuantity());
        verify(transactionRepo).save(any(StockTransaction.class));
    }

    @Test
    void testSaveItem_withZeroQuantity_shouldNotCreateTransaction() {
        AddInventoryItemRequest req = new AddInventoryItemRequest();
        req.setName("Mouse");
        req.setCategory("Electronics");
        req.setSku("SKU456");
        req.setLocation("Chittagong");
        req.setUnitPrice(new BigDecimal("100"));
        req.setQuantity(0);

        InventoryItem savedItem = new InventoryItem();
        savedItem.setId(UUID.randomUUID());
        savedItem.setName("Mouse");
        savedItem.setQuantity(0);

        when(itemRepo.save(any())).thenReturn(savedItem);

        InventoryItem result = inventoryService.saveItem(req);

        assertEquals("Mouse", result.getName());
        assertEquals(0, result.getQuantity());
        verify(transactionRepo, never()).save(any()); 
    }



    @Test
    void testAddStock_itemNotFound_shouldThrow() {
        UUID itemId = UUID.randomUUID();

        when(itemRepo.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            inventoryService.addStock(itemId, 5, new BigDecimal("200"), "Dhaka"));
    }

}
