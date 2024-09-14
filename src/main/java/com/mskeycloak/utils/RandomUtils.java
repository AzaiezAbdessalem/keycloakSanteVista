package com.mskeycloak.utils;

import java.security.SecureRandom;

public class RandomUtils {

    private RandomUtils() {
    }

    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();

        if (length < 1) {
            length = 30;
        }

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    public static String generateRandomCode() {
        final String characters = "123456789";
        final StringBuilder sb = new StringBuilder();
        final SecureRandom random = new SecureRandom();
        final int length = 8;
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }

}
