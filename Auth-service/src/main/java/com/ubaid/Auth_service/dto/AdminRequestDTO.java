package com.ubaid.Auth_service.dto;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminRequestDTO {

    private String adminId;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String department;

    private String adminLevel;

    private boolean isActive;
}
