package com.example.banking.controller;

import com.example.banking.dto.AccountCreationRequest;
import com.example.banking.dto.BankAccountResponse;
import com.example.banking.entity.BankAccount;
import com.example.banking.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/auth/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService accountService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody AccountCreationRequest request, Principal principal) {
        String username = principal.getName(); // Extract from JWT token
        BankAccount account = accountService.createAccount(
                username,
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
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<BankAccountResponse>> getActiveAccounts() {
        List<BankAccountResponse> response = accountService.getActiveAccountResponses();
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        accountService.approveAccount(id);
        return ResponseEntity.ok("Account approved");
    }
}

