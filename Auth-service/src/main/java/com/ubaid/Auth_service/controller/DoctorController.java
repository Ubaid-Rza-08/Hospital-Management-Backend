package com.ubaid.Auth_service.controller;

import com.ubaid.Auth_service.dto.DoctorRequestDTO;
import com.ubaid.Auth_service.dto.DoctorResponseDto;
import com.ubaid.Auth_service.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    // DOCTOR create or update own profile
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping
    public DoctorResponseDto createOrUpdate(@Valid @RequestBody DoctorRequestDTO dto) {
        return doctorService.createOrUpdateDoctor(dto);
    }

    // DOCTOR update own record
    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponseDto> updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorRequestDTO requestDTO) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, requestDTO));
    }

    // ADMIN read any doctor
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDto> getDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctor(id));
    }

    // ADMIN list all doctors
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<DoctorResponseDto>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    // ADMIN delete doctor
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    // ADMIN search by license number
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/license/{licenseNumber}")
    public ResponseEntity<DoctorResponseDto> getDoctorByLicense(@PathVariable String licenseNumber) {
        return ResponseEntity.ok(doctorService.getDoctorByLicenseNumber(licenseNumber));
    }

    // ADMIN search by specialization
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<DoctorResponseDto>> getDoctorsBySpecialization(@PathVariable String specialization) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(specialization));
    }

    // ADMIN search doctors
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<DoctorResponseDto>> searchDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Boolean isAvailable) {
        return ResponseEntity.ok(doctorService.searchDoctors(name, specialization, isAvailable));
    }

    // DOCTOR get own profile
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/profile")
    public ResponseEntity<DoctorResponseDto> getOwnProfile() {
        return ResponseEntity.ok(doctorService.getDoctorProfile());
    }
}
