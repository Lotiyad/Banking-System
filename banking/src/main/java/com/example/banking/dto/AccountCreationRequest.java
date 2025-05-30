package com.example.banking.dto;

import com.example.banking.status.AccountType;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class AccountCreationRequest {

    private AccountType accountType;
    private BigDecimal initialDeposit;
    private String branch;
}
