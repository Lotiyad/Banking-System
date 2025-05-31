package com.example.banking.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class MonthlyResponse {
    private String accountNumber;
    private String transactionType;
    private BigDecimal totalAmount;

    public MonthlyResponse(String accountNumber, String transactionType, BigDecimal totalAmount) {
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.totalAmount = totalAmount;
    }


}

