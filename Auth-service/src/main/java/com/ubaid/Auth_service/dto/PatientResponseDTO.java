package com.ubaid.Auth_service.dto;

import lombok.*;
import java.sql.Timestamp;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatientResponseDTO {
    private Long id;
    private String username;
    private String roles;
    private String patientId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String emergencyContact;
    private String emergencyContactRelation;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
