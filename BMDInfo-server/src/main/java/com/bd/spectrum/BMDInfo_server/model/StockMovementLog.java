package com.bd.spectrum.BMDInfo_server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity representing a stock movement log (audit trail).
 * Captures transfer or adjustment activities between locations
 * for tracking and auditing purposes.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private InventoryItem item;

    private String fromLocation;
    private String toLocation;
    private Integer quantity;
    private LocalDate date;
    private String action;
}
