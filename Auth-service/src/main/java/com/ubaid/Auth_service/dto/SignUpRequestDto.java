package com.ubaid.Auth_service.dto;

import com.ubaid.Auth_service.entity.type.RoleType;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequestDto {
    private String username;
    private String password;
    private String firstName;
    private String lastName;

    // New: separate email field
    private String email;

    private Set<RoleType> roles = new HashSet<>();
}
