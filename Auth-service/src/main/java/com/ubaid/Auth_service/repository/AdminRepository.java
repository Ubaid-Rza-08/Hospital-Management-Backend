package com.ubaid.Auth_service.repository;

import com.ubaid.Auth_service.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminRepository extends JpaRepository<Admin,Long> {
    List<Admin> findByDepartmentContainingIgnoreCase(String department);
    List<Admin> findByAdminLevel(String adminLevel);
    boolean existsByAdminId(String adminId);

    @Query("SELECT a FROM Admin a WHERE " +
            "(:name IS NULL OR CONCAT(a.firstName, ' ', a.lastName) LIKE %:name%) AND " +
            "(:department IS NULL OR a.department LIKE %:department%) AND " +
            "(:adminLevel IS NULL OR a.adminLevel = :adminLevel)")
    List<Admin> searchAdmins(@Param("name") String name,
                             @Param("department") String department,
                             @Param("adminLevel") String adminLevel);

}
