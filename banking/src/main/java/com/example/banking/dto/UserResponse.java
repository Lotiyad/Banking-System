package com.example.banking.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String role;
    private boolean approved;
    private boolean accountNonLocked;


}

