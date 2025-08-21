package com.bd.spectrum.BMDInfo_server.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new inventory item.
 * Used when adding items through InventoryController (/add-item).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddInventoryItemRequest {

    private String name;

    private String category;

    /** Unique SKU (Stock Keeping Unit) identifier */
    private String sku;

    private String location;

    private BigDecimal unitPrice;

    private int quantity;
}
