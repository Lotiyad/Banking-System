package com.example.banking.controller;

import com.example.banking.dto.BankAccountResponse;
import com.example.banking.dto.UserResponse;
import com.example.banking.entity.AuditLog;
import com.example.banking.entity.BankAccount;
import com.example.banking.entity.User;
import com.example.banking.service.AuditLogService;
import com.example.banking.service.BankAccountService;

import com.example.banking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
public class AdminUserController {

    private final UserService userService;
    private final BankAccountService bankAccountService;
    private final AuditLogService auditLogService;




@GetMapping("/users")
public ResponseEntity<List<UserResponse>> getAllUsers() {
    List<User> users = userService.findAllUsers();

    List<UserResponse> response = users.stream()
            .map(user -> new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getRole().name(),
                    user.isApproved(),
                    user.isAccountNonLocked()
            ))
            .toList();

    return ResponseEntity.ok(response);
}

@GetMapping("/accounts")
public ResponseEntity<List<BankAccountResponse>> getAllAccounts() {
    List<BankAccount> accounts = bankAccountService.findAllAccounts();
    List<BankAccountResponse> response = accounts.stream()
            .map(BankAccountResponse::fromEntity)
            .collect(Collectors.toList());
    return ResponseEntity.ok(response);
}
    @PutMapping("/accounts/{accountId}/freeze")
    public ResponseEntity<String> freezeAccount(@PathVariable Long accountId) {
        bankAccountService.freezeAccount(accountId);
        return ResponseEntity.ok("✅ Account with ID " + accountId + " has been frozen.");
    }

    @PutMapping("/accounts/{accountId}/unfreeze")
    public ResponseEntity<String> unfreezeAccount(@PathVariable Long accountId) {
        bankAccountService.unfreezeAccount(accountId);
        return ResponseEntity.ok("✅ Account with ID " + accountId + " has been unfrozen.");
    }


@GetMapping("/audit-logs")
public ResponseEntity<List<AuditLog>> getAuditLogs() {
    List<AuditLog> logs = auditLogService.findAll();
    return ResponseEntity.ok(logs);
}

}
