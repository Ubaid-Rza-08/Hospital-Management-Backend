package com.ubaid.Auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@Builder
public class LoginResponseDto {
    private String jwt;
    private Long userId;
    private String username;
    private Set<String> roles;


    // Error constructor for failed logins
    public LoginResponseDto(String errorMessage, Long userId, String username, Set<String> roles) {
        this.jwt = errorMessage;
        this.userId = userId;
        this.username = username;
        this.roles = roles;
    }
}

