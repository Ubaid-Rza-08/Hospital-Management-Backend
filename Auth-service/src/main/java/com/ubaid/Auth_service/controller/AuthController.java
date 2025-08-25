package com.ubaid.Auth_service.controller;


import com.ubaid.Auth_service.dto.LoginRequestDto;
import com.ubaid.Auth_service.dto.LoginResponseDto;
import com.ubaid.Auth_service.dto.SignUpRequestDto;
import com.ubaid.Auth_service.dto.SignupResponseDto;
import com.ubaid.Auth_service.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto responseDto = authService.login(loginRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody SignUpRequestDto signUpRequestDto) {
        SignupResponseDto responseDto = authService.signup(signUpRequestDto);
        return ResponseEntity.ok(responseDto);
    }
}
