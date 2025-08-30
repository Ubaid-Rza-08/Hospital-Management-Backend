package com.ubaid.Auth_service.repository;

import com.ubaid.Auth_service.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor,Long> {
    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
    List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);
    boolean existsByDoctorId(String doctorId);
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Doctor d WHERE d.user.id = ?1")
    boolean existsByUser_Id(Long userId);

    @Query("SELECT d FROM Doctor d WHERE " +
            "(:name IS NULL OR CONCAT(d.firstName, ' ', d.lastName) LIKE %:name%) AND " +
            "(:specialization IS NULL OR d.specialization LIKE %:specialization%) AND " +
            "(:isAvailable IS NULL OR d.isAvailable = :isAvailable)")
    List<Doctor> searchDoctors(@Param("name") String name,
                               @Param("specialization") String specialization,
                               @Param("isAvailable") Boolean isAvailable);

}
