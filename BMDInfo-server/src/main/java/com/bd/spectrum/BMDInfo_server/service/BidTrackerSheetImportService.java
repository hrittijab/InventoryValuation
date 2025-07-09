package com.bd.spectrum.BMDInfo_server.service;

import com.bd.spectrum.BMDInfo_server.dto.CategoryCountDTO;
import com.bd.spectrum.BMDInfo_server.dto.ClientSubmissionSummaryDTO;
import com.bd.spectrum.BMDInfo_server.dto.MonthlySubmissionDTO;
import com.bd.spectrum.BMDInfo_server.model.BidTracker;
import com.bd.spectrum.BMDInfo_server.repository.BidTrackerRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BidTrackerSheetImportService {

    private final BidTrackerRepository bidTrackerRepository;

    public BidTrackerSheetImportService(BidTrackerRepository bidTrackerRepository) {
        this.bidTrackerRepository = bidTrackerRepository;
    }

    public void importSheetData() throws Exception {
        this.deleteAll();
        Sheets sheets = getSheetsService();
        String spreadsheetId = "1v5Nw0E8FDUh2FZ7bHRF4BSJOA8u7nQUET3rgKFCvUMc";
        String range = "bids!B4:AE";

        ValueRange response = sheets.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> rows = response.getValues();

        if (rows != null) {
            DateTimeFormatter dateFormatter1 = DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy", Locale.ENGLISH);
            DateTimeFormatter dateFormatter2 = DateTimeFormatter.ofPattern("dd MMM,yyyy", Locale.ENGLISH);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH);

            for (List<Object> row : rows) {
                int i = 0;
                String client = getString(row, i++);
                String description = getString(row, i++);
                String cam = getString(row, i++);
                String salesResource = getString(row, i++);
                String bidManager = getString(row, i++);
                String initiationMode = getString(row, i++);
                String stage = getString(row, i++);
                String procurementType = getString(row, i++);
                String goods = getString(row, i++);
                String works = getString(row, i++);
                String service = getString(row, i++);
                String tenderId = getString(row, i++);
                String initiation = getString(row, i++);
                String published_initiation = getString(row, i++);
                LocalDate published = parseDate(getString(row, i++), dateFormatter1);
                LocalDate preBidDate = parseDate(getString(row, i++), dateFormatter1);
                LocalTime preBidTime = parseTime(getString(row, i++), timeFormatter);
                LocalDate submissionDate = parseDate(getString(row, i++), dateFormatter1);
                LocalTime submissionTime = parseTime(getString(row, i++), timeFormatter);
                String securityMode = getString(row, i++);
                BigDecimal securityAmount = parseBigDecimal(getString(row, i++));
                BigDecimal creditFacility = parseBigDecimal(getString(row, i++));
                LocalDate issuingDate = parseDate(getString(row, i++), dateFormatter1);
                String referenceNumber = getString(row, i++);
                String issuingBank = getString(row, i++);
                LocalDate expiryDate = parseDate(getString(row, i++), dateFormatter2);
                String submission = getString(row, i++);
                String result = getString(row, i++);
                String noName = getString(row, i++);
                String remarks = getString(row, i++);

                BidTracker bidTracker = new BidTracker(
                        null, client, description, cam, salesResource, bidManager,
                        initiationMode, stage, procurementType, goods, works, service,
                        tenderId, initiation, published_initiation, published, preBidDate,
                        preBidTime, submissionDate, submissionTime, securityMode,
                        securityAmount, creditFacility, issuingDate, referenceNumber,
                        issuingBank, expiryDate, submission, result, noName, remarks
                );

                bidTrackerRepository.save(bidTracker);
            }
        }
    }

    private Sheets getSheetsService() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("credentials.json");

        if (inputStream == null) {
            throw new FileNotFoundException("credentials.json not found in resources");
        }

        GoogleCredential credential = GoogleCredential.fromStream(inputStream)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

        return new Sheets.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
                .setApplicationName("Google Sheets Import")
                .build();
    }

    // ---------- Helper methods ----------

    private String getString(List<Object> row, int index) {
        return row.size() > index ? row.get(index).toString().trim() : "";
    }

    private LocalDate parseDate(String input, DateTimeFormatter formatter) {
        try {
            if (input == null || input.isBlank() || input.equalsIgnoreCase("N/A")) return null;
            return LocalDate.parse(input, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime parseTime(String input, DateTimeFormatter formatter) {
        try {
            if (input == null || input.isBlank() || input.equalsIgnoreCase("N/A")) return null;
            return LocalTime.parse(input, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String input) {
        try {
            if (input == null || input.isBlank() || input.equalsIgnoreCase("N/A")) return null;
            return new BigDecimal(input.replaceAll(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<BidTracker> getAll(){
        return bidTrackerRepository.findAll();
    }

    public BidTracker getById(UUID id){
        return bidTrackerRepository.findById(id).orElse(null);
    }

    public void deleteAll(){
        bidTrackerRepository.deleteAll();
    }

    public List<BidTracker> getByDateRange(LocalDate fromDate, LocalDate toDate){
       return bidTrackerRepository.findBySubmissionDateBetween(fromDate, toDate);
    }

    public List<CategoryCountDTO> getCategoryStats() {
        return bidTrackerRepository.getSubmissionAndResultStats().stream()
                .map(row -> new CategoryCountDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<CategoryCountDTO> getCategoryStatsByDate(LocalDate from, LocalDate to) {
        return bidTrackerRepository.getSubmissionAndResultStatsByDate(from, to).stream()
                .map(row -> new CategoryCountDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }


    public List<MonthlySubmissionDTO> getLast12MonthsSubmissionSummary() {
        return bidTrackerRepository.getMonthlySubmissionCounts().stream()
                .map(row -> new MonthlySubmissionDTO(
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<ClientSubmissionSummaryDTO> getClientSummary() {
        return bidTrackerRepository.getClientSubmissionSummary().stream()
                .map(row -> new ClientSubmissionSummaryDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }

    public List<ClientSubmissionSummaryDTO> getClientSummaryByDate(LocalDate from, LocalDate to) {
        return bidTrackerRepository.getClientSubmissionSummaryByDate(from, to).stream()
                .map(row -> new ClientSubmissionSummaryDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }


}
