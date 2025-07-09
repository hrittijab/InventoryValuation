package com.bd.spectrum.BMDInfo_server.controller;

import com.bd.spectrum.BMDInfo_server.dto.CategoryCountDTO;
import com.bd.spectrum.BMDInfo_server.dto.ClientSubmissionSummaryDTO;
import com.bd.spectrum.BMDInfo_server.dto.MonthlySubmissionDTO;
import com.bd.spectrum.BMDInfo_server.model.BidTracker;
import com.bd.spectrum.BMDInfo_server.service.BidTrackerSheetImportService;
import com.bd.spectrum.BMDInfo_server.service.LastFetchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bid-tracker-sheets")
public class BidTrackerSheetController {

    private final BidTrackerSheetImportService importService;

    @Autowired
    private LastFetchService lastFetchService;

    public BidTrackerSheetController(BidTrackerSheetImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/import")
    public String importData() throws Exception {
        importService.importSheetData();
        lastFetchService.saveLastFetch();
        return "Data imported successfully!";
    }

    @GetMapping
    public List<BidTracker> get(){
        return importService.getAll();
    }

    @GetMapping("{id}")
    public BidTracker getById(@PathVariable UUID id){
        return importService.getById(id);
    }

    @GetMapping("/get-by-date")
    public List<BidTracker> getByDateRange(
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return importService.getByDateRange(fromDate, toDate);
    }

    @GetMapping("/total-summary")
    public List<CategoryCountDTO> getSubmissionAndResultSummary() {
        return importService.getCategoryStats();
    }

    @GetMapping("/total-summary-by-date")
    public List<CategoryCountDTO> getSubmissionAndResultSummaryByDate(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return importService.getCategoryStatsByDate(from, to);
    }

    @GetMapping("/monthly-submission-summary")
    public List<MonthlySubmissionDTO> getMonthlySubmissionStats() {
        return importService.getLast12MonthsSubmissionSummary();
    }

    @GetMapping("/client-submission-summary")
    public List<ClientSubmissionSummaryDTO> getClientSubmissionStats() {
        return importService.getClientSummary();
    }

    @GetMapping("/client-submission-summary-by-date-range")
    public List<ClientSubmissionSummaryDTO> getClientSubmissionSummary(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return importService.getClientSummaryByDate(from, to);
    }


}