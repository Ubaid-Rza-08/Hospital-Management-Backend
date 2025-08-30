package com.ubaid.Auth_service.repository;

import com.ubaid.Auth_service.entity.Patient;
import com.ubaid.Auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient,Long> {
    // Add this method to search by full name
//    @Query("SELECT p FROM Patient p WHERE CONCAT(p.firstName, ' ', p.lastName) = :name")
//    Optional<Patient> findByName(@Param("name") String name);

//    List<Patient> findByBirthDateOrEmail(LocalDate birthDate, String email);
//
//    List<Patient> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);

//    List<Patient> findByNameContainingOrderByIdDesc(String query);

//    @Query("SELECT p FROM Patient p where p.bloodGroup = ?1")
//    List<Patient> findByBloodGroup(@Param("bloodGroup") BloodGroupType bloodGroup);
//
//    @Query("select p from Patient p where p.birthDate > :birthDate")
//    List<Patient> findByBornAfterDate(@Param("birthDate") LocalDate birthDate);
//
//    @Query("select new com.ubaid.Auth_service.dto.BloodGroupCountResponseEntity(p.bloodGroup," +
//            " Count(p)) from Patient p group by p.bloodGroup")
//    List<Object[]> countEachBloodGroupType();
//    List<BloodGroupCountResponseEntity> countEachBloodGroupType();
//
//    @Query(value = "select * from patient", nativeQuery = true)
//    Page<Patient> findAllPatients(Pageable pageable);
//
//    @Transactional
//    @Modifying
//    @Query("UPDATE Patient p SET p.name = :name where p.id = :id")
//    int updateNameWithId(@Param("name") String name, @Param("id") Long id);
//
//
//    //    @Query("SELECT p FROM Patient p LEFT JOIN FETCH p.appointments a LEFT JOIN FETCH a.doctor")
//    @Query("SELECT p FROM Patient p LEFT JOIN FETCH p.appointments")
//    List<Patient> findAllPatientWithAppointment();

    boolean existsByPatientId(String patientId);

//    Patient findByPatientId(String patientId);
//
//    Patient findByFirstName(String firstName);

    // Or use a custom query to search full name
    @Query("SELECT p FROM Patient p WHERE CONCAT(p.firstName, ' ', p.lastName) = :fullName")
    Patient findByFullName(@Param("fullName") String fullName);

    // Or search by either first or last name containing the search term
//    @Query("SELECT p FROM Patient p WHERE p.firstName LIKE %:name% OR p.lastName LIKE %:name%")
//    List<Patient> findByNameContaining(@Param("name") String name);

    // With this custom query:
//    @Query("SELECT p FROM Patient p WHERE CONCAT(p.firstName, ' ', p.lastName) LIKE %:name% ORDER BY p.id DESC")
//    List<Patient> findByNameContainingOrderByIdDesc(@Param("name") String name);
    Optional<Patient> findByUser(User user);

    // Optional name queries if you keep them:
    @Query("select p from Patient p where concat(p.firstName, ' ', p.lastName) = :name")
    Optional<Patient> findByName(@Param("name") String name);

    @Query("select p from Patient p where lower(concat(p.firstName, ' ', p.lastName)) like lower(concat('%', :name, '%'))")
    List<Patient> findByNameContaining(@Param("name") String name);

    boolean existsByUser(User user);

    Optional<Patient> findByUserId(Long currentUserId);



}
