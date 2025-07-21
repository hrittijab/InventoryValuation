package com.bd.spectrum.BMDInfo_server.repository;

import com.bd.spectrum.BMDInfo_server.model.InventoryValuation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InventoryValuationRepository extends JpaRepository<InventoryValuation, UUID> {}
