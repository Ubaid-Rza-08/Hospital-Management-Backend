package com.ubaid.Auth_service.dto;
import lombok.*;

import java.sql.Timestamp;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentResponseDTO {

    private Long id;

    private String departmentName;

    private String departmentCode;

    private String description;

    private Long headOfDepartmentId; // or DoctorResponseDTO

    private Set<Long> doctorIds; // or Set<DoctorResponseDTO>

    private String location;

    private String contactNumber;

    private boolean isActive;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}
