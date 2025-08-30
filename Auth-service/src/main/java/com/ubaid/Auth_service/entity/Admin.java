package com.ubaid.Auth_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "admins")
public class Admin {

    @Id
    @Column(name = "user_id")
    private Long id;

    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @Column(unique = true)
    private String adminId;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String department;

    private String adminLevel;

    private boolean isActive;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // Use LocalDateTime for Hibernate annotations

    @UpdateTimestamp
    private LocalDateTime updatedAt; // Use LocalDateTime for Hibernate annotations
}