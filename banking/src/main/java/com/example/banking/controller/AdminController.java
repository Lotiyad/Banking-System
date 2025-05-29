package com.example.banking.controller;
import java.util.Map;

import com.example.banking.dto.AdminUserCreationRequest;
import com.example.banking.repository.UserRepository;
import com.example.banking.service.UserService;
import com.example.banking.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/create-user")
    public ResponseEntity<?> createUserByAdmin(@RequestBody AdminUserCreationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        }

        String requestedRole = request.getRole();
        if (requestedRole == null ||
                (!requestedRole.equalsIgnoreCase("ADMIN") && !requestedRole.equalsIgnoreCase("STAFF"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid role. Only ADMIN or STAFF roles allowed.");
        }

        String normalizedRole = requestedRole.toUpperCase();

        userService.createUserByAdmin(
                request.getUsername(),
                request.getPassword(),
                normalizedRole
        );

        return ResponseEntity.ok("User with role " + normalizedRole + " created successfully.");
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @GetMapping("/admin/pending-registrations")
    public ResponseEntity<?> getPendingRegistrations() {
        List<User> pendingUsers = userService.getPendingUsers();

        return ResponseEntity.ok(pendingUsers.stream().map(user -> Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "accountNonLocked", user.isAccountNonLocked()
        )).toList());
    }

    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @PutMapping("/staff/approve-user/{username}")
    public ResponseEntity<?> approveUser(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOpt.get();
        user.setApproved(true);
        userRepository.save(user);

        return ResponseEntity.ok("User approved successfully");
    }

}
