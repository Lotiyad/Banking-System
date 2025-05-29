package com.example.banking.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequest {
    private String accountNumber;
    private BigDecimal amount;
    private String remarks;
}

