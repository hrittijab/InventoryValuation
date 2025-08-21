package com.bd.spectrum.BMDInfo_server.repository;

import com.bd.spectrum.BMDInfo_server.model.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {
    List<StockTransaction> findByItemSkuAndLocation(String sku, String location);
    List<StockTransaction> findByItemSkuAndLocationAndType(String sku, String location, String type);
}
