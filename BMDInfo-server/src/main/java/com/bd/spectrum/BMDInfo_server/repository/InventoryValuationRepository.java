package com.bd.spectrum.BMDInfo_server.repository;

import com.bd.spectrum.BMDInfo_server.model.InventoryValuation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface InventoryValuationRepository extends JpaRepository<InventoryValuation, UUID> {

    Optional<InventoryValuation> findByItemIdAndLocationAndValuationMethod(
            UUID itemId, String location, String valuationMethod
    );

    // For valuation history 
    List<InventoryValuation> findByItemIdOrderByLastUpdatedAsc(UUID itemId);

    // History filtered by location
    List<InventoryValuation> findByItemIdAndLocationOrderByLastUpdatedAsc(UUID itemId, String location);

    // History filtered by method
    List<InventoryValuation> findByItemIdAndValuationMethodOrderByLastUpdatedAsc(
            UUID itemId, String valuationMethod
    );

    // Prevent duplicate valuations for same item, method, and date
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END " +
           "FROM InventoryValuation v " +
           "WHERE v.item.id = :itemId AND v.location = :location " +
           "AND v.valuationMethod = :method " +
           "AND DATE(v.lastUpdated) = :date")
    boolean existsByItemIdAndLocationAndValuationMethodAndDate(
            @Param("itemId") UUID itemId,
            @Param("location") String location,
            @Param("method") String method,
            @Param("date") LocalDate date
    );
}
