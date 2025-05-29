package com.example.banking.service;

import com.example.banking.constant.SecurityConstants;
import com.example.banking.auth.AuthRequest;
import com.example.banking.dto.RegisterRequest;
import com.example.banking.repository.UserRepository;
import com.example.banking.status.Role;
import com.example.banking.entity.User;
import com.example.banking.config.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final JWTUtil jwtUtil;


    public void registerUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);

        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        user.setLockTime(null);
        user.setApproved(false);
        userRepository.save(user);
    }


    public String authenticateUser(AuthRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("User not found");
        }

        User user = userOpt.get();

        if (!user.isApproved()) {
            throw new IllegalArgumentException("Your registration is pending approval by staff.");
        }

        if (!user.isAccountNonLocked()) {
            if (!unlockWhenTimeExpired(user)) {
                throw new LockedException("Account locked. Try again later.");
            }
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            increaseFailedAttempts(user);
            throw new BadCredentialsException("Invalid credentials");
        }

        resetFailedAttempts(user);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isAccountNonLocked(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );

        return jwtUtil.generateToken(userDetails);

    }


    public void increaseFailedAttempts(User user) {
        int newFailAttempts = user.getFailedAttempt() + 1;
        user.setFailedAttempt(newFailAttempts);
        if (newFailAttempts >= SecurityConstants.MAX_FAILED_ATTEMPTS) {
            lockAccount(user);
        }
        userRepository.save(user);
    }
    public List<User> getPendingUsers() {
        return userRepository.findByApprovedFalse();
    }

    public void resetFailedAttempts(User user) {
        user.setFailedAttempt(0);
        user.setLockTime(null);
        userRepository.save(user);
    }

    public void lockAccount(User user) {
        user.setAccountNonLocked(false);
        user.setLockTime(LocalDateTime.now());
    }
    public User createUserByAdmin(String username, String rawPassword, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.valueOf(role.toUpperCase()));

        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        user.setLockTime(null);
        user.setApproved(true);
        return userRepository.save(user);
    }

    public boolean unlockWhenTimeExpired(User user) {
        if (user.getLockTime() == null) return false;

        LocalDateTime unlockTime = user.getLockTime().plusMinutes(SecurityConstants.LOCK_TIME_DURATION);
        if (LocalDateTime.now().isAfter(unlockTime)) {
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            user.setFailedAttempt(0);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
