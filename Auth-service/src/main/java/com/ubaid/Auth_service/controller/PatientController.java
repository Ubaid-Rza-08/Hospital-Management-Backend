package com.ubaid.Auth_service.controller;

import com.ubaid.Auth_service.dto.PatientRequestDTO;
import com.ubaid.Auth_service.dto.PatientResponseDTO;
import com.ubaid.Auth_service.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // PATIENT create (user from token; patientId generated)

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public PatientResponseDTO createOrUpdate(@Valid @RequestBody PatientRequestDTO dto) {
        return patientService.createOrUpdatePatient(dto);
    }


    // PATIENT update own record
    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientRequestDTO requestDTO) {
        return ResponseEntity.ok(patientService.updatePatient(id, requestDTO));
    }

    // ADMIN read
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatient(id));
    }

    // ADMIN list
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    // ADMIN delete
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    // Optional admin-only name queries
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/name/{name}")
    public ResponseEntity<PatientResponseDTO> getPatientByName(@PathVariable String name) {
        return ResponseEntity.ok(patientService.getPatientByName(name));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<PatientResponseDTO>> searchPatients(@RequestParam(required = false) String name) {
        return ResponseEntity.ok(patientService.searchPatientsByName(name));
    }
}
