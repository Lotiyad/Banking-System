package com.example.banking.service;

import com.example.banking.dto.BankAccountResponse;
import com.example.banking.entity.BankAccount;
import com.example.banking.repository.BankAccountRepository;
import com.example.banking.repository.UserRepository;
import com.example.banking.status.AccountStatus;
import com.example.banking.status.AccountType;
import com.example.banking.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository accountRepository;
    private final UserRepository userRepository;

    public BankAccount createAccount(String username, AccountType accountType, BigDecimal initialDeposit, String branch) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        BankAccount account = new BankAccount();
        account.setOwner(user);
        account.setAccountType(accountType);
        account.setAccountNumber(generateAccountNumber());
        account.setStatus(AccountStatus.FROZEN);
        account.setApproved(false);
        account.setCreatedDate(LocalDate.now());
        account.setInitialDeposit(initialDeposit);
        account.setBalance(initialDeposit);
        account.setBranch(branch);

        return accountRepository.save(account);
    }


    public List<BankAccount> getPendingAccounts() {
        return accountRepository.findByApprovedFalse();
    }

    public void approveAccount(Long accountId) {
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setApproved(true);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
    }
    public List<BankAccountResponse> getAccountsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<BankAccount> accounts = accountRepository.findByOwner(user);

        return accounts.stream()
                .map(account -> new BankAccountResponse(
                        account.getAccountNumber(),
                        account.getAccountType().name(),
                        account.getStatus().name(),
                        account.getOwner().getUsername(),
                        account.getCreatedDate(),
                        account.isApproved(),
                        account.getInitialDeposit(),
                        account.getBalance(),
                        account.getBranch()
                ))
                .collect(Collectors.toList());
    }

    private String generateAccountNumber() {
        return "ACCT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

