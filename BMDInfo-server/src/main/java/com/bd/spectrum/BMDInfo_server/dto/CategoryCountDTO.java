package com.bd.spectrum.BMDInfo_server.dto;

public class CategoryCountDTO {
    private String category;
    private Long total;

    public CategoryCountDTO(String category, Long total) {
        this.category = category;
        this.total = total;
    }

    public String getCategory() {
        return category;
    }

    public Long getTotal() {
        return total;
    }
}
