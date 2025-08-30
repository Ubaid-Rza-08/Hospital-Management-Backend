package com.ubaid.Auth_service.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class AdminIdGenerator {

    private static final String PREFIX = "ADM";
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yy");

    /**
     * Generate base admin ID from department and username
     * Format: ADM-{year}-{departmentPrefix}-{usernamePrefix}
     * If department is null, uses username only
     */
    public String baseId(String department, String username) {
        StringBuilder baseId = new StringBuilder(PREFIX);

        // Add current year
        String year = LocalDate.now().format(YEAR_FORMATTER);
        baseId.append("-").append(year);

        // Add department prefix if available
        if (department != null && !department.trim().isEmpty()) {
            String cleanDept = department.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            String deptPrefix = cleanDept.length() >= 3 ?
                    cleanDept.substring(0, 3) : cleanDept;
            baseId.append("-").append(deptPrefix);
        }

        // Add username prefix
        if (username != null && !username.trim().isEmpty()) {
            String cleanUsername = username.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            String usernamePrefix = cleanUsername.length() >= 3 ?
                    cleanUsername.substring(0, 3) : cleanUsername;
            baseId.append("-").append(usernamePrefix);
        }

        return baseId.toString();
    }

    /**
     * Alternative method using admin level instead of department
     * Format: ADM-{year}-{levelPrefix}-{usernamePrefix}
     */
    public String baseIdWithLevel(String adminLevel, String username) {
        StringBuilder baseId = new StringBuilder(PREFIX);

        // Add current year
        String year = LocalDate.now().format(YEAR_FORMATTER);
        baseId.append("-").append(year);

        // Add admin level prefix if available
        if (adminLevel != null && !adminLevel.trim().isEmpty()) {
            String cleanLevel = adminLevel.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            String levelPrefix = cleanLevel.length() >= 3 ?
                    cleanLevel.substring(0, 3) : cleanLevel;
            baseId.append("-").append(levelPrefix);
        }

        // Add username prefix
        if (username != null && !username.trim().isEmpty()) {
            String cleanUsername = username.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            String usernamePrefix = cleanUsername.length() >= 3 ?
                    cleanUsername.substring(0, 3) : cleanUsername;
            baseId.append("-").append(usernamePrefix);
        }

        return baseId.toString();
    }

    /**
     * Generate base admin ID using both department and level
     * Format: ADM-{year}-{deptPrefix}-{levelPrefix}-{usernamePrefix}
     */
    public String baseIdWithDeptAndLevel(String department, String adminLevel, String username) {
        StringBuilder baseId = new StringBuilder(PREFIX);

        // Add current year
        String year = LocalDate.now().format(YEAR_FORMATTER);
        baseId.append("-").append(year);

        // Add department prefix
        if (department != null && !department.trim().isEmpty()) {
            String cleanDept = department.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            String deptPrefix = cleanDept.length() >= 2 ?
                    cleanDept.substring(0, 2) : cleanDept;
            baseId.append("-").append(deptPrefix);
        }

        // Add level prefix
        if (adminLevel != null && !adminLevel.trim().isEmpty()) {
            String cleanLevel = adminLevel.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            String levelPrefix = cleanLevel.length() >= 2 ?
                    cleanLevel.substring(0, 2) : cleanLevel;
            baseId.append("-").append(levelPrefix);
        }

        // Add username prefix
        if (username != null && !username.trim().isEmpty()) {
            String cleanUsername = username.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            String usernamePrefix = cleanUsername.length() >= 2 ?
                    cleanUsername.substring(0, 2) : cleanUsername;
            baseId.append("-").append(usernamePrefix);
        }

        return baseId.toString();
    }
}
