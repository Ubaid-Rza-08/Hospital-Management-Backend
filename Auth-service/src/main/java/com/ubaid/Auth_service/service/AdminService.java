package com.ubaid.Auth_service.service;

import com.ubaid.Auth_service.dto.AdminRequestDTO;
import com.ubaid.Auth_service.dto.AdminResponseDTO;
import com.ubaid.Auth_service.entity.Admin;
import com.ubaid.Auth_service.entity.User;
import com.ubaid.Auth_service.repository.AdminRepository;
import com.ubaid.Auth_service.repository.UserRepository;
import com.ubaid.Auth_service.security.SecurityUserUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final AdminIdGenerator adminIdGenerator;

    @PreAuthorize("hasRole('ADMIN')")
    public AdminResponseDTO createOrUpdateAdmin(AdminRequestDTO requestDTO) {
        Long currentUserId = SecurityUserUtil.currentUserId();
        String currentUsername = SecurityUserUtil.currentUsername();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + currentUserId));

        Optional<Admin> existingOpt = adminRepository.findById(currentUserId);

        if (existingOpt.isEmpty()) {
            // Create new admin
            String generatedId = generateUniqueAdminId(requestDTO.getDepartment(), currentUsername);

            // Sync to User first
            syncUserFromAdminEdits(user, requestDTO);

            Admin admin = new Admin();
            admin.setId(currentUserId); // Shared primary key
            admin.setUser(user);
            admin.setAdminId(generatedId);

            // Map from DTO
            mapToAdminEntityPartial(admin, requestDTO);

            try {
                admin = adminRepository.save(admin);
            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate constraint violation while creating admin", ex);
            }

            return mapToAdminResponseDTO(admin);

        } else {
            // Update existing admin
            Admin admin = existingOpt.get();

            // Sync to User first
            syncUserFromAdminEdits(user, requestDTO);

            // Apply partial updates (don't change adminId once set)
            mapToAdminEntityPartial(admin, requestDTO);

            try {
                admin = adminRepository.save(admin);
            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate constraint violation while updating admin", ex);
            }

            return mapToAdminResponseDTO(admin);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AdminResponseDTO updateAdmin(Long id, AdminRequestDTO requestDTO) {
        Long currentUserId = SecurityUserUtil.currentUserId();

        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found with ID: " + id));

        // Allow admin to update own profile or implement level-based authorization
        if (!admin.getId().equals(currentUserId)) {
            // You can add additional authorization logic here based on admin levels
            // For now, allowing any admin to update any admin profile
        }

        User user = admin.getUser();

        // Sync to User first
        syncUserFromAdminEdits(user, requestDTO);

        // Partial update on Admin
        mapToAdminEntityPartial(admin, requestDTO);

        try {
            admin = adminRepository.save(admin);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate constraint violation while updating admin", ex);
        }

        return mapToAdminResponseDTO(admin);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public AdminResponseDTO getAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found with ID: " + id));
        return mapToAdminResponseDTO(admin);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAdmin(Long id) {
        Long currentUserId = SecurityUserUtil.currentUserId();

        if (currentUserId.equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete your own admin account");
        }

        if (!adminRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found with ID: " + id);
        }

        adminRepository.deleteById(id);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminResponseDTO> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(this::mapToAdminResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminResponseDTO> getAdminsByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department cannot be null or empty");
        }
        return adminRepository.findByDepartmentContainingIgnoreCase(department.trim()).stream()
                .map(this::mapToAdminResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminResponseDTO> getAdminsByLevel(String adminLevel) {
        if (adminLevel == null || adminLevel.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin level cannot be null or empty");
        }
        return adminRepository.findByAdminLevel(adminLevel.trim()).stream()
                .map(this::mapToAdminResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminResponseDTO> searchAdmins(String name, String department, String adminLevel) {
        return adminRepository.searchAdmins(name, department, adminLevel).stream()
                .map(this::mapToAdminResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public AdminResponseDTO getAdminProfile() {
        Long currentUserId = SecurityUserUtil.currentUserId();
        Admin admin = adminRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin profile not found"));
        return mapToAdminResponseDTO(admin);
    }

    // Generate unique adminId
    private String generateUniqueAdminId(String department, String username) {
        String base = adminIdGenerator.baseId(department, username);
        String candidate = base;
        int i = 1;
        while (adminRepository.existsByAdminId(candidate)) {
            candidate = base + "-" + (++i);
            if (i > 50) {
                candidate = base + "-" + System.currentTimeMillis();
                break;
            }
        }
        return candidate;
    }

    // Partial mapping from DTO to entity
    private void mapToAdminEntityPartial(Admin admin, AdminRequestDTO dto) {
        if (dto.getFirstName() != null) admin.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) admin.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null) admin.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getDepartment() != null) admin.setDepartment(dto.getDepartment());
        if (dto.getAdminLevel() != null) admin.setAdminLevel(dto.getAdminLevel());
        admin.setActive(dto.isActive());
    }

    private AdminResponseDTO mapToAdminResponseDTO(Admin admin) {
        AdminResponseDTO responseDTO = new AdminResponseDTO();
        responseDTO.setId(admin.getId());
        if (admin.getUser() != null) {
            responseDTO.setUsername(admin.getUser().getUsername());
            responseDTO.setRoles(admin.getUser().getRoles());
        }
        responseDTO.setAdminId(admin.getAdminId());
        responseDTO.setFirstName(admin.getFirstName());
        responseDTO.setLastName(admin.getLastName());
        responseDTO.setPhoneNumber(admin.getPhoneNumber());
        responseDTO.setDepartment(admin.getDepartment());
        responseDTO.setAdminLevel(admin.getAdminLevel());
        responseDTO.setActive(admin.isActive());
        responseDTO.setCreatedAt(admin.getCreatedAt() != null ? Timestamp.valueOf(admin.getCreatedAt()) : null);
        responseDTO.setUpdatedAt(admin.getUpdatedAt() != null ? Timestamp.valueOf(admin.getUpdatedAt()) : null);
        return responseDTO;
    }

    // Synchronize edits from Admin DTO to User
    private void syncUserFromAdminEdits(User user, AdminRequestDTO requestDTO) {
        boolean changed = false;

        if (requestDTO.getFirstName() != null && !requestDTO.getFirstName().isBlank()
                && !requestDTO.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(requestDTO.getFirstName());
            changed = true;
        }

        if (requestDTO.getLastName() != null && !requestDTO.getLastName().isBlank()
                && !requestDTO.getLastName().equals(user.getLastName())) {
            user.setLastName(requestDTO.getLastName());
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }
    }
}
