package com.ubaid.Auth_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String departmentName;

    @Column(unique = true)
    private String departmentCode;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_of_department_id")
    @ToString.Exclude
    private Doctor headOfDepartment;

    private String location;
    private String contactNumber;
    private boolean isActive;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "my_dpt_doctors",
            joinColumns = @JoinColumn(name = "dpt_id"),
            inverseJoinColumns = @JoinColumn(name = "doctor_id")
    )
    @ToString.Exclude
    @JsonIgnore
    private Set<Doctor> doctors = new HashSet<>();

    // Helper methods for bidirectional sync
    public void addDoctor(Doctor doctor) {
        this.doctors.add(doctor);
        doctor.getDepartments().add(this);
    }

    public void removeDoctor(Doctor doctor) {
        this.doctors.remove(doctor);
        doctor.getDepartments().remove(this);
    }
}
