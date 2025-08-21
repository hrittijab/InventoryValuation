package com.bd.spectrum.BMDInfo_server.repository;

import com.bd.spectrum.BMDInfo_server.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.Optional;


public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {
    Optional<InventoryItem> findByIdAndLocation(UUID id, String location);
    Optional<InventoryItem> findByNameAndLocation(String name, String location);
    Optional<InventoryItem> findBySkuAndLocation(String sku, String location);
}
