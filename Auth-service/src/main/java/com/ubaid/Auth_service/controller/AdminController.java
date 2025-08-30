package com.ubaid.Auth_service.controller;

import com.ubaid.Auth_service.dto.AdminRequestDTO;
import com.ubaid.Auth_service.dto.AdminResponseDTO;
import com.ubaid.Auth_service.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ADMIN create or update own profile
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public AdminResponseDTO createOrUpdate(@Valid @RequestBody AdminRequestDTO dto) {
        return adminService.createOrUpdateAdmin(dto);
    }

    // ADMIN update own record
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> updateAdmin(@PathVariable Long id, @Valid @RequestBody AdminRequestDTO requestDTO) {
        return ResponseEntity.ok(adminService.updateAdmin(id, requestDTO));
    }

    // ADMIN read any admin (self or others)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> getAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getAdmin(id));
    }

    // ADMIN list all admins
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AdminResponseDTO>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    // ADMIN delete admin (only super admin or higher level)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // ADMIN search by department
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/department/{department}")
    public ResponseEntity<List<AdminResponseDTO>> getAdminsByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(adminService.getAdminsByDepartment(department));
    }

    // ADMIN search by admin level
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/level/{adminLevel}")
    public ResponseEntity<List<AdminResponseDTO>> getAdminsByLevel(@PathVariable String adminLevel) {
        return ResponseEntity.ok(adminService.getAdminsByLevel(adminLevel));
    }

    // ADMIN search admins
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<AdminResponseDTO>> searchAdmins(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String adminLevel) {
        return ResponseEntity.ok(adminService.searchAdmins(name, department, adminLevel));
    }

    // ADMIN get own profile
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/profile")
    public ResponseEntity<AdminResponseDTO> getOwnProfile() {
        return ResponseEntity.ok(adminService.getAdminProfile());
    }
}
