package com.example.banking.service;

import com.example.banking.dto.TransactionResponse;
import com.example.banking.entity.BankAccount;
import com.example.banking.entity.Transaction;
import com.example.banking.entity.User;
import com.example.banking.repository.BankAccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.UserRepository;
import com.example.banking.status.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final BankAccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private String generateReference() {
        return UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }

    public Transaction deposit(String accountNumber, BigDecimal amount, String remarks) {
        BankAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

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

        if (source.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds for transfer");
        }

        source.setBalance(source.getBalance().subtract(amount));
        dest.setBalance(dest.getBalance().add(amount));

        accountRepository.save(source);
        accountRepository.save(dest);

        Transaction tx = new Transaction();
        tx.setSourceAccount(source);
        tx.setDestinationAccount(dest);
        tx.setType(TransactionType.TRANSFER);
        tx.setAmount(amount);
        tx.setTimestamp(LocalDateTime.now());
        tx.setRemarks(remarks);
        tx.setReferenceNumber(generateReference());



        return transactionRepository.save(tx);
    }
    public List<TransactionResponse> getUserTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<BankAccount> accounts = accountRepository.findByOwner(user);

        List<Transaction> allTransactions = new ArrayList<>();
        for (BankAccount account : accounts) {
            List<Transaction> source = transactionRepository.findBySourceAccount(account);
            List<Transaction> destination = transactionRepository.findByDestinationAccount(account);
            allTransactions.addAll(source);
            allTransactions.addAll(destination);
        }

        // Optional: Sort by timestamp descending
        allTransactions.sort(Comparator.comparing(Transaction::getTimestamp).reversed());

        return allTransactions.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TransactionResponse toResponse(Transaction tx) {
        return new TransactionResponse(
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

        // Optional: Sort by latest first
        allTransactions.sort(Comparator.comparing(Transaction::getTimestamp).reversed());

        return allTransactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

}

