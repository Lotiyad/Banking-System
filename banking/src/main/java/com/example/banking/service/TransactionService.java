package com.example.banking.service;

import com.example.banking.dto.MonthlyResponse;
import com.example.banking.dto.MonthlySummaryResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.entity.BankAccount;
import com.example.banking.entity.Transaction;
import com.example.banking.entity.User;
import com.example.banking.repository.BankAccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.UserRepository;
import com.example.banking.status.AccountStatus;
import com.example.banking.status.TransactionType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;



import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final BankAccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BankAccountService bankAccountService;
    private String generateReference() {
        return UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }

    public Transaction deposit(String accountNumber, BigDecimal amount, String remarks) {
        BankAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.isApproved() || account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Deposits are only allowed on approved and active accounts.");
        }
        account.setBalance(account.getBalance().add(amount));

        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setSourceAccount(account);
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(amount);
        tx.setTimestamp(LocalDateTime.now());
        tx.setRemarks(remarks);
        tx.setReferenceNumber(generateReference());



        return transactionRepository.save(tx);
    }

    public Transaction withdraw(String accountNumber, BigDecimal amount, String remarks) {
        BankAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        if (!account.isApproved() || account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("withdraw are only allowed on approved and active accounts.");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setSourceAccount(account);
        tx.setType(TransactionType.WITHDRAW);
        tx.setAmount(amount);
        tx.setTimestamp(LocalDateTime.now());
        tx.setRemarks(remarks);
        tx.setReferenceNumber(generateReference());



        return transactionRepository.save(tx);
    }

    public Transaction transfer(String fromAcc, String toAcc, BigDecimal amount, String remarks) {
        BankAccount source = accountRepository.findByAccountNumber(fromAcc)
                .orElseThrow(() -> new RuntimeException("Source account not found"));
        BankAccount dest = accountRepository.findByAccountNumber(toAcc)
                .orElseThrow(() -> new RuntimeException("Destination account not found"));

        validateAccountActiveAndApproved(source, "source");
        validateAccountActiveAndApproved(dest, "destination");

        if (source.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds for transfer");
        }


        source.setBalance(source.getBalance().subtract(amount));
        dest.setBalance(dest.getBalance().add(amount));

        accountRepository.save(source);
        accountRepository.save(dest);

        String referenceNumber = generateReference();


        Transaction debitTx = new Transaction();
        debitTx.setSourceAccount(source);
        debitTx.setDestinationAccount(dest);
        debitTx.setType(TransactionType.DEBIT);
        debitTx.setAmount(amount);
        debitTx.setTimestamp(LocalDateTime.now());
        debitTx.setRemarks("Transfer to " + toAcc + " - " + remarks);
        debitTx.setReferenceNumber(referenceNumber);


        Transaction creditTx = new Transaction();
        creditTx.setSourceAccount(source);
        creditTx.setDestinationAccount(dest);
        creditTx.setType(TransactionType.CREDIT);
        creditTx.setAmount(amount);
        creditTx.setTimestamp(LocalDateTime.now());
        creditTx.setRemarks("Transfer from " + fromAcc + " - " + remarks);
        creditTx.setReferenceNumber(referenceNumber);

        transactionRepository.save(debitTx);
        transactionRepository.save(creditTx);

        return debitTx;
    }

    public List<TransactionResponse> getUserTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<BankAccount> userAccounts = accountRepository.findByOwner(user);
        Set<String> userAccountNumbers = userAccounts.stream()
                .map(BankAccount::getAccountNumber)
                .collect(Collectors.toSet());

        List<Transaction> allTransactions = transactionRepository.findAll(); // Optimize this if needed

        List<Transaction> filtered = allTransactions.stream()
                .filter(tx -> {
                    String source = tx.getSourceAccount() != null ? tx.getSourceAccount().getAccountNumber() : null;
                    String destination = tx.getDestinationAccount() != null ? tx.getDestinationAccount().getAccountNumber() : null;

                    return switch (tx.getType()) {
                        case DEBIT, WITHDRAW -> source != null && userAccountNumbers.contains(source);
                        case CREDIT -> destination != null && userAccountNumbers.contains(destination);
                        case DEPOSIT -> source != null && userAccountNumbers.contains(source); // deposit to user's account
                    };
                })
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .collect(Collectors.toList());

        return filtered.stream().map(this::toResponse).collect(Collectors.toList());
    }



    public TransactionResponse toResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getReferenceNumber(),
                tx.getType().name(),
                tx.getAmount(),
                tx.getTimestamp(),
                tx.getRemarks(),
                tx.getSourceAccount() != null ? tx.getSourceAccount().getAccountNumber() : null,
                tx.getDestinationAccount() != null ? tx.getDestinationAccount().getAccountNumber() : null
        );
    }
    public List<TransactionResponse> getAllTransactions() {
        List<Transaction> allTransactions = transactionRepository.findAll();


        allTransactions.sort(Comparator.comparing(Transaction::getTimestamp).reversed());

        return allTransactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    public void validateAccountOwnership(String accountNumber, String username) {
        BankAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getOwner().getUsername().equals(username)) {
            throw new SecurityException("Access denied: This account does not belong to the logged-in user.");
        }
    }

    private void validateAccountActiveAndApproved(BankAccount account, String role) {
        if (!account.isApproved() || account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("The " + role + " account is not approved or active.");
        }
    }
    public List<Transaction> getTransactions(Long accountId, LocalDate start, LocalDate end) {
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return transactionRepository.findBySourceAccountOrDestinationAccountAndTimestampBetween(
                account, account, start.atStartOfDay(), end.atTime(LocalTime.MAX));
    }
    public List<MonthlySummaryResponse> getMonthlySummary(Long accountId, YearMonth yearMonth) {
        BankAccount account = bankAccountService.findById(accountId);

        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Object[]> results = transactionRepository.findMonthlySummary(account, startDate, endDate);

        return results.stream()
                .map(r -> new MonthlySummaryResponse(
                        r[0].toString(),
                        (BigDecimal) r[1]
                ))
                .collect(Collectors.toList());
    }

    public byte[] generatePdfStatement(List<Transaction> transactions) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        PdfWriter.getInstance(document, baos);

        document.open();
        document.add(new Paragraph("Bank Statement"));
        document.add(new Paragraph("Date: " + LocalDate.now().toString()));
        document.add(new Paragraph(" ")); // add empty line for spacing

        for (Transaction txn : transactions) {
            String line = txn.getTimestamp() + " - " + txn.getType()
                    + " - " + txn.getAmount() + " - Ref: " + txn.getReferenceNumber();
            document.add(new Paragraph(line));
        }

        document.close();
        return baos.toByteArray();
    }
    public List<Transaction> findByAccount(Long accountId) {
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        String accNum = account.getAccountNumber();

        return transactionRepository.findAll().stream()
                .filter(tx -> {
                    String source = tx.getSourceAccount() != null ? tx.getSourceAccount().getAccountNumber() : null;
                    String destination = tx.getDestinationAccount() != null ? tx.getDestinationAccount().getAccountNumber() : null;

                    return switch (tx.getType()) {
                        case DEBIT, WITHDRAW ,DEPOSIT -> accNum.equals(source); // account spent money
                        case CREDIT -> accNum.equals(destination); // account received money
                    };

                })
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public List<Transaction> findAllTransactions() {
        return transactionRepository.findAll();
    }
    public List<MonthlyResponse> getMonthlySummaryForAllAccounts(YearMonth yearMonth) {
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Object[]> results = transactionRepository.findMonthlySummaryForAllAccounts(startDate, endDate);

        return results.stream()
                .map(r -> new MonthlyResponse(
                        r[0].toString(),              // account number
                        r[1].toString(),              // transaction type
                        (BigDecimal) r[2]             // total amount
                ))
                .collect(Collectors.toList());
    }
    public List<Transaction> findTransactionsBetween(LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByTimestampBetween(start, end);
    }

}

