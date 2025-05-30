package com.example.banking.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class MonthlySummaryResponse {
    private String transactionType;
    private BigDecimal totalAmount;
    public MonthlySummaryResponse(String transactionType, BigDecimal totalAmount) {
        this.transactionType = transactionType;
        this.totalAmount = totalAmount;
    }
}
