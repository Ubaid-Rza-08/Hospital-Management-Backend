package com.ubaid.Auth_service.dto;

import com.ubaid.Auth_service.entity.type.BloodGroupType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatientRequestDTO {

    // Removed userId and patientId from client payload. Both are inferred/generated.

    private String firstName;
    private String lastName;
    @Email
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private BloodGroupType bloodGroup;
    private String emergencyContact;
    private String emergencyContactRelation;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean active;
}
