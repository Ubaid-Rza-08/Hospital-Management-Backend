package com.ubaid.Auth_service.security;

import com.ubaid.Auth_service.dto.*;
import com.ubaid.Auth_service.entity.*;
import com.ubaid.Auth_service.entity.type.AuthProviderType;
import com.ubaid.Auth_service.entity.type.RoleType;
import com.ubaid.Auth_service.repository.*;
import com.ubaid.Auth_service.security.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AdminRepository adminRepository;

    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        try {
            log.info("Attempting authentication for user: {}", loginRequestDto.getUsername());

            // Check if user exists first
            User user = userRepository.findByUsername(loginRequestDto.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + loginRequestDto.getUsername()));

            log.info("User found: {} with roles: {}", user.getUsername(), user.getRoles());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUsername(),
                            loginRequestDto.getPassword()
                    )
            );

            User authenticatedUser = (User) authentication.getPrincipal();
            String token = authUtil.generateAccessToken(authenticatedUser);

            // Convert roles to Set<String> - FIXED
            Set<String> roles = authenticatedUser.getRoles().stream()
                    .map(RoleType::name)
                    .collect(Collectors.toSet());

            log.info("Login successful for user: {} with roles: {}", authenticatedUser.getUsername(), roles);

            return LoginResponseDto.builder()
                    .jwt(token)
                    .userId(authenticatedUser.getId())
                    .username(authenticatedUser.getUsername())
                    .roles(roles)
                    .build();

        } catch (Exception e) {
            log.error("Login failed for user {}: {}", loginRequestDto.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    @Transactional
    public UserResponseDto signup(SignUpRequestDto signupRequestDto) {
        log.info("Signup attempt for user: {}", signupRequestDto.getUsername());

        User user = signUpInternal(signupRequestDto, AuthProviderType.EMAIL, null);

        log.info("Signup successful for user: {}", user.getUsername());
        return mapUserToResponseDto(user);
    }

    @Transactional
    public User signUpInternal(SignUpRequestDto signupRequestDto, AuthProviderType authProviderType, String providerId) {
        // Check if username already exists
        userRepository.findByUsername(signupRequestDto.getUsername())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("Username already taken: " + signupRequestDto.getUsername());
                });

        // Check if email already exists
        if (signupRequestDto.getEmail() != null && !signupRequestDto.getEmail().isBlank()) {
            userRepository.findByEmail(signupRequestDto.getEmail())
                    .ifPresent(u -> {
                        throw new IllegalArgumentException("Email already registered: " + signupRequestDto.getEmail());
                    });
        }

        Set<RoleType> roles = signupRequestDto.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = Set.of(RoleType.PATIENT);
        }

        User user = User.builder()
                .username(signupRequestDto.getUsername())
                .email(signupRequestDto.getEmail())
                .firstName(signupRequestDto.getFirstName())
                .phone(signupRequestDto.getPhone())
                .lastName(signupRequestDto.getLastName())
                .providerId(providerId)
                .providerType(authProviderType)
                .roles(new HashSet<>(roles))
                .build();

        if (authProviderType == AuthProviderType.EMAIL) {
            if (signupRequestDto.getPassword() == null || signupRequestDto.getPassword().isBlank()) {
                throw new IllegalArgumentException("Password is required for EMAIL sign up");
            }
            user.setPassword(passwordEncoder.encode(signupRequestDto.getPassword()));
        }

        user = userRepository.save(user);

        // Create role-specific entities
        if (roles.contains(RoleType.PATIENT)) {
            createPatientEntity(user);
        }
        if (roles.contains(RoleType.DOCTOR)) {
            createDoctorEntity(user);
        }
        if (roles.contains(RoleType.ADMIN)) {
            createAdminEntity(user);
        }

        return user;
    }

    // Entity creation methods
    private void createPatientEntity(User user) {
        String patientId = generatePatientId(user.getUsername());

        Patient patient = Patient.builder()
                .user(user)
                .patientId(patientId)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .active(true)
                .build();

        patientRepository.save(patient);
        log.info("Created patient entity with ID: {}", patientId);
    }

    private void createDoctorEntity(User user) {
        String doctorId = generateDoctorId(user.getUsername());

        Doctor doctor = Doctor.builder()
                .user(user)
                .doctorId(doctorId)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(true)
                .isAvailable(true)
                .build();

        doctorRepository.save(doctor);
        log.info("Created doctor entity with ID: {}", doctorId);
    }

    private void createAdminEntity(User user) {
        String adminId = generateAdminId(user.getUsername());

        Admin admin = Admin.builder()
                .user(user)
                .adminId(adminId)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(true)
                .build();

        adminRepository.save(admin);
        log.info("Created admin entity with ID: {}", adminId);
    }

    // ID generation methods
    private String generatePatientId(String username) {
        return "PAT-" + sanitizeUsername(username).toUpperCase() + "-" + System.currentTimeMillis();
    }

    private String generateDoctorId(String username) {
        return "DOC-" + sanitizeUsername(username).toUpperCase() + "-" + System.currentTimeMillis();
    }

    private String generateAdminId(String username) {
        return "ADM-" + sanitizeUsername(username).toUpperCase() + "-" + System.currentTimeMillis();
    }

    private String sanitizeUsername(String username) {
        if (username == null || username.isBlank()) return "USER";
        return username.replaceAll("[^A-Za-z0-9]", "").substring(0, Math.min(username.length(), 10));
    }

    // Mapping methods
    private UserResponseDto mapUserToResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .build();
    }

    @Transactional
    public ResponseEntity<LoginResponseDto> handleOAuth2LoginRequest(OAuth2User oAuth2User, String registrationId) {
        AuthProviderType providerType = authUtil.getProviderTypeFromRegistrationId(registrationId);
        String providerId = authUtil.determineProviderIdFromOAuth2User(oAuth2User, registrationId);

        User user = userRepository.findByProviderIdAndProviderType(providerId, providerType).orElse(null);
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (name == null) {
            String login = oAuth2User.getAttribute("login");
            name = login != null ? login : providerId;
        }

        String firstName = "";
        String lastName = "";
        if (name != null && !name.isBlank()) {
            String[] parts = name.trim().split("\\s+", 2);
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : "";
        }

        User emailUser = (email != null && !email.isBlank())
                ? userRepository.findByEmail(email).orElse(null)
                : null;

        if (user == null && emailUser == null) {
            String derivedUsername = authUtil.determineUsernameFromOAuth2User(oAuth2User, registrationId, providerId);

            SignUpRequestDto signUp = SignUpRequestDto.builder()
                    .username(derivedUsername)
                    .password(null)
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .roles(Set.of(RoleType.PATIENT))
                    .build();

            user = signUpInternal(signUp, providerType, providerId);

        } else if (user != null) {
            updateUserFromOAuth2(user, email, firstName, lastName);
        } else {
            throw new BadCredentialsException("This email is already registered with provider " + emailUser.getProviderType());
        }

        Set<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        LoginResponseDto body = LoginResponseDto.builder()
                .jwt(authUtil.generateAccessToken(user))
                .userId(user.getId())
                .username(user.getUsername())
                .roles(roleNames)
                .build();

        return ResponseEntity.ok(body);
    }

    private void updateUserFromOAuth2(User user, String email, String firstName, String lastName) {
        boolean updated = false;

        if (email != null && !email.isBlank() && (user.getEmail() == null || !email.equals(user.getEmail()))) {
            Optional<User> existingWithEmail = userRepository.findByEmail(email);
            if (existingWithEmail.isPresent() && !existingWithEmail.get().getId().equals(user.getId())) {
                throw new BadCredentialsException("This email is already registered with another account");
            }
            user.setEmail(email);
            updated = true;
        }

        if (firstName != null && !firstName.isBlank() && !firstName.equals(user.getFirstName())) {
            user.setFirstName(firstName);
            updated = true;
        }
        if (lastName != null && !lastName.isBlank() && !lastName.equals(user.getLastName())) {
            user.setLastName(lastName);
            updated = true;
        }

        if (updated) {
            userRepository.save(user);
        }
    }

    public UserResponseDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapUserToResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUserRoles(Long id, UpdateUserRolesRequest request) {
        log.info("Updating roles for user ID: {} to: {}", id, request.getRoles());

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setRoles(request.getRoles());
        User updatedUser = userRepository.save(user);

        log.info("Successfully updated user roles for ID: {}", id);
        return mapUserToResponseDto(updatedUser);
    }
}
