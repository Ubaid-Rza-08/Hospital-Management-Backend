package com.ubaid.Auth_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    @ToString.Exclude
    private User user;

    @Column(unique = true)
    private String doctorId;

    private String firstName;
    private String lastName;
    private String phoneNumber;

    @Column(unique = true)
    private String licenseNumber;

    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private boolean isAvailable;
    private boolean isActive;

    @ManyToMany(mappedBy = "doctors", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private Set<Department> departments = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    // Helper methods for bidirectional sync
    public void addDepartment(Department department) {
        this.departments.add(department);
        department.getDoctors().add(this);
    }

    public void removeDepartment(Department department) {
        this.departments.remove(department);
        department.getDoctors().remove(this);
    }
}
