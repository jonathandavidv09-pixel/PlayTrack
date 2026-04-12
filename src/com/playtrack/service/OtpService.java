package com.playtrack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;

@Service
public class OtpService {

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Value("${spring.mail.port:587}")
    private int smtpPort;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Value("${playtrack.otp.debug-log-enabled:false}")
    private boolean debugOtpLogEnabled;

    @Value("${playtrack.otp.allow-simulated-fallback:false}")
    private boolean allowSimulatedFallback;

    private String currentOtp;
    private String currentEmail;
    private long otpTimestamp;
    private boolean lastDeliverySimulated;
    private String lastDeliveryMessage = "";
    private static final long OTP_VALIDITY_MS = 5 * 60 * 1000;

    public boolean sendOtp(String email) {
        Random random = new Random();
        currentOtp = String.format("%06d", random.nextInt(999999));
        currentEmail = email;
        otpTimestamp = System.currentTimeMillis();
        lastDeliverySimulated = false;
        lastDeliveryMessage = "";

        String effectiveHost = resolveConfigValue(smtpHost, "PLAYTRACK_SMTP_HOST");
        String effectiveUser = resolveConfigValue(senderEmail, "PLAYTRACK_SMTP_USERNAME");
        String effectivePassword = normalizeSmtpPassword(resolveConfigValue(smtpPassword, "PLAYTRACK_SMTP_PASSWORD"));
        int effectivePort = resolvePort();

        if (debugOtpLogEnabled) {
            com.playtrack.util.DatabaseLogger.logOtp(email, currentOtp);
            System.out.println("[OTP Service] Generated OTP for " + email + ": " + currentOtp);
        }

        try {
            if (!isSmtpConfigured(effectiveHost, effectiveUser, effectivePassword)) {
                if (allowSimulatedFallback || debugOtpLogEnabled) {
                    return useSimulatedDelivery(email,
                            "SMTP is not configured. Set PLAYTRACK_SMTP_HOST, PLAYTRACK_SMTP_USERNAME, and PLAYTRACK_SMTP_PASSWORD.");
                }
                lastDeliveryMessage = buildConfigErrorMessage(effectiveHost, effectiveUser, effectivePassword);
                System.err.println(lastDeliveryMessage);
                return false;
            }

            JavaMailSender mailSender = buildMailSender(effectiveHost, effectivePort, effectiveUser, effectivePassword);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(effectiveUser, "PlayTrack Security");
            helper.setTo(email);
            helper.setSubject("Your PlayTrack Verification Code");

            String htmlContent = "<div style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>"
                    + "<div style='max-width: 500px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>"
                    + "<h2 style='color: #6366f1; text-align: center; margin-bottom: 20px;'>PlayTrack Verification</h2>"
                    + "<p style='color: #4a5568; font-size: 16px;'>Hello,</p>"
                    + "<p style='color: #4a5568; font-size: 16px;'>Please use the following One-Time Password (OTP) to complete your request. This code is valid for 5 minutes.</p>"
                    + "<div style='text-align: center; margin: 30px 0;'>"
                    + "<span style='font-size: 32px; font-weight: bold; background-color: #e0e7ff; color: #4338ca; padding: 10px 20px; border-radius: 6px; letter-spacing: 4px;'>"
                    + currentOtp + "</span>"
                    + "</div>"
                    + "<p style='color: #718096; font-size: 14px;'>If you did not request this verification code, please ignore this email.</p>"
                    + "<br><hr style='border: none; border-top: 1px solid #edf2f7;'><br>"
                    + "<p style='color: #a0aec0; font-size: 12px; text-align: center;'>&copy; PlayTrack Ent. All rights reserved.</p>"
                    + "</div></div>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("OTP email successfully sent to " + email);
            lastDeliveryMessage = "";
            return true;

        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
            if (allowSimulatedFallback || debugOtpLogEnabled) {
                return useSimulatedDelivery(email,
                        "Email delivery failed, so a local OTP was generated for this session.");
            }
            lastDeliveryMessage = classifyMailError(e);
            return false;
        }
    }

