package com.bd.spectrum.BMDInfo_server.controller;

import com.bd.spectrum.BMDInfo_server.service.PersonSheetImportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sheets")
public class PersonSheetController {

    private final PersonSheetImportService importService;

    public PersonSheetController(PersonSheetImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/import")
    public String importData() throws Exception {
        importService.importSheetData();
        return "Data imported successfully!";
    }
}