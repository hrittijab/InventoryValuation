package com.bd.spectrum.BMDInfo_server.service;

import com.bd.spectrum.BMDInfo_server.dto.AddInventoryItemRequest;
import com.bd.spectrum.BMDInfo_server.model.InventoryItem;
import com.bd.spectrum.BMDInfo_server.model.StockMovementLog;
import com.bd.spectrum.BMDInfo_server.model.StockTransaction;
import com.bd.spectrum.BMDInfo_server.repository.InventoryItemRepository;
import com.bd.spectrum.BMDInfo_server.repository.InventoryValuationRepository;
import com.bd.spectrum.BMDInfo_server.repository.StockMovementLogRepository;
import com.bd.spectrum.BMDInfo_server.repository.StockTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryServiceImplTest {

    @Mock
    private InventoryItemRepository itemRepo;
    @Mock
    private StockTransactionRepository transactionRepo;
    @Mock
    private InventoryValuationRepository valuationRepo;
    @Mock
    private StockMovementLogRepository movementRepo;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private InventoryItem createItem(UUID id, String name, String sku, String location, int qty, BigDecimal price) {
        return new InventoryItem(id, name, "Electronics", sku, location, price, qty, null);
    }

    private StockTransaction createTransaction(InventoryItem item, int qty, BigDecimal price, String type, int daysAgo) {
        return new StockTransaction(
                UUID.randomUUID(),
                item,
                qty,
                price,
                LocalDateTime.now().minusDays(daysAgo),
                type,
                item.getLocation()
        );
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ Already passing
    @Test
    void testSaveItem_withQuantity_shouldCreateTransaction() {
        AddInventoryItemRequest request = new AddInventoryItemRequest("Laptop", "Electronics", "LAP-123", "Dhaka", BigDecimal.valueOf(1000), 10);

        when(itemRepo.save(any(InventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepo.save(any(StockTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryItem saved = inventoryService.saveItem(request);

        assertEquals("Laptop", saved.getName());
        verify(transactionRepo, times(1)).save(any(StockTransaction.class));
    }

    // ✅ Already passing
    @Test
    void testSaveItem_withZeroQuantity_shouldNotCreateTransaction() {
        AddInventoryItemRequest request = new AddInventoryItemRequest("Laptop", "Electronics", "LAP-123", "Dhaka", BigDecimal.valueOf(1000), 0);

        when(itemRepo.save(any(InventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryItem saved = inventoryService.saveItem(request);

        assertEquals(0, saved.getQuantity());
        verify(transactionRepo, never()).save(any(StockTransaction.class));
    }

    // ✅ Already passing
    @Test
    void testAddStock_itemNotFound_shouldThrow() {
        UUID itemId = UUID.randomUUID();
        when(itemRepo.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                inventoryService.addStock(itemId, 5, BigDecimal.valueOf(1000), "Dhaka"));
    }

    // 🔧 FIXED
    @Test
    void testCalculateFIFO() {
        UUID itemId = UUID.randomUUID();
        InventoryItem item = createItem(itemId, "Laptop", "LAP-123", "Chittagong", 10, BigDecimal.valueOf(1000));

        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));

        List<StockTransaction> transactions = List.of(
                createTransaction(item, 5, BigDecimal.valueOf(1000), "IN", 5),
                createTransaction(item, 5, BigDecimal.valueOf(1200), "IN", 2)
        );

        when(transactionRepo.findByItemSkuAndLocation("LAP-123", "Chittagong")).thenReturn(transactions);

        BigDecimal value = inventoryService.calculateFIFO(itemId, "Chittagong");
        assertEquals(BigDecimal.valueOf(1000 * 5 + 1200 * 5), value);
    }

    // 🔧 FIXED
    @Test
    void testCalculateLIFO() {
        UUID itemId = UUID.randomUUID();
        InventoryItem item = createItem(itemId, "Laptop", "LAP-123", "Chittagong", 10, BigDecimal.valueOf(1200));

        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));

        List<StockTransaction> transactions = List.of(
                createTransaction(item, 5, BigDecimal.valueOf(1000), "IN", 5),
                createTransaction(item, 5, BigDecimal.valueOf(1200), "IN", 1)
        );

        when(transactionRepo.findByItemSkuAndLocationAndType("LAP-123", "Chittagong", "IN"))
                .thenReturn(transactions);

        BigDecimal value = inventoryService.calculateLIFO(itemId, "Chittagong");
        assertEquals(BigDecimal.valueOf(1200 * 5 + 1000 * 5), value);
    }

    @Test
void testCalculateWeightedAverage() {
    UUID itemId = UUID.randomUUID();
    InventoryItem item = createItem(itemId, "Laptop", "LAP-123", "Chittagong", 10, BigDecimal.ZERO);

    when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));

    List<StockTransaction> inTransactions = List.of(
            createTransaction(item, 6, BigDecimal.valueOf(1000), "IN", 5),
            createTransaction(item, 4, BigDecimal.valueOf(1200), "IN", 2)
    );

    when(transactionRepo.findByItemSkuAndLocationAndType("LAP-123", "Chittagong", "IN"))
            .thenReturn(inTransactions);
    when(transactionRepo.findByItemSkuAndLocation("LAP-123", "Chittagong"))
            .thenReturn(inTransactions);

    BigDecimal value = inventoryService.calculateWeightedAverage(itemId, "Chittagong");
    BigDecimal expected = BigDecimal.valueOf((1000 * 6 + 1200 * 4)); // 10800

    // ✅ Scale-safe assertion
    assertEquals(0, expected.compareTo(value));
}


    // 🔧 FIXED
    @Test
    void testTransferStock_createsDestinationItemIfNotExists() {
        UUID itemId = UUID.randomUUID();
        InventoryItem sourceItem = createItem(itemId, "Laptop", "LAP-123", "Dhaka", 10, BigDecimal.valueOf(1000));

        when(itemRepo.findByIdAndLocation(itemId, "Dhaka")).thenReturn(Optional.of(sourceItem));
        when(itemRepo.findByNameAndLocation("Laptop", "Chittagong")).thenReturn(Optional.empty());
        when(itemRepo.save(any(InventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        inventoryService.transferStock(itemId, "Dhaka", "Chittagong", 5);

        verify(transactionRepo, times(2)).save(any(StockTransaction.class)); // OUT + IN
        verify(movementRepo, times(1)).save(any(StockMovementLog.class));
    }

    // 🔧 FIXED
    @Test
    void testTransferStock() {
        UUID itemId = UUID.randomUUID();
        InventoryItem sourceItem = createItem(itemId, "Laptop", "LAP-123", "Dhaka", 10, BigDecimal.valueOf(1000));
        InventoryItem destItem = createItem(UUID.randomUUID(), "Laptop", "LAP-123", "Chittagong", 5, BigDecimal.valueOf(1000));

        when(itemRepo.findByIdAndLocation(itemId, "Dhaka")).thenReturn(Optional.of(sourceItem));
        when(itemRepo.findByNameAndLocation("Laptop", "Chittagong")).thenReturn(Optional.of(destItem));
        when(itemRepo.save(any(InventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        inventoryService.transferStock(itemId, "Dhaka", "Chittagong", 5);

        verify(transactionRepo, times(2)).save(any(StockTransaction.class)); // OUT + IN
        verify(movementRepo, times(1)).save(any(StockMovementLog.class));
    }
}
