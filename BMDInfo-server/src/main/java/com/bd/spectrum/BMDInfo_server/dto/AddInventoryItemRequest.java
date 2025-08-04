package com.bd.spectrum.BMDInfo_server.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddInventoryItemRequest {
    private String name;
    private String category;
    private String sku;
    private String location;
    private BigDecimal unitPrice;
    private int quantity;
}
