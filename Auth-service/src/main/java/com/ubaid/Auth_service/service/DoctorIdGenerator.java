package com.ubaid.Auth_service.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class DoctorIdGenerator {

    private static final String PREFIX = "DOC";
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yy");

    /**
     * Generate base doctor ID from license number and username
     * Format: DOC-{year}-{licensePrefix}-{usernamePrefix}
     * If license number is null, uses username only
     */
    public String baseId(String licenseNumber, String username) {
        StringBuilder baseId = new StringBuilder(PREFIX);

        // Add current year
        String year = LocalDate.now().format(YEAR_FORMATTER);
        baseId.append("-").append(year);

        // Add license number prefix if available
        if (licenseNumber != null && !licenseNumber.trim().isEmpty()) {
            String cleanLicense = licenseNumber.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            String licensePrefix = cleanLicense.length() >= 4 ?
                    cleanLicense.substring(0, 4) : cleanLicense;
            baseId.append("-").append(licensePrefix);
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
     * Alternative method using specialization instead of license
     * Format: DOC-{year}-{specializationPrefix}-{usernamePrefix}
     */
    public String baseIdWithSpecialization(String specialization, String username) {
        StringBuilder baseId = new StringBuilder(PREFIX);

        // Add current year
        String year = LocalDate.now().format(YEAR_FORMATTER);
        baseId.append("-").append(year);

        // Add specialization prefix if available
        if (specialization != null && !specialization.trim().isEmpty()) {
            String cleanSpec = specialization.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            String specPrefix = cleanSpec.length() >= 4 ?
                    cleanSpec.substring(0, 4) : cleanSpec;
            baseId.append("-").append(specPrefix);
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
}

