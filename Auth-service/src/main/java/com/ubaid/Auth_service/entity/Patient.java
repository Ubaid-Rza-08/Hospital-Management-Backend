package com.ubaid.Auth_service.entity;

import com.ubaid.Auth_service.entity.type.BloodGroupType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "patients", uniqueConstraints = {
        @UniqueConstraint(name = "uk_patients_user", columnNames = {"user_id"}),
        @UniqueConstraint(name = "uk_patients_patient_id", columnNames = {"patient_id"})
})
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "patient_id", nullable = false, unique = true, updatable = false)
    private String patientId;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String gender;

    @Enumerated(EnumType.STRING)
    private BloodGroupType bloodGroup;

    private String emergencyContact;
    private String emergencyContactRelation;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    @Column(name = "is_active")
    private boolean active;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @Version
    private Long version; // optimistic locking
}
