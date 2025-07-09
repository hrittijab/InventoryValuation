package com.bd.spectrum.BMDInfo_server.dto;

public class ClientSubmissionSummaryDTO {
    private String client;
    private Long submittedCount;
    private Long notSubmittedCount;

    public ClientSubmissionSummaryDTO(String client, Long submittedCount, Long notSubmittedCount) {
        this.client = client;
        this.submittedCount = submittedCount;
        this.notSubmittedCount = notSubmittedCount;
    }

    public String getClient() { return client; }
    public Long getSubmittedCount() { return submittedCount; }
    public Long getNotSubmittedCount() { return notSubmittedCount; }
}

