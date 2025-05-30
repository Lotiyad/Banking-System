package com.example.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String referenceNumber;
    private String type;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String remarks;
    private String sourceAccount;
    private String destinationAccount;

}

