package com.example.banking.entity;


import com.example.banking.status.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(nullable = false)
    private boolean approved = false;

    private boolean accountNonLocked = true;
    private int failedAttempt = 0;
    private LocalDateTime lockTime;


}

