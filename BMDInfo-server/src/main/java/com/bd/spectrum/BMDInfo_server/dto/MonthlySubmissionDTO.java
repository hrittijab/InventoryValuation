package com.bd.spectrum.BMDInfo_server.dto;

public class MonthlySubmissionDTO {
    private String month;
    private String submission;
    private Long total;

    public MonthlySubmissionDTO(String month, String submission, Long total) {
        this.month = month;
        this.submission = submission;
        this.total = total;
    }

    public String getMonth() { return month; }
    public String getSubmission() { return submission; }
    public Long getTotal() { return total; }
}

