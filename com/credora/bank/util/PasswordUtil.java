package com.credora.bank.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    public static String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                String part = Integer.toHexString(0xff & b);
                if (part.length() == 1) {
                    sb.append('0');
                }
                sb.append(part);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static boolean verify(String rawPassword, String storedHash) {
        String rawHash = hash(rawPassword);
        return rawHash.equalsIgnoreCase(storedHash);
    }
}
