package com.ubaid.Auth_service.service;

import com.ubaid.Auth_service.dto.PatientRequestDTO;
import com.ubaid.Auth_service.dto.PatientResponseDTO;
import com.ubaid.Auth_service.entity.Patient;
import com.ubaid.Auth_service.entity.User;
import com.ubaid.Auth_service.repository.PatientRepository;
import com.ubaid.Auth_service.repository.UserRepository;
import com.ubaid.Auth_service.security.SecurityUserUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientIdGenerator patientIdGenerator;

    // Upsert: create if missing, otherwise update existing (patientId remains immutable)
    @PreAuthorize("hasRole('PATIENT')")
    public PatientResponseDTO createOrUpdatePatient(PatientRequestDTO requestDTO) {
        Long currentUserId = SecurityUserUtil.currentUserId();
        String currentUsername = SecurityUserUtil.currentUsername();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + currentUserId));

        Optional<Patient> existingOpt = patientRepository.findByUserId(currentUserId);

        if (existingOpt.isEmpty()) {
            // Create new
            String generatedId = generateUniquePatientId(requestDTO.getDateOfBirth(), currentUsername);

            // Sync to User first (names/email with uniqueness)
            syncUserFromPatientEdits(user, requestDTO);

            Patient patient = new Patient();
            patient.setUser(user);
            patient.setPatientId(generatedId);

            // Partial mapping from DTO (will not null-out missing fields)
            mapToPatientEntityPartial(patient, requestDTO);

            // If DTO omitted email, mirror from user
            if ((patient.getEmail() == null || patient.getEmail().isBlank()) && user.getEmail() != null) {
                patient.setEmail(user.getEmail());
            }

            try {
                patient = patientRepository.save(patient);
            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate constraint violation while creating patient", ex);
            }

            return mapToPatientResponseDTO(patient);

        } else {
            // Update existing; do not modify patientId
            Patient patient = existingOpt.get();

            // Sync to User first (names/email with uniqueness)
            syncUserFromPatientEdits(user, requestDTO);

            // Apply partial updates
            mapToPatientEntityPartial(patient, requestDTO);

            // Optional policy: only align patient.email with user.email if DTO omitted email
            if (requestDTO.getEmail() == null && user.getEmail() != null && !user.getEmail().isBlank()) {
                patient.setEmail(user.getEmail());
            }

            try {
                patient = patientRepository.save(patient);
            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate constraint violation while updating patient", ex);
            }

            return mapToPatientResponseDTO(patient);
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public PatientResponseDTO getPatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with ID: " + id));
        return mapToPatientResponseDTO(patient);
    }

    // PATIENT updates own profile by DB id; patientId remains immutable
    @PreAuthorize("hasRole('PATIENT')")
    public PatientResponseDTO updatePatient(Long id, PatientRequestDTO requestDTO) {
        Long currentUserId = SecurityUserUtil.currentUserId();

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with ID: " + id));

        if (!patient.getUser().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify another user's patient profile");
        }

        User user = patient.getUser();

        // Sync to User first (email uniqueness, names)
        syncUserFromPatientEdits(user, requestDTO);

        // Partial update on Patient
        mapToPatientEntityPartial(patient, requestDTO);

        // Optional: align Patient.email with User.email when DTO omitted email
        if (requestDTO.getEmail() == null && user.getEmail() != null && !user.getEmail().isBlank()) {
            patient.setEmail(user.getEmail());
        }

        try {
            patient = patientRepository.save(patient);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate constraint violation while updating patient", ex);
        }

        return mapToPatientResponseDTO(patient);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with ID: " + id);
        }
        patientRepository.deleteById(id);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public List<PatientResponseDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::mapToPatientResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public PatientResponseDTO getPatientByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient name cannot be null or empty");
        }
        Patient patient = patientRepository.findByName(name.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with name: " + name));
        return mapToPatientResponseDTO(patient);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @PreAuthorize("hasRole('ADMIN')")
    public List<PatientResponseDTO> searchPatientsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllPatients();
        }
        return patientRepository.findByNameContaining(name.trim()).stream()
                .map(this::mapToPatientResponseDTO)
                .collect(Collectors.toList());
    }

    // Generate unique patientId once (DOB + username if DOB provided; your generator handles null DOB)
    private String generateUniquePatientId(LocalDate dob, String username) {
        String base = patientIdGenerator.baseId(dob, username);
        String candidate = base;
        int i = 1;
        while (patientRepository.existsByPatientId(candidate)) {
            candidate = base + "-" + (++i);
            if (i > 50) {
                candidate = base + "-" + System.currentTimeMillis();
                break;
            }
        }
        return candidate;
    }

    // Partial mapping: apply only non-null values from DTO
    private void mapToPatientEntityPartial(Patient patient, PatientRequestDTO dto) {
        if (dto.getFirstName() != null) patient.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) patient.setLastName(dto.getLastName());
        if (dto.getEmail() != null) patient.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) patient.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getDateOfBirth() != null) patient.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) patient.setGender(dto.getGender());
        if (dto.getBloodGroup() != null) patient.setBloodGroup(dto.getBloodGroup());
        if (dto.getEmergencyContact() != null) patient.setEmergencyContact(dto.getEmergencyContact());
        if (dto.getEmergencyContactRelation() != null) patient.setEmergencyContactRelation(dto.getEmergencyContactRelation());
        if (dto.getStreetAddress() != null) patient.setStreetAddress(dto.getStreetAddress());
        if (dto.getCity() != null) patient.setCity(dto.getCity());
        if (dto.getState() != null) patient.setState(dto.getState());
        if (dto.getPostalCode() != null) patient.setPostalCode(dto.getPostalCode());
        if (dto.getCountry() != null) patient.setCountry(dto.getCountry());
        if (dto.getActive() != null) patient.setActive(dto.getActive());
    }

    private PatientResponseDTO mapToPatientResponseDTO(Patient patient) {
        PatientResponseDTO responseDTO = new PatientResponseDTO();
        responseDTO.setId(patient.getId());
        if (patient.getUser() != null) {
            responseDTO.setUsername(patient.getUser().getUsername());
            if (patient.getUser().getRoles() != null) {
                responseDTO.setRoles(patient.getUser().getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(",")));
            }
        }
        responseDTO.setPatientId(patient.getPatientId());
        responseDTO.setFirstName(patient.getFirstName());
        responseDTO.setLastName(patient.getLastName());
        responseDTO.setPhoneNumber(patient.getPhoneNumber());
        responseDTO.setDateOfBirth(patient.getDateOfBirth());
        responseDTO.setGender(patient.getGender());
        responseDTO.setBloodGroup(patient.getBloodGroup() != null ? patient.getBloodGroup().toString() : null);
        responseDTO.setEmergencyContact(patient.getEmergencyContact());
        responseDTO.setEmergencyContactRelation(patient.getEmergencyContactRelation());
        responseDTO.setStreetAddress(patient.getStreetAddress());
        responseDTO.setCity(patient.getCity());
        responseDTO.setState(patient.getState());
        responseDTO.setPostalCode(patient.getPostalCode());
        responseDTO.setCountry(patient.getCountry());
        responseDTO.setActive(patient.isActive());
        responseDTO.setCreatedAt(patient.getCreatedAt());
        responseDTO.setUpdatedAt(patient.getUpdatedAt());
        return responseDTO;
    }

    // Synchronize edits from Patient DTO to User (email uniqueness + optional names)
    private void syncUserFromPatientEdits(User user, PatientRequestDTO requestDTO) {
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

        String newEmail = requestDTO.getEmail();
        if (newEmail != null && !newEmail.isBlank() && !newEmail.equals(user.getEmail())) {
            userRepository.findByEmail(newEmail).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
                }
            });
            user.setEmail(newEmail);
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }
    }
}
