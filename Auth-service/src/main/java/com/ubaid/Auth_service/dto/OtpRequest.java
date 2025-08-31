package com.ubaid.Auth_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OtpRequest {
    @NotNull(message="Provide phone number")
    private String phone;
}
