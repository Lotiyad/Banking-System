package com.example.banking.entity;

import com.example.banking.status.AccountStatus;
import com.example.banking.status.AccountType;
import jakarta.persistence.*;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "account")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @ManyToOne(optional = false)
    private User owner;

    private LocalDate createdDate;

    private boolean approved;


    @Column(nullable = false)
    private BigDecimal initialDeposit;
    private BigDecimal balance = BigDecimal.ZERO;
    @Column(nullable = false)
    private String branch;
}
