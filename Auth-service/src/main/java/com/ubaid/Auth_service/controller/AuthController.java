package com.ubaid.Auth_service.controller;

import com.ubaid.Auth_service.dto.*;
import com.ubaid.Auth_service.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        try {
            log.info("Login attempt for user: {}", loginRequestDto.getUsername());
            LoginResponseDto responseDto = authService.login(loginRequestDto);
            log.info("Login successful for user: {} with roles: {}",
                    loginRequestDto.getUsername(), responseDto.getRoles());
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", loginRequestDto.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponseDto("Login failed: " + e.getMessage(), null, null, new HashSet<>()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        try {
            log.info("Signup attempt for user: {}", signUpRequestDto.getUsername());
            UserResponseDto responseDto = authService.signup(signUpRequestDto);
            log.info("Signup successful for user: {}", signUpRequestDto.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalArgumentException e) {
            log.error("Signup failed for user {}: {}", signUpRequestDto.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error during signup for user {}: {}", signUpRequestDto.getUsername(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/getUser")
    public ResponseEntity<UserResponseDto> findById(@RequestParam("id") Long id) {
        try {
            log.info("Fetching user with ID: {}", id);
            UserResponseDto response = authService.findById(id);
            log.info("Successfully fetched user: {} with roles: {}", response.getUsername(), response.getRoles());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("User not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/updateUserRoles/{id}")
    public ResponseEntity<UserResponseDto> updateUserRoles(@PathVariable("id") Long id,
                                                           @Valid @RequestBody UpdateUserRolesRequest request) {
        try {
            UserResponseDto response = authService.updateUserRoles(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error updating user roles for ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
