package com.bd.spectrum.BMDInfo_server.controller;

import com.bd.spectrum.BMDInfo_server.model.BidTracker;
import com.bd.spectrum.BMDInfo_server.service.BidTrackerSheetImportService;
import org.springframework.web.bind.annotation.*;

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
}