package com.playtrack.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${playtrack.otp.debug-log-enabled:false}")
    private boolean debugOtpLogEnabled;

    @Value("${playtrack.otp.allow-simulated-fallback:false}")
    private boolean allowSimulatedFallback;

    private String currentOtp;
    private String currentEmail;
    private long otpTimestamp;
    private static final long OTP_VALIDITY_MS = 5 * 60 * 1000; // 5 minutes

    /**
     * Generates a 6-digit OTP and sends it via email.
     * Returns true if OTP was sent successfully.
     */
    public boolean sendOtp(String email) {
        Random random = new Random();
        currentOtp = String.format("%06d", random.nextInt(999999));
        currentEmail = email;
        otpTimestamp = System.currentTimeMillis();

        if (debugOtpLogEnabled) {
            // Development-only troubleshooting path.
            com.playtrack.util.DatabaseLogger.logOtp(email, currentOtp);
            System.out.println("[OTP Service] Generated OTP for " + email + ": " + currentOtp);
        }

        try {
            if (senderEmail == null || senderEmail.trim().isEmpty() || senderEmail.equals("your.email@gmail.com")) {
                if (allowSimulatedFallback || debugOtpLogEnabled) {
                    System.err.println("WARNING: Using simulated OTP because spring.mail.username is not configured.");
                    return true;
                }
                System.err.println("OTP delivery is not configured. Please set SMTP environment variables.");
                return false;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, "PlayTrack Security");
            helper.setTo(email);
            helper.setSubject("Your PlayTrack Verification Code");

            // HTML Body
            String htmlContent = "<div style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>"
                    + "<div style='max-width: 500px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>"
                    + "<h2 style='color: #6366f1; text-align: center; margin-bottom: 20px;'>PlayTrack Verification</h2>"
                    + "<p style='color: #4a5568; font-size: 16px;'>Hello,</p>"
                    + "<p style='color: #4a5568; font-size: 16px;'>Please use the following One-Time Password (OTP) to complete your request. This code is valid for 5 minutes.</p>"
                    + "<div style='text-align: center; margin: 30px 0;'>"
                    + "<span style='font-size: 32px; font-weight: bold; background-color: #e0e7ff; color: #4338ca; padding: 10px 20px; border-radius: 6px; letter-spacing: 4px;'>" + currentOtp + "</span>"
                    + "</div>"
                    + "<p style='color: #718096; font-size: 14px;'>If you did not request this verification code, please ignore this email.</p>"
                    + "<br><hr style='border: none; border-top: 1px solid #edf2f7;'><br>"
                    + "<p style='color: #a0aec0; font-size: 12px; text-align: center;'>&copy; PlayTrack Ent. All rights reserved.</p>"
                    + "</div></div>";

            helper.setText(htmlContent, true); // true indicates html

            mailSender.send(message);
            System.out.println("OTP email successfully sent to " + email);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the current OTP (for testing/simulated fallback purposes).
     */
    public String getCurrentOtp() {
        return currentOtp;
    }

    /**
     * Verifies an OTP for the given email.
     */
    public boolean verifyOtp(String email, String otp) {
        if (currentOtp == null || currentEmail == null) return false;
        if (System.currentTimeMillis() - otpTimestamp > OTP_VALIDITY_MS) {
            currentOtp = null;
            currentEmail = null;
            return false;
        }
        return currentEmail.equals(email) && currentOtp.equals(otp);
    }

    /**
     * Clears the current OTP after successful verification.
     */
    public void clearOtp() {
        currentOtp = null;
        currentEmail = null;
    }
}
