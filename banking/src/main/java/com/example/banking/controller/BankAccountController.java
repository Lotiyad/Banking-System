package com.example.banking.controller;

import com.example.banking.dto.AccountCreationRequest;
import com.example.banking.entity.BankAccount;
import com.example.banking.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService accountService;

    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody AccountCreationRequest request) {
        BankAccount account = accountService.createAccount(

                request.getUsername(),
                request.getAccountType(),
                request.getInitialDeposit(),
                request.getBranch()
        );
        return ResponseEntity.ok("Account request submitted with ID: " + account.getId());
    }

    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<?> viewPending() {
        return ResponseEntity.ok(accountService.getPendingAccounts());
    }

    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        accountService.approveAccount(id);
        return ResponseEntity.ok("Account approved");
    }
}

