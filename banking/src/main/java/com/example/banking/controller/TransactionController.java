package com.example.banking.controller;

import com.example.banking.dto.TransactionRequest;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.dto.TransferRequest;
import com.example.banking.entity.Transaction;
import com.example.banking.repository.UserRepository;
import com.example.banking.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@RequestBody TransactionRequest request) {
        Transaction transaction = transactionService.deposit(
                request.getAccountNumber(), request.getAmount(), request.getRemarks());
        TransactionResponse response = transactionService.toResponse(transaction);
        return ResponseEntity.ok(response);
    }



    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@RequestBody TransactionRequest request) {
        Transaction transaction = transactionService.withdraw(
                request.getAccountNumber(), request.getAmount(), request.getRemarks());
        TransactionResponse response = transactionService.toResponse(transaction);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody TransferRequest request) {
        Transaction transaction = transactionService.transfer(
                request.getSourceAccountNumber(),
                request.getDestinationAccountNumber(),
                request.getAmount(),
                request.getRemarks()
        );
        return ResponseEntity.ok(transactionService.toResponse(transaction)); // or use your local toResponse()
    }

    @GetMapping("/my")
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(Principal principal) {
        String username = principal.getName();
        List<TransactionResponse> transactions = transactionService.getUserTransactions(username);
        return ResponseEntity.ok(transactions);
    }
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        List<TransactionResponse> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

}
