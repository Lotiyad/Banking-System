package com.example.banking.service;

import com.example.banking.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.example.banking.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository; // Your JPA repository

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user by username (or email)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Convert your User entity to Spring Security's UserDetails object
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isAccountNonLocked(), // accountNonLocked
                true,                      // accountNonExpired
                true,                      // credentialsNonExpired
                true,                      // enabled
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}

