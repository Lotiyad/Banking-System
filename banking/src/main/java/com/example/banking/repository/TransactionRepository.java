package com.example.banking.repository;

import com.example.banking.entity.BankAccount;
import com.example.banking.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySourceAccount(BankAccount account);
    List<Transaction> findBySourceAccountOrDestinationAccount(BankAccount sourceAccount, BankAccount destinationAccount);
    List<Transaction> findByDestinationAccount(BankAccount account);
    List<Transaction> findBySourceAccountOrDestinationAccountAndTimestampBetween(
            BankAccount source, BankAccount destination, LocalDateTime start, LocalDateTime end);
    @Query("""
    SELECT t.type AS transactionType, SUM(t.amount) AS totalAmount
    FROM Transaction t
    WHERE (t.sourceAccount = :account OR t.destinationAccount = :account)
      AND t.timestamp >= :startDate
      AND t.timestamp < :endDate
    GROUP BY t.type
""")
    List<Object[]> findMonthlySummary(
            @Param("account") BankAccount account,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    List<Transaction> findAll();
    @Query("""
    SELECT 
        t.sourceAccount.accountNumber,
        t.type,
        SUM(t.amount)
    FROM Transaction t
    WHERE t.timestamp BETWEEN :startDate AND :endDate
    GROUP BY t.sourceAccount.accountNumber, t.type
""")
    List<Object[]> findMonthlySummaryForAllAccounts(LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

}

