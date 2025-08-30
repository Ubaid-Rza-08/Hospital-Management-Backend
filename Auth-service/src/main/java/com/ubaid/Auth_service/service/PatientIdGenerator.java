package com.ubaid.Auth_service.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.LocalDate;

@Component
public class PatientIdGenerator {
    public String baseId(LocalDate dob, String username) {
        String date = dob != null ? String.format("%04d%02d%02d", dob.getYear(), dob.getMonthValue(), dob.getDayOfMonth()) : "00000000";
        String cleanUser = sanitize(username);
        return "PAT-" + date + "-" + cleanUser;
    }

    private String sanitize(String input) {
        if (input == null) return "USER";
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFKD)
                .replaceAll("[^\\p{ASCII}]", "");
        String alnum = normalized.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (alnum.isBlank()) return "USER";
        return alnum.length() > 16 ? alnum.substring(0, 16) : alnum;
    }
}

