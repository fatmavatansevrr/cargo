package com.cargotracking.shipment_service.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TrackingNumberGeneratorImpl implements TrackingNumberGenerator {

    private static final String PREFIX = "CT";
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    @Override
    public String generate() {
        String datePart = LocalDateTime.now().format(DATE_FORMATTER);
        String randomPart = generateRandomString(6);
        return PREFIX + datePart + randomPart;
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
