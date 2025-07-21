package com.bd.spectrum.BMDInfo_server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryValuation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private InventoryItem item;

    private String valuationMethod; 
    private BigDecimal totalValue;
    private String location;
    private LocalDate lastUpdated;
}
