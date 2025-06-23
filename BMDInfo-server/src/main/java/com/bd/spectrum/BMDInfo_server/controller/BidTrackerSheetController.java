package com.bd.spectrum.BMDInfo_server.controller;

import com.bd.spectrum.BMDInfo_server.service.BidTrackerSheetImportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}