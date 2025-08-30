package com.ubaid.Auth_service.service;

import com.ubaid.Auth_service.dto.DoctorRequestDTO;
import com.ubaid.Auth_service.dto.DoctorResponseDto;
import com.ubaid.Auth_service.entity.Doctor;
import com.ubaid.Auth_service.entity.User;
import com.ubaid.Auth_service.entity.Department;
import com.ubaid.Auth_service.repository.DepartmentRepository;
import com.ubaid.Auth_service.repository.DoctorRepository;
import com.ubaid.Auth_service.repository.UserRepository;
import com.ubaid.Auth_service.security.SecurityUserUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorIdGenerator doctorIdGenerator;

    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorResponseDto createOrUpdateDoctor(DoctorRequestDTO requestDTO) {
        Long currentUserId = SecurityUserUtil.currentUserId();
        String currentUsername = SecurityUserUtil.currentUsername();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + currentUserId));

        Optional<Doctor> existingOpt = doctorRepository.findByUserId(currentUserId);

        if (existingOpt.isEmpty()) {
            // Create new doctor
            String generatedId = generateUniqueDoctorId(requestDTO.getLicenseNumber(), currentUsername);

            // Sync to User first
            syncUserFromDoctorEdits(user, requestDTO);

            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setDoctorId(generatedId);

            // Map from DTO
            mapToDoctorEntityPartial(doctor, requestDTO);

            // Handle departments if provided
            if (requestDTO.getDepartmentIds() != null && !requestDTO.getDepartmentIds().isEmpty()) {
                Set<Department> departments = departmentRepository.findAllById(requestDTO.getDepartmentIds())
                        .stream().collect(Collectors.toSet());
                doctor.setDepartments(departments);
            }

            try {
                doctor = doctorRepository.save(doctor);
            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate constraint violation while creating doctor", ex);
            }

            return mapToDoctorResponseDto(doctor);

        } else {
            // Update existing doctor
            Doctor doctor = existingOpt.get();

            // Sync to User first
            syncUserFromDoctorEdits(user, requestDTO);

            // Apply partial updates
            mapToDoctorEntityPartial(doctor, requestDTO);

            // Handle departments update
            if (requestDTO.getDepartmentIds() != null) {
                Set<Department> departments = departmentRepository.findAllById(requestDTO.getDepartmentIds())
                        .stream().collect(Collectors.toSet());
                doctor.setDepartments(departments);
            }

            try {
                doctor = doctorRepository.save(doctor);
            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate constraint violation while updating doctor", ex);
            }

            return mapToDoctorResponseDto(doctor);
        }
    }

    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorResponseDto updateDoctor(Long id, DoctorRequestDTO requestDTO) {
        Long currentUserId = SecurityUserUtil.currentUserId();

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with ID: " + id));

        if (!doctor.getUser().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify another user's doctor profile");
        }

        User user = doctor.getUser();

        // Sync to User first
        syncUserFromDoctorEdits(user, requestDTO);

        // Partial update on Doctor
        mapToDoctorEntityPartial(doctor, requestDTO);

        // Handle departments update
        if (requestDTO.getDepartmentIds() != null) {
            Set<Department> departments = departmentRepository.findAllById(requestDTO.getDepartmentIds())
                    .stream().collect(Collectors.toSet());
            doctor.setDepartments(departments);
        }

        try {
            doctor = doctorRepository.save(doctor);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate constraint violation while updating doctor", ex);
        }

        return mapToDoctorResponseDto(doctor);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorResponseDto getDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with ID: " + id));
        return mapToDoctorResponseDto(doctor);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with ID: " + id);
        }
        doctorRepository.deleteById(id);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public List<DoctorResponseDto> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::mapToDoctorResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorResponseDto getDoctorByLicenseNumber(String licenseNumber) {
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "License number cannot be null or empty");
        }
        Doctor doctor = doctorRepository.findByLicenseNumber(licenseNumber.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with license number: " + licenseNumber));
        return mapToDoctorResponseDto(doctor);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public List<DoctorResponseDto> getDoctorsBySpecialization(String specialization) {
        if (specialization == null || specialization.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Specialization cannot be null or empty");
        }
        return doctorRepository.findBySpecializationContainingIgnoreCase(specialization.trim()).stream()
                .map(this::mapToDoctorResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public List<DoctorResponseDto> searchDoctors(String name, String specialization, Boolean isAvailable) {
        return doctorRepository.searchDoctors(name, specialization, isAvailable).stream()
                .map(this::mapToDoctorResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorResponseDto getDoctorProfile() {
        Long currentUserId = SecurityUserUtil.currentUserId();
        Doctor doctor = doctorRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
        return mapToDoctorResponseDto(doctor);
    }

    // Generate unique doctorId
    private String generateUniqueDoctorId(String licenseNumber, String username) {
        String base = doctorIdGenerator.baseId(licenseNumber, username);
        String candidate = base;
        int i = 1;
        while (doctorRepository.existsByDoctorId(candidate)) {
            candidate = base + "-" + (++i);
            if (i > 50) {
                candidate = base + "-" + System.currentTimeMillis();
                break;
            }
        }
        return candidate;
    }

    // Partial mapping from DTO to entity
    private void mapToDoctorEntityPartial(Doctor doctor, DoctorRequestDTO dto) {
        if (dto.getFirstName() != null) doctor.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) doctor.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null) doctor.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getLicenseNumber() != null) doctor.setLicenseNumber(dto.getLicenseNumber());
        if (dto.getSpecialization() != null) doctor.setSpecialization(dto.getSpecialization());
        if (dto.getQualification() != null) doctor.setQualification(dto.getQualification());
        if (dto.getExperienceYears() != null) doctor.setExperienceYears(dto.getExperienceYears());
        if (dto.getConsultationFee() != null) doctor.setConsultationFee(dto.getConsultationFee());
        doctor.setAvailable(dto.isAvailable());
        doctor.setActive(dto.isActive());
    }

    private DoctorResponseDto mapToDoctorResponseDto(Doctor doctor) {
        DoctorResponseDto responseDto = new DoctorResponseDto();
        responseDto.setId(doctor.getId());
        responseDto.setDoctorId(doctor.getDoctorId());
        if (doctor.getUser() != null) {
            responseDto.setUserId(doctor.getUser().getId());
            responseDto.setUsername(doctor.getUser().getUsername());
            responseDto.setEmail(doctor.getUser().getEmail());
            responseDto.setUserRoles(doctor.getUser().getRoles());
        }
        responseDto.setFirstName(doctor.getFirstName());
        responseDto.setLastName(doctor.getLastName());
        responseDto.setPhoneNumber(doctor.getPhoneNumber());
        responseDto.setLicenseNumber(doctor.getLicenseNumber());
        responseDto.setSpecialization(doctor.getSpecialization());
        responseDto.setQualification(doctor.getQualification());
        responseDto.setExperienceYears(doctor.getExperienceYears());
        responseDto.setConsultationFee(doctor.getConsultationFee());
        responseDto.setAvailable(doctor.isAvailable());
        responseDto.setActive(doctor.isActive());
        responseDto.setCreatedAt(doctor.getCreatedAt());
        responseDto.setUpdatedAt(doctor.getUpdatedAt());
        return responseDto;
    }

    // Synchronize edits from Doctor DTO to User
    private void syncUserFromDoctorEdits(User user, DoctorRequestDTO requestDTO) {
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
