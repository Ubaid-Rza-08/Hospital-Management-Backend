package com.ubaid.Auth_service.dto;
import com.ubaid.Auth_service.entity.type.RoleType;
import lombok.*;

import java.sql.Timestamp;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminResponseDTO {

    private Long id;

    private String username; // from User

    private Set<RoleType> roles; // from User

    private String adminId;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String department;

    private String adminLevel;

    private boolean isActive;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}
