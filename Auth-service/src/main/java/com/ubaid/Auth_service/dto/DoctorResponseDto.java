package com.ubaid.Auth_service.dto;
import com.ubaid.Auth_service.entity.type.RoleType;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponseDto {
    private Long id;
    private String doctorId;
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String licenseNumber;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private boolean isAvailable;
    private boolean isActive;
    private Set<RoleType> userRoles;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
