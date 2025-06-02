package com.example.banking.service;

import com.example.banking.dto.BankAccountResponse;
import com.example.banking.entity.BankAccount;
import com.example.banking.exception.ResourceNotFoundException;
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
    private final BankAccountRepository bankAccountRepository;
    private final AuditLogService auditLogService;
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


    public List<BankAccountResponse> getPendingAccounts() {
        List<BankAccount> pendingAccounts = accountRepository.findByApprovedFalse();

        return pendingAccounts.stream()
                .map(account -> new BankAccountResponse(
                        account.getId(),
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

    public void approveAccount(Long accountId) {
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setApproved(true);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
        auditLogService.logAction(
                "Account Approval",
                "Account approved: ID=" + account.getId() + ", AccountNumber=" + account.getAccountNumber()
        );
    }

    public List<BankAccountResponse> getAccountsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<BankAccount> accounts = accountRepository.findByOwner(user);

        return accounts.stream()
                .map(account -> new BankAccountResponse(
                        account.getId(),
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
    public BankAccount findById(Long accountId) {
        return bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }
    public List<BankAccountResponse> getActiveAccountResponses() {
        List<BankAccount> activeAccounts = accountRepository.findByStatus(AccountStatus.ACTIVE);

        return activeAccounts.stream()
                .map(account -> new BankAccountResponse(
                        account.getId(),
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
    public List<BankAccount> findAllAccounts() {
        return bankAccountRepository.findAll();
    }
    public void freezeAccount(Long accountId) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        account.setStatus(AccountStatus.FROZEN);
        bankAccountRepository.save(account);

        // Create descriptive details string
        String details = "Account frozen: ID=" + account.getId() + ", AccountNumber=" + account.getAccountNumber();

        auditLogService.logAction("Freeze Account", details);
    }

    public void unfreezeAccount(Long accountId) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        account.setStatus(AccountStatus.ACTIVE);
        bankAccountRepository.save(account);

        // Create descriptive details string
        String details = "Account unfrozen: ID=" + account.getId() + ", AccountNumber=" + account.getAccountNumber();

        auditLogService.logAction("Unfreeze Account", details);
    }


}

