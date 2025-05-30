package com.example.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountResponse {
    private Long id;
    private String accountNumber;
    private String accountType;
    private String status;
    private String username;
    private LocalDate createdDate;
    private boolean approved;
    private BigDecimal initialDeposit;
    private BigDecimal balance;

    private String branch;
}

