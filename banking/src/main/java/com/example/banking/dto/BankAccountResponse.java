package com.example.banking.dto;

import com.example.banking.entity.BankAccount;
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
    public static BankAccountResponse fromEntity(BankAccount account) {
        BankAccountResponse dto = new BankAccountResponse();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setAccountType(account.getAccountType().name());
        dto.setStatus(account.getStatus().name());
        dto.setUsername(account.getOwner() != null ? account.getOwner().getUsername() : null);
        dto.setCreatedDate(account.getCreatedDate());
        dto.setApproved(account.isApproved());
        dto.setInitialDeposit(account.getInitialDeposit());
        dto.setBalance(account.getBalance());
        dto.setBranch(account.getBranch());
        return dto;
    }

}

