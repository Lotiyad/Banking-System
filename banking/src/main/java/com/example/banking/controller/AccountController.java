package com.example.banking.controller;

import com.example.banking.dto.BankAccountResponse;
import com.example.banking.service.BankAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customer")
public class AccountController {

    private final BankAccountService bankAccountService;

    public AccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/account")
    public ResponseEntity<List<BankAccountResponse>> getUserAccounts() {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();


        List<BankAccountResponse> accounts = bankAccountService.getAccountsByUsername(username);

        return ResponseEntity.ok(accounts);
    }
}

