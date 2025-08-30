package com.ubaid.Auth_service.dto;
import lombok.*;

import java.util.Set;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentRequestDTO {

    private String departmentName;

    private String departmentCode;

    private String description;

    private Long headOfDepartmentId; // Doctor id

    private Set<Long> doctorIds; // For assigning doctors

    private String location;

    private String contactNumber;

    private boolean isActive;
}
