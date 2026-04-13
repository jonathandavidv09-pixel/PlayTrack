package com.playtrack.ui.auth;

import com.playtrack.service.AuthService;
import com.playtrack.service.OtpService;
import com.playtrack.ui.components.StyleConfig;
import com.playtrack.util.Validator;
import com.playtrack.util.PasswordUtil;
import com.playtrack.config.AuthDBConnection;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AuthFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final char PASSWORD_MASK = '\u25CF';
    private JPanel cardsPanel;
    private CardLayout cardLayout;
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;

    private AuthService authService = new AuthService();
    private Runnable onLoginSuccess;

   
    private String pendingUsername;
    private String pendingEmail;
    private String pendingPassword;

    public AuthFrame(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setTitle("PlayTrack - Authentication");
        setSize(1400, 900);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 700));

       
        CinematicBackgroundPanel mainPanel = new CinematicBackgroundPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel() {
            @Override
            public void doLayout() {
                super.doLayout();
                int w = getWidth();

                if (getComponentCount() > 0) {
                    
                    Component branding = getComponent(0);
                    branding.setBounds(0, 0, w, branding.getPreferredSize().height);
                }
            }
        };
        leftPanel.setOpaque(false);
        leftPanel.setLayout(null);

       
        JPanel brandingPanel = new JPanel();
        brandingPanel.setLayout(new BoxLayout(brandingPanel, BoxLayout.Y_AXIS));
        brandingPanel.setOpaque(false);
        brandingPanel.setBorder(BorderFactory.createEmptyBorder(28, 56, 0, 50));

       
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoRow.setOpaque(false);

        
        String[] logoCandidates = { "resources/LogoDarkMode.png", "resources/logo.png" };
        for (String logoPath : logoCandidates) {
            try (InputStream logoStream = getClass().getClassLoader().getResourceAsStream(logoPath)) {
                if (logoStream != null) {
                    java.awt.image.BufferedImage logoImg = ImageIO.read(logoStream);
                    if (logoImg != null) {
                      
                        int logoH = 78;
                        int computedLogoW = (int) ((double) logoImg.getWidth() / logoImg.getHeight() * logoH);
                        int logoW = Math.min(computedLogoW, 220);
                        Image scaledLogo = logoImg.getScaledInstance(logoW, logoH, Image.SCALE_SMOOTH);
                        logoRow.add(new JLabel(new ImageIcon(scaledLogo)));
                        break;
                    }
                }
            } catch (Exception ex) {
                System.err.println("Could not load " + logoPath + ": " + ex.getMessage());
            }
        }

        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandingPanel.add(logoRow);
        brandingPanel.add(Box.createVerticalStrut(130));

        JLabel welcomeTo = new JLabel("Welcome to");
        welcomeTo.setFont(new Font("Segoe UI", Font.BOLD, 44));
        welcomeTo.setForeground(Color.WHITE);
        welcomeTo.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandingPanel.add(welcomeTo);

        final String brandTitleText = "PLAYTRACK";
        final Font brandTitleFont = new Font("Segoe UI", Font.BOLD, 112);
        JComponent playTrackGradientTitle = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(brandTitleFont);
                return new Dimension(fm.stringWidth(brandTitleText) + 8, fm.getHeight() + 6);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(brandTitleFont);

                FontMetrics fm = g2.getFontMetrics();
                int baseline = fm.getAscent();

                GradientPaint titleGradient = new GradientPaint(
                        0, 0, new Color(180, 24, 45),
                        getWidth(), 0, new Color(253, 164, 129));
                g2.setPaint(titleGradient);
                g2.drawString(brandTitleText, 0, baseline);
                g2.dispose();
            }
        };
        playTrackGradientTitle.setOpaque(false);
        playTrackGradientTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandingPanel.add(playTrackGradientTitle);
        brandingPanel.add(Box.createVerticalStrut(8));

        JLabel subtitleHeading = new JLabel("Your personal media journal");
        subtitleHeading.setFont(new Font("Segoe UI", Font.BOLD, 48));
        subtitleHeading.setForeground(new Color(230, 236, 246));
        subtitleHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandingPanel.add(subtitleHeading);
        brandingPanel.add(Box.createVerticalStrut(18));

        JLabel subtitle = new JLabel(
                "<html>Track, review, and revisit your movies,<br>games, and books in one personal media journal.</html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        subtitle.setForeground(new Color(216, 224, 238, 225));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandingPanel.add(subtitle);

        leftPanel.add(brandingPanel); 

       
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(560, 0));
        rightPanel.setMinimumSize(new Dimension(560, 0));
        rightPanel.setMaximumSize(new Dimension(560, Integer.MAX_VALUE));

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setOpaque(false);
        Dimension fixedAuthCard = new Dimension(420, 600);
        cardsPanel.setPreferredSize(fixedAuthCard);
        cardsPanel.setMinimumSize(fixedAuthCard);
        cardsPanel.setMaximumSize(fixedAuthCard);

        loginPanel = new LoginPanel(loginAction(), e -> cardLayout.show(cardsPanel, "register"),
                () -> showForgotPasswordDialog());
        registerPanel = new RegisterPanel(registerAction(), e -> cardLayout.show(cardsPanel, "login"));

        cardsPanel.add(loginPanel, "login");
        cardsPanel.add(registerPanel, "register");

        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.fill = GridBagConstraints.NONE;
        rightGbc.weightx = 1;
        rightGbc.weighty = 1;
        rightGbc.insets = new Insets(30, 10, 30, 90);
        rightPanel.add(cardsPanel, rightGbc);

        
        mainPanel.add(leftPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        add(mainPanel);
    }

    private ActionListener loginAction() {
        return e -> {
            String identifier = loginPanel.getIdentifier();
            String password = loginPanel.getPassword();

            if (identifier.isEmpty() || password.isEmpty()) {
                loginPanel.setError("Please fill in all fields");
                return;
            }

            if (authService.login(identifier, password)) {
                onLoginSuccess.run();
                dispose();
            } else {
                loginPanel.setError("Invalid username/email or password");
            }
        };
    }

    private ActionListener registerAction() {
        return e -> {
            String username = registerPanel.getUsername();
            String email = registerPanel.getEmail();
            String password = registerPanel.getPassword();
            String confirm = registerPanel.getConfirmPassword();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                registerPanel.setError("Please fill in all fields");
                return;
            }

            if (!Validator.isValidEmail(email)) {
                registerPanel.setError("Please enter a valid email address");
                return;
            }

            if (!password.equals(confirm)) {
                registerPanel.setError("Passwords do not match");
                return;
            }

            if (password.length() < 8) {
                registerPanel.setError("Password must be at least 8 characters");
                return;
            }

            if (!Validator.hasComplexity(password)) {
                registerPanel.setError("Password must have uppercase, lowercase, numbers & symbols");
                return;
            }

            if (Validator.isCommonPassword(password)) {
                registerPanel.setError("Password is too common");
                return;
            }

            if (authService.isUsernameTaken(username)) {
                registerPanel.setError("the username is already taken");
                return;
            }

           
            pendingUsername = username;
            pendingEmail = email;
            pendingPassword = password;

        
            OtpService otpService = com.playtrack.util.SpringContext.getBean(OtpService.class);
            boolean sent = otpService.sendOtp(email);
            if (!sent) {
                registerPanel.setError(otpService.getLastDeliveryMessage());
                return;
            }
            String otp = otpService.getCurrentOtp();
            if (otpService.wasLastDeliverySimulated()) {
                showLocalOtpNotice(this, email, otp);
            }

            
            showOtpDialog(otp, otpService.wasLastDeliverySimulated());
        };
    }

    private void showForgotPasswordDialog() {
        showForgotStep1_EmailEntry();
    }

    private void showLocalOtpNotice(Component parent, String email, String otp) {
        JOptionPane.showMessageDialog(
                parent,
                "<html><center><b>SMTP is not configured.</b><br>"
                        + "PlayTrack generated a local verification code for this session.<br><br>"
                        + "Code for <b>" + email + "</b>:<br>"
                        + "<span style='font-size:18px; color:#d34045;'><b>" + otp + "</b></span></center></html>",
                "Local OTP Mode",
                JOptionPane.INFORMATION_MESSAGE);
    }

   
    private void showForgotStep1_EmailEntry() {
        JDialog dialog = createStyledDialog("Forgot Password", 460, 420);
        JPanel panel = createDialogPanel();
        GridBagConstraints gbc = createDialogGbc();

       
        JLabel icon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = getHeight() / 2;
                
                g2.setColor(new Color(211, 64, 69, 25));
                g2.fillOval(cx - 40, cy - 40, 80, 80);
                g2.setColor(new Color(211, 64, 69, 50));
                g2.fillOval(cx - 30, cy - 30, 60, 60);
               
                g2.setColor(new Color(211, 64, 69));
                g2.fillOval(cx - 22, cy - 22, 44, 44);
               
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                g2.fillRoundRect(cx - 9, cy - 3, 18, 14, 4, 4);
              
                g2.setColor(Color.WHITE);
                g2.drawArc(cx - 7, cy - 14, 14, 16, 0, 180);
             
                g2.setColor(new Color(211, 64, 69));
                g2.fillOval(cx - 3, cy + 1, 6, 6);
                g2.dispose();
            }
        };
        icon.setPreferredSize(new Dimension(90, 90));
        gbc.gridy = 0;
        gbc.insets = new Insets(25, 40, 5, 40);
        panel.add(icon, gbc);

     
        JLabel title = new JLabel("Forgot Password?", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(StyleConfig.TEXT_COLOR);
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 40, 3, 40);
        panel.add(title, gbc);

        
        JLabel desc = new JLabel(
                "<html><center>No worries! Enter your registered email<br>and we'll send you a verification code.</center></html>",
                SwingConstants.CENTER);
        desc.setFont(StyleConfig.FONT_SMALL);
        desc.setForeground(StyleConfig.TEXT_SECONDARY);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 40, 15, 40);
        panel.add(desc, gbc);

        
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailLabel.setForeground(new Color(200, 200, 210));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 50, 4, 50);
        panel.add(emailLabel, gbc);

        
        JTextField emailField = createStyledTextField("Enter your email address");
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 50, 5, 50);
        panel.add(emailField, gbc);

        JLabel errorLabel = createErrorLabel();
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 50, 5, 50);
        panel.add(errorLabel, gbc);

        
        // Primary action button for requesting the reset OTP code.
        JButton sendBtn = createGradientButton("SEND VERIFICATION CODE");
        sendBtn.addActionListener(ev -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                errorLabel.setText("Please enter your email address");
                return;
            }
            if (!Validator.isValidEmail(email)) {
                errorLabel.setText("Please enter a valid email address");
                return;
            }
          
            sendBtn.setEnabled(false);
            sendBtn.setText("SENDING...");
            OtpService otpService = com.playtrack.util.SpringContext.getBean(OtpService.class);
            boolean sent = otpService.sendOtp(email);
            if (!sent) {
                errorLabel.setText(otpService.getLastDeliveryMessage());
                sendBtn.setEnabled(true);
                sendBtn.setText("SEND VERIFICATION CODE");
                return;
            }
            if (otpService.wasLastDeliverySimulated()) {
                showLocalOtpNotice(dialog, email, otpService.getCurrentOtp());
            }
            dialog.dispose();
            showForgotStep2_OtpVerify(email);
        });
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 50, 10, 50);
        panel.add(sendBtn, gbc);

      
        JLabel cancelLink = createCancelLink(dialog);
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 50, 20, 50);
        panel.add(cancelLink, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

   
    private void showForgotStep2_OtpVerify(String email) {
        JDialog dialog = createStyledDialog("Verify Code", 460, 430);
        JPanel panel = createDialogPanel();
        GridBagConstraints gbc = createDialogGbc();

        
        JLabel icon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.setColor(new Color(211, 64, 69, 25));
                g2.fillOval(cx - 40, cy - 40, 80, 80);
                g2.setColor(new Color(211, 64, 69, 50));
                g2.fillOval(cx - 30, cy - 30, 60, 60);
                g2.setColor(new Color(211, 64, 69));
                g2.fillOval(cx - 22, cy - 22, 44, 44);
             
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRect(cx - 12, cy - 6, 24, 16);
                g2.drawLine(cx - 12, cy - 6, cx, cy + 4);
                g2.drawLine(cx, cy + 4, cx + 12, cy - 6);
                g2.dispose();
            }
        };
        icon.setPreferredSize(new Dimension(90, 90));
        gbc.gridy = 0;
        gbc.insets = new Insets(25, 40, 5, 40);
        panel.add(icon, gbc);

      
        JLabel title = new JLabel("Check Your Email", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(StyleConfig.TEXT_COLOR);
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 40, 3, 40);
        panel.add(title, gbc);

        
        JLabel desc = new JLabel(
                "<html><center>We've sent a verification code to<br><b style='color:#d34045'>" + email
                        + "</b></center></html>",
                SwingConstants.CENTER);
        desc.setFont(StyleConfig.FONT_SMALL);
        desc.setForeground(StyleConfig.TEXT_SECONDARY);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 30, 5, 30);
        panel.add(desc, gbc);

      
        JLabel hint = new JLabel(
                "<html><center>Check your inbox and spam folder for the code.</center></html>",
                SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(StyleConfig.TEXT_LIGHT);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 40, 12, 40);
        panel.add(hint, gbc);

       
        JTextField[] otpFields = new JTextField[6];
        Runnable onComplete = () -> {
        
        };
        JPanel otpControls = createOtpBoxRow(otpFields, onComplete);
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 40, 5, 40);
        panel.add(otpControls, gbc);

     
        JLabel resendLabel = new JLabel("<html>Didn't receive a code? <font color='" + String.format("#%06x", StyleConfig.PRIMARY_COLOR.getRGB() & 0xFFFFFF) + "'><b>Resend</b></font></html>", SwingConstants.CENTER);
        resendLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resendLabel.setForeground(StyleConfig.TEXT_LIGHT);
        resendLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resendLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                OtpService otpService = com.playtrack.util.SpringContext.getBean(OtpService.class);
                boolean sent = otpService.sendOtp(email);
                if (!sent) {
                    JOptionPane.showMessageDialog(dialog,
                            otpService.getLastDeliveryMessage(),
                            "Resend Failed",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (otpService.wasLastDeliverySimulated()) {
                    showLocalOtpNotice(dialog, email, otpService.getCurrentOtp());
                } else {
                    JOptionPane.showMessageDialog(dialog, "Verification code resent to " + email);
                }
                for (JTextField f : otpFields) f.setText("");
                otpFields[0].requestFocusInWindow();
            }
        });
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 40, 10, 40);
        panel.add(resendLabel, gbc);

        
        JLabel errorLabel = createErrorLabel();
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 50, 5, 50);
        panel.add(errorLabel, gbc);

        
        // Primary action button for verifying the OTP code.
        JButton verifyBtn = createGradientButton("VERIFY CODE");
        verifyBtn.addActionListener(ev -> {
            StringBuilder sb = new StringBuilder();
            for (JTextField f : otpFields) sb.append(f.getText());
            String otp = sb.toString().trim();
            if (otp.length() < 6) {
                errorLabel.setText("Please enter the 6-digit verification code");
                return;
            }
            OtpService otpService = com.playtrack.util.SpringContext.getBean(OtpService.class);
            if (otpService.verifyOtp(email, otp)) {
                otpService.clearOtp();
                dialog.dispose();
                showForgotStep3_ResetPassword(email);
            } else {
                errorLabel.setText("Invalid or expired code. Please try again.");
                for (JTextField f : otpFields) f.setText("");
                otpFields[0].requestFocusInWindow();
            }
        });
        gbc.gridy = 7;
        gbc.insets = new Insets(5, 50, 10, 50);
        panel.add(verifyBtn, gbc);

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                otpFields[0].requestFocusInWindow();
            }
        });

      
        JLabel cancelLink = createCancelLink(dialog);
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 50, 20, 50);
        panel.add(cancelLink, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }


    private void showForgotStep3_ResetPassword(String email) {
        JDialog dialog = createStyledDialog("Reset Password", 460, 460);
        JPanel panel = createDialogPanel();
        GridBagConstraints gbc = createDialogGbc();

      
        JLabel icon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.setColor(new Color(46, 204, 113, 25));
                g2.fillOval(cx - 40, cy - 40, 80, 80);
                g2.setColor(new Color(46, 204, 113, 50));
                g2.fillOval(cx - 30, cy - 30, 60, 60);
                g2.setColor(new Color(46, 204, 113));
                g2.fillOval(cx - 22, cy - 22, 44, 44);
       
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.fillOval(cx - 6, cy - 10, 12, 12);
                g2.fillRect(cx - 2, cy + 1, 4, 12);
                g2.fillRect(cx - 5, cy + 6, 6, 3);
                g2.dispose();
            }
        };
        icon.setPreferredSize(new Dimension(90, 90));
        gbc.gridy = 0;
        gbc.insets = new Insets(25, 40, 5, 40);
        panel.add(icon, gbc);

    
        JLabel title = new JLabel("Create New Password", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(StyleConfig.TEXT_COLOR);
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 40, 3, 40);
        panel.add(title, gbc);

     
        JLabel desc = new JLabel(
                "<html><center>Your new password must be at least<br>8 characters long.</center></html>",
                SwingConstants.CENTER);
        desc.setFont(StyleConfig.FONT_SMALL);
        desc.setForeground(StyleConfig.TEXT_SECONDARY);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 40, 12, 40);
        panel.add(desc, gbc);

       
        JLabel pwdLabel = new JLabel("New Password");
        pwdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pwdLabel.setForeground(new Color(200, 200, 210));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 50, 4, 50);
        panel.add(pwdLabel, gbc);

    
        JPasswordField pwdField = createStyledPasswordField();
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 50, 10, 50);
        panel.add(pwdField, gbc);

        
        JLabel confirmLabel = new JLabel("Confirm Password");
        confirmLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        confirmLabel.setForeground(new Color(200, 200, 210));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 50, 4, 50);
        panel.add(confirmLabel, gbc);

      
        JPasswordField confirmField = createStyledPasswordField();
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 50, 5, 50);
        panel.add(confirmField, gbc);

       
        JLabel errorLabel = createErrorLabel();
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 50, 5, 50);
        panel.add(errorLabel, gbc);

        // Primary action button for applying the new password.
        JButton resetBtn = createGradientButton("RESET PASSWORD");
        resetBtn.addActionListener(ev -> {
            String p1 = new String(pwdField.getPassword());
            String p2 = new String(confirmField.getPassword());

            if (p1.isEmpty() || p2.isEmpty()) {
                errorLabel.setText("Please fill in both fields");
                return;
            }
            if (p1.length() < 8) {
                errorLabel.setText("Password must be at least 8 characters");
                return;
            }
            if (!p1.equals(p2)) {
                errorLabel.setText("Passwords do not match");
                return;
            }

            
            try (Connection conn = AuthDBConnection.getConnection()) {
                String updateQuery = "UPDATE users SET password_hash = ? WHERE email = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setString(1, PasswordUtil.hashPassword(p1));
                    stmt.setString(2, email);
                    int updated = stmt.executeUpdate();
                    if (updated > 0) {
                        dialog.dispose();
                        showForgotStep4_Success();
                    } else {
                        errorLabel.setText("Email not found. Could not reset.");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Database error. Please try again.");
            }
        });
        gbc.gridy = 8;
        gbc.insets = new Insets(5, 50, 10, 50);
        panel.add(resetBtn, gbc);

        
        JLabel cancelLink = createCancelLink(dialog);
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 50, 20, 50);
        panel.add(cancelLink, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    
    private void showForgotStep4_Success() {
        JDialog dialog = createStyledDialog("Password Reset", 420, 360);
        JPanel panel = createDialogPanel();
        GridBagConstraints gbc = createDialogGbc();

        
        JLabel icon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.setColor(new Color(46, 204, 113, 25));
                g2.fillOval(cx - 40, cy - 40, 80, 80);
                g2.setColor(new Color(46, 204, 113, 50));
                g2.fillOval(cx - 30, cy - 30, 60, 60);
                g2.setColor(new Color(46, 204, 113));
                g2.fillOval(cx - 22, cy - 22, 44, 44);
                
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 10, cy, cx - 3, cy + 8);
                g2.drawLine(cx - 3, cy + 8, cx + 11, cy - 7);
                g2.dispose();
            }
        };
        icon.setPreferredSize(new Dimension(100, 100));
        gbc.gridy = 0;
        gbc.insets = new Insets(30, 40, 10, 40);
        panel.add(icon, gbc);

        
        JLabel title = new JLabel("Password Reset!", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(StyleConfig.TEXT_COLOR);
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 40, 5, 40);
        panel.add(title, gbc);

        
        JLabel desc = new JLabel(
                "<html><center>Your password has been successfully reset.<br>You can now sign in with your new password.</center></html>",
                SwingConstants.CENTER);
        desc.setFont(StyleConfig.FONT_NORMAL);
        desc.setForeground(StyleConfig.TEXT_SECONDARY);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 30, 20, 30);
        panel.add(desc, gbc);

        
        // Navigation button that returns users to the login card.
        JButton loginBtn = createGradientButton("BACK TO LOGIN");
        loginBtn.addActionListener(ev -> {
            dialog.dispose();
            cardLayout.show(cardsPanel, "login");
        });
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 50, 30, 50);
        panel.add(loginBtn, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    
    private JDialog createStyledDialog(String title, int width, int height) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        
        dialog.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                dialog.setShape(
                        new java.awt.geom.RoundRectangle2D.Double(0, 0, dialog.getWidth(), dialog.getHeight(), 24, 24));
            }
        });
        return dialog;
    }

    private JPanel createDialogPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(25, 22, 30));
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
                
                g2.setColor(new Color(80, 60, 70, 100));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 24, 24));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private GridBagConstraints createDialogGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField() {
            {
                addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent e) {
                        repaint();
                    }

                    public void focusLost(java.awt.event.FocusEvent e) {
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(120, 120, 140));
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                    g2.drawString(placeholder, getInsets().left + 2, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(300, 44));
        field.setBackground(new Color(36, 33, 45));
        field.setForeground(StyleConfig.TEXT_COLOR);
        field.setCaretColor(StyleConfig.TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 70, 100), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField() {
            private Rectangle eyeBounds = new Rectangle();
            private boolean passwordVisible = false;

            {
                char defaultEchoChar = PASSWORD_MASK;
                setEchoChar(defaultEchoChar);
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (eyeBounds.contains(e.getPoint())) {
                            passwordVisible = !passwordVisible;
                            setEchoChar(passwordVisible ? (char) 0 : defaultEchoChar);
                            repaint();
                        }
                    }
                });
                addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                    public void mouseMoved(java.awt.event.MouseEvent e) {
                        if (eyeBounds.contains(e.getPoint())) {
                            setCursor(new Cursor(Cursor.HAND_CURSOR));
                        } else {
                            setCursor(new Cursor(Cursor.TEXT_CURSOR));
                        }
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int eyeWidth = 20;
                int eyeHeight = 20;
                int eyeX = getWidth() - eyeWidth - 10;
                int eyeY = (getHeight() - eyeHeight) / 2;
                eyeBounds = new Rectangle(eyeX, eyeY, eyeWidth, eyeHeight);

                Color eyeColor = passwordVisible ? StyleConfig.PRIMARY_COLOR : StyleConfig.TEXT_SECONDARY;
                com.playtrack.ui.components.UIUtils.drawEyeIcon(g2, eyeX, eyeY, eyeWidth, eyeHeight, eyeColor, passwordVisible);

                g2.dispose();
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(300, 44));
        field.setBackground(new Color(36, 33, 45));
        field.setForeground(StyleConfig.TEXT_COLOR);
        field.setCaretColor(StyleConfig.TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 70, 100), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 40)));
        return field;
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setFont(StyleConfig.FONT_SMALL);
        label.setForeground(StyleConfig.ERROR_COLOR);
        return label;
    }

    private JButton createGradientButton(String text) {
        // Shared rounded gradient button style for auth dialogs.
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gp = new GradientPaint(0, 0, new Color(211, 64, 69), getWidth(), 0,
                        new Color(220, 130, 50));
                g2.setPaint(isEnabled() ? gp : new Color(80, 80, 90));
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(getText());
                g2.drawString(getText(), (getWidth() - tw) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(300, 44));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createCancelLink(JDialog dialog) {
        JLabel link = new JLabel("← Back to Login", SwingConstants.CENTER);
        link.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        link.setForeground(StyleConfig.TEXT_LIGHT);
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dialog.dispose();
            }

            public void mouseEntered(java.awt.event.MouseEvent e) {
                link.setForeground(StyleConfig.TEXT_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                link.setForeground(StyleConfig.TEXT_LIGHT);
            }
        });
        return link;
    }

    private JPanel createOtpBoxRow(JTextField[] fields, Runnable onFill) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        panel.setOpaque(false);

        for (int i = 0; i < 6; i++) {
            final int index = i;
            JTextField f = new JTextField() {
                private boolean glowing = false;

                {
                    setOpaque(false);
                    setFont(new Font("Segoe UI", Font.BOLD, 22));
                    setHorizontalAlignment(JTextField.CENTER);
                    setForeground(new Color(230, 230, 240));
                    setCaretColor(new Color(230, 230, 240));
                    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

                    addFocusListener(new java.awt.event.FocusAdapter() {
                        public void focusGained(java.awt.event.FocusEvent e) {
                            glowing = true;
                            repaint();
                        }
                        public void focusLost(java.awt.event.FocusEvent e) {
                            glowing = false;
                            repaint();
                        }
                    });
                }

                @Override
                protected void paintBorder(Graphics g) {
                    
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();
                    float strokeWidth = 3f;
                    float pad = strokeWidth; 

                    g2.setColor(new Color(36, 33, 45));
                    g2.fill(new java.awt.geom.RoundRectangle2D.Float(pad, pad, w - pad * 2, h - pad * 2, 12, 12));

                    if (glowing) {
                        g2.setColor(new Color(StyleConfig.PRIMARY_COLOR.getRed(), StyleConfig.PRIMARY_COLOR.getGreen(), StyleConfig.PRIMARY_COLOR.getBlue(), 80));
                        g2.setStroke(new BasicStroke(strokeWidth + 1f));
                        g2.draw(new java.awt.geom.RoundRectangle2D.Float(pad, pad, w - pad * 2, h - pad * 2, 12, 12));
                        
                        g2.setColor(StyleConfig.PRIMARY_LIGHT);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.draw(new java.awt.geom.RoundRectangle2D.Float(pad, pad, w - pad * 2, h - pad * 2, 12, 12));
                    } else {
                        g2.setColor(new Color(80, 70, 100));
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.draw(new java.awt.geom.RoundRectangle2D.Float(pad, pad, w - pad * 2, h - pad * 2, 12, 12));
                    }

                    g2.dispose();
                    super.paintComponent(g);
                }
            };

            f.setPreferredSize(new Dimension(46, 54));
            
            ((javax.swing.text.AbstractDocument) f.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
                    if (text == null) return;
                    text = text.replaceAll("[^0-9]", "");
                    
                    if (text.isEmpty()) {
                        super.replace(fb, offset, length, "", attrs);
                        return;
                    }
                    
                    int currentLen = fb.getDocument().getLength();
                    int maxAllowed = 1 - currentLen + length;

                    if (text.length() > maxAllowed) {
                        if (text.length() > 1) {
                            int pasteIdx = 0;
                            for (int j = index; j < 6 && pasteIdx < text.length(); j++) {
                                fields[j].setText(String.valueOf(text.charAt(pasteIdx++)));
                            }
                            int focusIdx = Math.min(5, index + text.length() - 1);
                            if (focusIdx == 5 && fields[5].getText().length() == 1 && onFill != null) {
                                fields[5].requestFocusInWindow();
                                SwingUtilities.invokeLater(onFill);
                            } else {
                                fields[focusIdx].requestFocusInWindow();
                            }
                            return;
                        }
                        fb.replace(0, currentLen, text.substring(0, 1), attrs);
                    } else {
                        super.replace(fb, offset, length, text, attrs);
                    }

                    if (fb.getDocument().getLength() == 1) {
                        if (index < 5) {
                            SwingUtilities.invokeLater(() -> fields[index + 1].requestFocusInWindow());
                        } else {
                            if (onFill != null) SwingUtilities.invokeLater(onFill);
                        }
                    }
                }
            });

            f.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                        if (f.getText().isEmpty() && index > 0) {
                            fields[index - 1].setText("");
                            fields[index - 1].requestFocusInWindow();
                        }
                    } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_LEFT) {
                        if (index > 0) fields[index - 1].requestFocusInWindow();
                    } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_RIGHT) {
                        if (index < 5) fields[index + 1].requestFocusInWindow();
                    }
                }
            });

            fields[i] = f;
            panel.add(f);
        }

        return panel;
    }

    private void showOtpDialog(String generatedOtp, boolean simulatedDelivery) {
        JDialog otpDialog = createStyledDialog("Email Verification", 470, 430);
        JPanel dialogPanel = createDialogPanel();
        GridBagConstraints gbc = createDialogGbc();
        gbc.anchor = GridBagConstraints.NORTH;

        JLabel emailIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setColor(new Color(211, 64, 69, 25));
                g2.fillOval(cx - 40, cy - 40, 80, 80);
                g2.setColor(new Color(211, 64, 69, 50));
                g2.fillOval(cx - 30, cy - 30, 60, 60);
                g2.setColor(new Color(211, 64, 69));
                g2.fillOval(cx - 22, cy - 22, 44, 44);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRect(cx - 12, cy - 6, 24, 16);
                g2.drawLine(cx - 12, cy - 6, cx, cy + 4);
                g2.drawLine(cx, cy + 4, cx + 12, cy - 6);
                g2.dispose();
            }
        };
        emailIcon.setPreferredSize(new Dimension(86, 86));
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 40, 4, 40);
        dialogPanel.add(emailIcon, gbc);

        JLabel title = new JLabel("Check Your Email", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(StyleConfig.TEXT_COLOR);
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 40, 2, 40);
        dialogPanel.add(title, gbc);

        JLabel desc = new JLabel(
                "<html><center>We've sent a verification code to<br><b>" + pendingEmail + "</b></center></html>",
                SwingConstants.CENTER);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(StyleConfig.TEXT_SECONDARY);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 40, 8, 40);
        dialogPanel.add(desc, gbc);

        String otpHintText = simulatedDelivery
                ? "<html><center>SMTP is not configured. Use this local code for now:<br><b style='color:#d34045; font-size:16px;'>"
                        + generatedOtp + "</b></center></html>"
                : "<html><center>Please check your inbox (and spam folder) for the code.</center></html>";
        JLabel otpHint = new JLabel(otpHintText, SwingConstants.CENTER);
        otpHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        otpHint.setForeground(StyleConfig.TEXT_SECONDARY);
        gbc.gridy = 3;
        gbc.insets = new Insets(2, 40, 10, 40);
        dialogPanel.add(otpHint, gbc);

        JTextField[] otpFields = new JTextField[6];
        JPanel otpControls = createOtpBoxRow(otpFields, () -> {
        });
        gbc.gridy = 4;
        gbc.insets = new Insets(2, 40, 8, 40);
        dialogPanel.add(otpControls, gbc);

        JLabel resendLabel = new JLabel(
                "<html>Didn't receive a code? <font color='" + String.format("#%06x", StyleConfig.PRIMARY_COLOR.getRGB() & 0xFFFFFF) + "'><b>Resend</b></font></html>",
                SwingConstants.CENTER);
        resendLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resendLabel.setForeground(StyleConfig.TEXT_LIGHT);
        resendLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resendLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                OtpService otpService = com.playtrack.util.SpringContext.getBean(OtpService.class);
                boolean sent = otpService.sendOtp(pendingEmail);
                if (!sent) {
                    JOptionPane.showMessageDialog(otpDialog,
                            otpService.getLastDeliveryMessage(),
                            "Resend Failed",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (otpService.wasLastDeliverySimulated()) {
                    showLocalOtpNotice(otpDialog, pendingEmail, otpService.getCurrentOtp());
                } else {
                    JOptionPane.showMessageDialog(otpDialog, "Verification code resent to " + pendingEmail);
                }
                for (JTextField f : otpFields) f.setText("");
                otpFields[0].requestFocusInWindow();
            }
        });
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 40, 6, 40);
        dialogPanel.add(resendLabel, gbc);

        JLabel errorLabel = createErrorLabel();
        errorLabel.setPreferredSize(new Dimension(320, 20));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 40, 6, 40);
        dialogPanel.add(errorLabel, gbc);

        // Final confirmation button for OTP registration flow.
        JButton verifyBtn = createGradientButton("VERIFY & CREATE ACCOUNT");
        verifyBtn.addActionListener(ev -> {
            StringBuilder sb = new StringBuilder();
            for (JTextField f : otpFields) sb.append(f.getText());
            String enteredOtp = sb.toString().trim();
            if (enteredOtp.length() < 6) {
                errorLabel.setText("Please enter the 6-digit OTP");
                return;
            }
            OtpService otpService = com.playtrack.util.SpringContext.getBean(OtpService.class);
            if (otpService.verifyOtp(pendingEmail, enteredOtp)) {
                otpService.clearOtp();
                if (authService.register(pendingUsername, pendingEmail, pendingPassword)) {
                    otpDialog.dispose();
                    JOptionPane.showMessageDialog(this,
                            "Registration successful! Please sign in.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    cardLayout.show(cardsPanel, "login");
                } else {
                    errorLabel.setText("Registration failed. Try again.");
                }
            } else {
                errorLabel.setText("Invalid or expired code. Try again.");
                for (JTextField f : otpFields) f.setText("");
                otpFields[0].requestFocusInWindow();
            }
        });
        gbc.gridy = 7;
        gbc.insets = new Insets(3, 50, 18, 50);
        dialogPanel.add(verifyBtn, gbc);

        gbc.gridy = 8;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        dialogPanel.add(Box.createVerticalGlue(), gbc);

        otpDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                otpFields[0].requestFocusInWindow();
            }
        });

        otpDialog.setContentPane(dialogPanel);
        otpDialog.setVisible(true);
    }

    
    
    
    private static class CinematicBackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private BufferedImage bgImage;

        CinematicBackgroundPanel() {
            setOpaque(true);
            setBackground(new Color(15, 12, 18));

            
            try {
                InputStream is = getClass().getClassLoader().getResourceAsStream("resources/auth_bg.png");
                if (is != null) {
                    bgImage = ImageIO.read(is);
                    is.close();
                } else {
                    System.err.println("auth_bg.png not found in resources");
                }
            } catch (Exception e) {
                System.err.println("Failed to load auth_bg.png: " + e.getMessage());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();

            if (bgImage != null) {
                
                double panelRatio = (double) w / h;
                double imgRatio = (double) bgImage.getWidth() / bgImage.getHeight();

                int drawW, drawH, drawX, drawY;
                if (panelRatio > imgRatio) {
                    
                    drawW = w;
                    drawH = (int) (w / imgRatio);
                    drawX = 0;
                    drawY = (h - drawH) / 2;
                } else {
                    
                    drawH = h;
                    drawW = (int) (h * imgRatio);
                    drawX = (w - drawW) / 2;
                    drawY = 0;
                }

                g2.drawImage(bgImage, drawX, drawY, drawW, drawH, null);

                
                
                java.awt.GradientPaint overlayGrad = new java.awt.GradientPaint(
                        0, 0, new Color(10, 8, 15, 40),
                        w, 0, new Color(10, 8, 15, 230));
                g2.setPaint(overlayGrad);
                g2.fillRect(0, 0, w, h);
            } else {
                
                GradientPaint bgGrad = new GradientPaint(0, 0, new Color(15, 12, 18),
                        w, h, new Color(25, 18, 28));
                g2.setPaint(bgGrad);
                g2.fillRect(0, 0, w, h);
            }

            g2.dispose();
        }
    }
}
