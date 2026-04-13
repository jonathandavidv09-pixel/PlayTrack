package com.playtrack.util;

import java.util.regex.Pattern;

// Utility component: shared helpers for the system layer.
public class Validator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // isValidEmail.
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    // isValidPassword.
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    // getPasswordStrength.
    public static String getPasswordStrength(String password) {
        if (password == null || password.length() < 8) return "Weak";
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        int score = 0;
        if (hasUpper) score++;
        if (hasLower) score++;
        if (hasDigit) score++;
        if (hasSpecial) score++;
        
        if (score <= 2) return "Medium";
        return "Strong";
    }

    private static final String[] COMMON_PASSWORDS = {"password", "password123", "123456", "12345678", "123456789", "qwerty", "admin", "admin123"};

    // isCommonPassword.
    public static boolean isCommonPassword(String password) {
        if (password == null) return false;
        String lower = password.toLowerCase();
        for (String p : COMMON_PASSWORDS) {
            if (lower.equals(p)) return true;
        }
        return false;
    }

    // hasComplexity.
    public static boolean hasComplexity(String password) {
        if (password == null) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}