    private JavaMailSender buildMailSender(String host, int port, String username, String password) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        return sender;
    }

    private String normalizeSmtpPassword(String rawPassword) {
        if (rawPassword == null) {
            return "";
        }
        // Gmail app passwords are often shown with spaces; SMTP requires the raw token.
        return rawPassword.replaceAll("\\s+", "").trim();
    }

    private String classifyMailError(Exception e) {
        String details = collectExceptionDetails(e).toLowerCase();
        if (details.contains("authentication")
                || details.contains("535")
                || details.contains("5.7.8")
                || details.contains("username and password not accepted")
                || details.contains("application-specific password required")) {
            return "Gmail auth failed. Check app password.";
        }
        if (details.contains("could not connect")
                || details.contains("connect timed out")
                || details.contains("connection timed out")
                || details.contains("unknownhostexception")) {
            return "SMTP connection failed. Check network.";
        }
        if (details.contains("starttls")
                || details.contains("ssl")
                || details.contains("tls")) {
            return "SMTP TLS failed. Use port 587 + STARTTLS.";
        }
        return "Unable to send verification code.";
    }

    private String buildConfigErrorMessage(String host, String username, String password) {
        boolean missingHost = isBlank(host);
        boolean missingUser = isBlank(username);
        boolean missingPassword = isBlank(password);

        if (missingHost && missingUser && missingPassword) {
            return "SMTP not configured. Set Gmail SMTP env vars and restart app.";
        }
        if (missingUser && missingPassword) {
            return "SMTP missing username and app password. Restart app after set.";
        }
        if (missingUser) {
            return "SMTP missing username.";
        }
        if (missingPassword) {
            return "SMTP missing app password.";
        }
        if (missingHost) {
            return "SMTP missing host.";
        }

        String normalizedHost = host.trim().toLowerCase();
        if (normalizedHost.contains("gmail.com")) {
            String normalizedPass = normalizeSmtpPassword(password);
            if (normalizedPass.length() != 16) {
                return "Gmail app password must be 16 chars.";
            }
        }
        return "SMTP config invalid. Check email settings.";
    }

    private String collectExceptionDetails(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        Throwable current = throwable;
        int depth = 0;
        while (current != null && depth < 6) {
            if (current.getMessage() != null) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append(current.getMessage());
            }
            current = current.getCause();
            depth++;
        }
        return sb.toString();
    }

    private boolean isSmtpConfigured(String host, String username, String password) {
        return !isBlank(host)
                && !isBlank(username)
                && !isBlank(password)
                && !"your.email@gmail.com".equalsIgnoreCase(username.trim());
    }

    private int resolvePort() {
        if (smtpPort > 0) {
            return smtpPort;
        }
        String raw = resolveConfigValue("", "PLAYTRACK_SMTP_PORT");
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception ignored) {
            return 587;
        }
    }

    private String resolveConfigValue(String injectedValue, String envKey) {
        if (!isBlank(injectedValue)) {
            return injectedValue.trim();
        }
        String envValue = System.getenv(envKey);
        if (!isBlank(envValue)) {
            return envValue.trim();
        }
        String sysPropValue = System.getProperty(envKey);
        if (!isBlank(sysPropValue)) {
            return sysPropValue.trim();
        }
        String windowsUserValue = readWindowsUserEnv(envKey);
        if (!isBlank(windowsUserValue)) {
            return windowsUserValue.trim();
        }
        return "";
    }

    private String readWindowsUserEnv(String envKey) {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) {
            return "";
        }
        try {
            Process process = new ProcessBuilder("reg", "query", "HKCU\\Environment", "/v", envKey)
                    .redirectErrorStream(true)
                    .start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.contains(envKey) || !line.contains("REG_")) {
                        continue;
                    }
                    String[] parts = line.trim().split("\\s{2,}");
                    if (parts.length >= 3) {
                        return parts[2];
                    }
                }
            }
        } catch (Exception ignored) {
            // no-op: fallback simply returns blank
        }
        return "";
    }

    private boolean useSimulatedDelivery(String email, String reason) {
        lastDeliverySimulated = true;
        lastDeliveryMessage = reason;
        System.err.println("WARNING: " + reason);
        System.err.println("[OTP Service] Local OTP for " + email + ": " + currentOtp);
        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String getCurrentOtp() {
        return currentOtp;
    }

    public boolean wasLastDeliverySimulated() {
        return lastDeliverySimulated;
    }

    public String getLastDeliveryMessage() {
        return lastDeliveryMessage;
    }

    public boolean verifyOtp(String email, String otp) {
        if (currentOtp == null || currentEmail == null) return false;
        if (System.currentTimeMillis() - otpTimestamp > OTP_VALIDITY_MS) {
            currentOtp = null;
            currentEmail = null;
            return false;
        }
        return currentEmail.equals(email) && currentOtp.equals(otp);
    }

    public void clearOtp() {
        currentOtp = null;
        currentEmail = null;
    }
}
