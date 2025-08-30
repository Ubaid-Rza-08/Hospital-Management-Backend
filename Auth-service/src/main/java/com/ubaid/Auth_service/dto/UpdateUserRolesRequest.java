package com.ubaid.Auth_service.dto;

import com.ubaid.Auth_service.entity.type.RoleType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRolesRequest {

    @NotNull(message = "Roles cannot be null")
    @NotEmpty(message = "At least one role must be specified")
    private Set<RoleType> roles;
}
