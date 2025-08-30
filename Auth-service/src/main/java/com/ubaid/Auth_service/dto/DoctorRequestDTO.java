package com.ubaid.Auth_service.dto;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorRequestDTO {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String licenseNumber;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private boolean isAvailable;
    private Set<Long> departmentIds;
    private boolean isActive;
}
