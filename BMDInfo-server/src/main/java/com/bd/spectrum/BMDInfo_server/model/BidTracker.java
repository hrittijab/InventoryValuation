package com.bd.spectrum.BMDInfo_server.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BidTracker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String client;
    private String description;
    private String cam;
    private String salesResource;
    private String bidManager;
    private String initiationMode;
    private String stage;
    private String procurementType;
    private String goods;
    private String works;
    private String service;
    private String tenderId;
    private String initiation;
    private String published_initiation;
    @JsonFormat(pattern = "EEE, MMM dd, yyyy", locale = "en")
    private LocalDate published;
    @JsonFormat(pattern = "EEE, MMM dd, yyyy", locale = "en")
    private LocalDate preBidDate;
    @JsonFormat(pattern = "h:mm:ss a", locale = "en")
    private LocalTime preBidTime;
    @JsonFormat(pattern = "EEE, MMM dd, yyyy", locale = "en")
    private LocalDate submissionDate;
    @JsonFormat(pattern = "h:mm:ss a", locale = "en")
    private LocalTime submissionTime;
    private String securityMode;
    private BigDecimal securityAmount;
    private BigDecimal creditFacility;
    @JsonFormat(pattern = "EEE, MMM dd, yyyy", locale = "en")
    private LocalDate issuingDate;
    private String referenceNumber;
    private String issuingBank;
    @JsonFormat(pattern = "dd MMM,yyyy", locale = "en")
    private LocalDate date;
    private String result;
    private String noName;
    private String remarks;
}