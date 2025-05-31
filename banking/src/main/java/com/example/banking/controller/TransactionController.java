package com.example.banking.controller;

import com.example.banking.dto.*;
import com.example.banking.entity.BankAccount;
import com.example.banking.entity.Transaction;
import com.example.banking.repository.UserRepository;
import com.example.banking.service.BankAccountService;
import com.example.banking.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final BankAccountService bankAccountService;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@RequestBody TransactionRequest request, Principal principal) {
        String username = principal.getName();
        transactionService.validateAccountOwnership(request.getAccountNumber(), username);

        Transaction transaction = transactionService.deposit(
                request.getAccountNumber(), request.getAmount(), request.getRemarks());
        TransactionResponse response = transactionService.toResponse(transaction);
        return ResponseEntity.ok(response);
    }




    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@RequestBody TransactionRequest request, Principal principal) {
        String username = principal.getName();
        transactionService.validateAccountOwnership(request.getAccountNumber(), username);

        Transaction transaction = transactionService.withdraw(
                request.getAccountNumber(), request.getAmount(), request.getRemarks());
        TransactionResponse response = transactionService.toResponse(transaction);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody TransferRequest request, Principal principal) {
        String username = principal.getName();
        transactionService.validateAccountOwnership(request.getSourceAccountNumber(), username);

        Transaction transaction = transactionService.transfer(
                request.getSourceAccountNumber(),
                request.getDestinationAccountNumber(),
                request.getAmount(),
                request.getRemarks()
        );
        return ResponseEntity.ok(transactionService.toResponse(transaction));
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
    @GetMapping("/{accountId}/statement")
    public ResponseEntity<byte[]> getStatement(@PathVariable Long accountId, Authentication auth) throws Exception {
        BankAccount account = bankAccountService.findById(accountId);

        // Check if user is owner or has STAFF/ADMIN role
        String loggedUsername = auth.getName();
        boolean isOwner = account.getOwner().getUsername().equals(loggedUsername);
        boolean isStaffOrAdmin = auth.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_STAFF") || role.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isStaffOrAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Transaction> transactions = transactionService.findByAccount(accountId);
        byte[] pdfBytes = transactionService.generatePdfStatement(transactions);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"statement.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
    @GetMapping("/summary/{accountId}")
    public ResponseEntity<List<MonthlySummaryResponse>> getMonthlySummary(
            @PathVariable Long accountId,
            @RequestParam String yearMonth,
            Authentication auth) {

        BankAccount account = bankAccountService.findById(accountId);

        String loggedUsername = auth.getName();
        boolean isOwner = account.getOwner().getUsername().equals(loggedUsername);
        boolean isStaffOrAdmin = auth.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_STAFF") || role.getAuthority().equals("ROLE_ADMIN")); // use ROLE_ prefix

        if (!isOwner && !isStaffOrAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Parse manually
        YearMonth ym = YearMonth.parse(yearMonth); // expects "2025-05"

        List<MonthlySummaryResponse> summary = transactionService.getMonthlySummary(accountId, ym);
        return ResponseEntity.ok(summary);
    }
    @GetMapping("/summary/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<MonthlyResponse>> getAllAccountsMonthlySummary(
            @RequestParam String yearMonth) {

        YearMonth ym = YearMonth.parse(yearMonth); // expects format "2025-05"
        List<MonthlyResponse> summary = transactionService.getMonthlySummaryForAllAccounts(ym);
        return ResponseEntity.ok(summary);
    }
    @GetMapping("/statement/all")
    public ResponseEntity<byte[]> downloadAllStatements(
            @RequestParam(required = false) String yearMonth,
            Authentication auth) throws Exception {

        boolean isStaffOrAdmin = auth.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_STAFF") || role.getAuthority().equals("ROLE_ADMIN"));

        if (!isStaffOrAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Transaction> allTransactions;
        if (yearMonth != null) {
            YearMonth ym = YearMonth.parse(yearMonth); // e.g. "2025-05"
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();
            allTransactions = transactionService.findTransactionsBetween(start, end);
        } else {
            allTransactions = transactionService.findAllTransactions();
        }

        byte[] pdfBytes = transactionService.generatePdfStatement(allTransactions);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition
                .builder("attachment")
                .filename("all_statements.pdf")
                .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }


}
