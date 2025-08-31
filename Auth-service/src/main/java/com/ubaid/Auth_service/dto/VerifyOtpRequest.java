package com.ubaid.Auth_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotNull(message = "phone number is required")
    private String phone;
    @NotNull(message = "provide otp")
    private String otp;
}
