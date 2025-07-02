package com.bd.spectrum.BMDInfo_server.controller;

import com.bd.spectrum.BMDInfo_server.dto.CategoryCountDTO;
import com.bd.spectrum.BMDInfo_server.dto.MonthlySubmissionDTO;
import com.bd.spectrum.BMDInfo_server.model.BidTracker;
import com.bd.spectrum.BMDInfo_server.service.BidTrackerSheetImportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bid-tracker-sheets")
public class BidTrackerSheetController {

    private final BidTrackerSheetImportService importService;

    public BidTrackerSheetController(BidTrackerSheetImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/import")
    public String importData() throws Exception {
        importService.importSheetData();
        return "Data imported successfully!";
    }

    @GetMapping
    public List<BidTracker> get(){
        return importService.getAll();
    }

    @GetMapping("{id}")
    public BidTracker getById(@PathVariable Long id){
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

    @GetMapping("/monthly-submission-summary")
    public List<MonthlySubmissionDTO> getMonthlySubmissionStats() {
        return importService.getLast12MonthsSubmissionSummary();
    }
}