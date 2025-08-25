package com.ubaid.Auth_service.security;

import com.ubaid.Auth_service.dto.LoginRequestDto;
import com.ubaid.Auth_service.dto.LoginResponseDto;
import com.ubaid.Auth_service.dto.SignUpRequestDto;
import com.ubaid.Auth_service.dto.SignupResponseDto;
import com.ubaid.Auth_service.entity.Patient;
import com.ubaid.Auth_service.entity.User;
import com.ubaid.Auth_service.entity.type.AuthProviderType;
import com.ubaid.Auth_service.entity.type.RoleType;
import com.ubaid.Auth_service.repository.PatientRepository;
import com.ubaid.Auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.text.Normalizer;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientRepository patientRepository;


    private String generateBasePatientIdFromUsername(String username) {
        String date = "00000000"; // no DOB available in signup; keep 8 zeros
        String cleanUser = sanitizeUsername(username);
        return "PAT-" + date + "-" + cleanUser;
    }

    private String sanitizeUsername(String input) {
        if (input == null) return "USER";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFKD)
                .replaceAll("[^\\p{ASCII}]", "");
        String alnum = normalized.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (alnum.isBlank()) return "USER";
        return alnum.length() > 16 ? alnum.substring(0, 16) : alnum;
    }

    private String generateUniquePatientIdFromUsername(String username) {
        String base = generateBasePatientIdFromUsername(username);
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


    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String token = authUtil.generateAccessToken(user);
        return new LoginResponseDto(token, user.getId());
    }

    public User signUpInternal(SignUpRequestDto signupRequestDto, AuthProviderType authProviderType, String providerId) {
        userRepository.findByUsername(signupRequestDto.getUsername()).ifPresent(u -> {
            throw new IllegalArgumentException("Username already taken");
        });
        if (signupRequestDto.getEmail() != null && !signupRequestDto.getEmail().isBlank()) {
            userRepository.findByEmail(signupRequestDto.getEmail()).ifPresent(u -> {
                throw new IllegalArgumentException("Email already registered");
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
                .lastName(signupRequestDto.getLastName())
                .providerId(providerId)
                .providerType(authProviderType)
                .roles(roles)
                .build();

        if (authProviderType == AuthProviderType.EMAIL) {
            if (signupRequestDto.getPassword() == null || signupRequestDto.getPassword().isBlank()) {
                throw new IllegalArgumentException("Password is required for EMAIL sign up");
            }
            user.setPassword(passwordEncoder.encode(signupRequestDto.getPassword()));
        }

        user = userRepository.save(user);

        // NEW: generate patientId using username
        String patientId = generateUniquePatientIdFromUsername(user.getUsername());

        Patient patient = Patient.builder()
                .patientId(patientId) // IMPORTANT
                .firstName(signupRequestDto.getFirstName())
                .lastName(signupRequestDto.getLastName())
                .email(user.getEmail()) // mirror User.email
                .user(user)
                .active(user.getEmail() != null && !user.getEmail().isBlank())
                .build();

        patientRepository.save(patient);

        return user;
    }



    // Public signup controller entry
    public SignupResponseDto signup(SignUpRequestDto signupRequestDto) {
        // For normal signup, enforce email format if you need it:
        // Validate email if provided
        User user = signUpInternal(signupRequestDto, AuthProviderType.EMAIL, null);
        return new SignupResponseDto(user.getId(), user.getUsername());
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
            // Create new user via OAuth2
            String derivedUsername = authUtil.determineUsernameFromOAuth2User(oAuth2User, registrationId, providerId);

            SignUpRequestDto signUp = new SignUpRequestDto();
            signUp.setUsername(derivedUsername);
            signUp.setPassword(null);
            signUp.setFirstName(firstName);
            signUp.setLastName(lastName);
            signUp.setEmail(email);
            signUp.setRoles(Set.of(RoleType.PATIENT));

            user = signUpInternal(signUp, providerType, providerId); // signUpInternal now saves names on User

        } else if (user != null) {
            boolean updated = false;

            // Update email if provided and changed (with uniqueness check)
            if (email != null && !email.isBlank() && (user.getEmail() == null || !email.equals(user.getEmail()))) {
                Optional<User> existingWithEmail = userRepository.findByEmail(email);
                if (existingWithEmail.isPresent() && !existingWithEmail.get().getId().equals(user.getId())) {
                    throw new BadCredentialsException("This email is already registered with another account");
                }
                user.setEmail(email);
                updated = true;
            }

            // NEW: Update names if provider returns them and they changed
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

                // Keep Patient in sync (optional, recommended)
                Optional<Patient> patientOpt = patientRepository.findByUser(user);
                if (patientOpt.isPresent()) {
                    Patient p = patientOpt.get();
                    // Mirror email if policy requires it
                    p.setEmail(user.getEmail());
                    // Mirror names too
                    if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
                        p.setFirstName(user.getFirstName());
                    }
                    if (user.getLastName() != null && !user.getLastName().isBlank()) {
                        p.setLastName(user.getLastName());
                    }
                    if (user.getEmail() != null && !user.getEmail().isBlank()) {
                        p.setActive(true);
                    }
                    patientRepository.save(p);
                }
            }
        } else {
            // user == null but emailUser != null (email collision with different provider)
            throw new BadCredentialsException("This email is already registered with provider " + emailUser.getProviderType());
        }

        LoginResponseDto body = new LoginResponseDto(authUtil.generateAccessToken(user), user.getId());
        return ResponseEntity.ok(body);
    }



}
