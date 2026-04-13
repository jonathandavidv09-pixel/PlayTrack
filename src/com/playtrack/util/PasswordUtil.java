package com.playtrack.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

// Utility component: shared helpers for the system layer.
public class PasswordUtil {
    // hashPassword.
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // verifyPassword.
    public static boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }
}