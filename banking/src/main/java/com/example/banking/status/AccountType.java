package com.example.banking.status;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AccountType {
    SAVINGS,
    CURRENT;
    @JsonCreator
    public static AccountType fromString(String value) {
        return AccountType.valueOf(value.toUpperCase());
    }
}
