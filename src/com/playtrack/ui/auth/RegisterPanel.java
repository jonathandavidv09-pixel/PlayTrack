package com.playtrack.ui.auth;

import com.playtrack.ui.components.*;
import com.playtrack.util.Validator;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class RegisterPanel extends JPanel {
    private PlaceholderTextField usernameField;
    private PlaceholderTextField emailField;
    private PlaceholderPasswordField passwordField;
    private PlaceholderPasswordField confirmPasswordField;
    private RoundedButton registerButton;
    private JLabel lengthCheckLabel;
    private JLabel complexCheckLabel;
    private JLabel commonCheckLabel;
    private JLabel matchCheckLabel;
    private JLabel errorLabel;

    public RegisterPanel(ActionListener registerAction, ActionListener switchToLoginAction) {
        setLayout(new GridBagLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(380, 580));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        // ========== TAB HEADER: LOGIN | REGISTER ==========
        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        tabHeader.setOpaque(false);

        JLabel loginTab = new JLabel("LOGIN");
        loginTab.setFont(new Font("Segoe UI", Font.BOLD, 22));
        loginTab.setForeground(new Color(160, 160, 175));
        loginTab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginTab.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                switchToLoginAction.actionPerformed(null);
            }
        });

        JLabel divider = new JLabel("  |  ");
        divider.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        divider.setForeground(new Color(100, 100, 120));

        JLabel registerTab = new JLabel("REGISTER");
        registerTab.setFont(new Font("Segoe UI", Font.BOLD, 22));
        registerTab.setForeground(StyleConfig.PRIMARY_COLOR);

        tabHeader.add(loginTab);
        tabHeader.add(divider);
        tabHeader.add(registerTab);

        gbc.gridy = 0;
        gbc.insets = new Insets(25, 30, 15, 30);
        add(tabHeader, gbc);

        // ========== Email Address ==========
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailLabel.setForeground(new Color(200, 200, 210));
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 35, 4, 35);
        add(emailLabel, gbc);

        emailField = new PlaceholderTextField("Enter your Email", "✉️");
        emailField.setPreferredSize(new Dimension(300, 42));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 35, 10, 35);
        add(emailField, gbc);

        // ========== Username ==========
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(new Color(200, 200, 210));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 35, 4, 35);
        add(userLabel, gbc);

        usernameField = new PlaceholderTextField("Choose a Username", "👤");
        usernameField.setPreferredSize(new Dimension(300, 42));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 35, 10, 35);
        add(usernameField, gbc);

        // ========== Password ==========
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passLabel.setForeground(new Color(200, 200, 210));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 35, 4, 35);
        add(passLabel, gbc);

        passwordField = new PlaceholderPasswordField("Create a Password", "🔒");
        passwordField.setPreferredSize(new Dimension(300, 42));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 35, 4, 35);
        add(passwordField, gbc);

        // ========== Password Checklist ==========
        JPanel checklistPanel = new JPanel();
        checklistPanel.setLayout(new BoxLayout(checklistPanel, BoxLayout.Y_AXIS));
        checklistPanel.setOpaque(false);
        checklistPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        lengthCheckLabel = createCheckLabel("At least 8 characters");
        complexCheckLabel = createCheckLabel("Uppercase, lowercase, numbers & symbols");
        commonCheckLabel = createCheckLabel("Not a common password");
        matchCheckLabel = createCheckLabel("Passwords match");

        checklistPanel.add(lengthCheckLabel);
        checklistPanel.add(Box.createVerticalStrut(4));
        checklistPanel.add(complexCheckLabel);
        checklistPanel.add(Box.createVerticalStrut(4));
        checklistPanel.add(commonCheckLabel);
        checklistPanel.add(Box.createVerticalStrut(4));
        checklistPanel.add(matchCheckLabel);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 35, 10, 35);
        add(checklistPanel, gbc);

        // ========== Confirm Password ==========
        JLabel confirmLabel = new JLabel("Confirm Password");
        confirmLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        confirmLabel.setForeground(new Color(200, 200, 210));
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 35, 4, 35);
        add(confirmLabel, gbc);

        confirmPasswordField = new PlaceholderPasswordField("Confirm your Password", "🔒");
        confirmPasswordField.setPreferredSize(new Dimension(300, 42));
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 35, 12, 35);
        add(confirmPasswordField, gbc);

        // ========== Error label ==========
        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setForeground(StyleConfig.ERROR_COLOR);
        errorLabel.setFont(StyleConfig.FONT_SMALL);
        gbc.gridy = 10;
        gbc.insets = new Insets(0, 35, 5, 35);
        add(errorLabel, gbc);

        // ========== Buttons: Back + Create Account ==========
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonsPanel.setOpaque(false);

        RoundedButton backButton = new RoundedButton("Back", new Color(45, 42, 55), 22);
        backButton.setForeground(new Color(200, 200, 215));
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backButton.addActionListener(switchToLoginAction);
        buttonsPanel.add(backButton);

        registerButton = new RoundedButton("Create Account", StyleConfig.PRIMARY_COLOR, 22);
        registerButton.setGradient(new Color(220, 130, 50));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerButton.addActionListener(registerAction);
        buttonsPanel.add(registerButton);

        gbc.gridy = 11;
        gbc.insets = new Insets(5, 35, 15, 35);
        add(buttonsPanel, gbc);

        // ========== Already have account? Sign in. ==========
        JPanel signInRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        signInRow.setOpaque(false);

        JLabel alreadyHave = new JLabel("Already have an account?");
        alreadyHave.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        alreadyHave.setForeground(new Color(160, 160, 175));
        signInRow.add(alreadyHave);

        JLabel signInLink = new JLabel("Sign in.");
        signInLink.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 12));
        signInLink.setForeground(new Color(80, 160, 230));
        signInLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signInLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                switchToLoginAction.actionPerformed(null);
            }
        });
        signInRow.add(signInLink);

        gbc.gridy = 12;
        gbc.insets = new Insets(0, 35, 20, 35);
        add(signInRow, gbc);

        // Password strength listener
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateChecklist(); }
            public void removeUpdate(DocumentEvent e) { updateChecklist(); }
            public void changedUpdate(DocumentEvent e) { updateChecklist(); }
        });

        confirmPasswordField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateChecklist(); }
            public void removeUpdate(DocumentEvent e) { updateChecklist(); }
            public void changedUpdate(DocumentEvent e) { updateChecklist(); }
        });
    }

    private void updateChecklist() {
        String pass = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());
        
        if (pass.isEmpty() && confirm.isEmpty()) {
            resetCheckLabel(lengthCheckLabel);
            resetCheckLabel(complexCheckLabel);
            resetCheckLabel(commonCheckLabel);
            resetCheckLabel(matchCheckLabel);
            return;
        }

        boolean isLength = pass.length() >= 8;
        boolean isComplex = Validator.hasComplexity(pass);
        boolean isCommon = Validator.isCommonPassword(pass);
        boolean isMatch = !pass.isEmpty() && pass.equals(confirm);

        updateCheckLabel(lengthCheckLabel, isLength);
        updateCheckLabel(complexCheckLabel, isComplex);
        updateCheckLabel(commonCheckLabel, !isCommon && !pass.isEmpty());
        updateCheckLabel(matchCheckLabel, isMatch);
    }
    
    private JLabel createCheckLabel(String text) {
        JLabel label = new JLabel(text);
        label.setIcon(new CheckIcon(CheckState.NEUTRAL, StyleConfig.TEXT_LIGHT));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(StyleConfig.TEXT_LIGHT);
        label.setIconTextGap(6);
        return label;
    }

    private void resetCheckLabel(JLabel label) {
        label.setIcon(new CheckIcon(CheckState.NEUTRAL, StyleConfig.TEXT_LIGHT));
        label.setForeground(StyleConfig.TEXT_LIGHT);
    }

    private void updateCheckLabel(JLabel label, boolean isValid) {
        if (isValid) {
            label.setIcon(new CheckIcon(CheckState.VALID, StyleConfig.SUCCESS_COLOR));
            label.setForeground(StyleConfig.SUCCESS_COLOR);
        } else {
            label.setIcon(new CheckIcon(CheckState.INVALID, StyleConfig.ERROR_COLOR));
            label.setForeground(StyleConfig.ERROR_COLOR);
        }
    }

    private enum CheckState { NEUTRAL, VALID, INVALID }

    private static class CheckIcon implements Icon {
        private CheckState state;
        private Color color;

        public CheckIcon(CheckState state, Color color) {
            this.state = state;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int cx = x + getIconWidth() / 2;
            int cy = y + getIconHeight() / 2;

            if (state == CheckState.NEUTRAL) {
                g2.drawOval(cx - 5, cy - 5, 10, 10);
            } else if (state == CheckState.VALID) {
                g2.drawLine(cx - 4, cy, cx - 1, cy + 3);
                g2.drawLine(cx - 1, cy + 3, cx + 5, cy - 4);
            } else if (state == CheckState.INVALID) {
                g2.drawLine(cx - 4, cy - 4, cx + 4, cy + 4);
                g2.drawLine(cx + 4, cy - 4, cx - 4, cy + 4);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 16; }

        @Override
        public int getIconHeight() { return 16; }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- TRANSLUCENT dark background card ---
        g2.setColor(new Color(28, 25, 35, 180));
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));

        // Subtle border with slight red tint
        g2.setColor(new Color(80, 50, 55, 120));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 18, 18));

        g2.dispose();
    }

    public String getUsername() { return usernameField.getText(); }
    public String getEmail() { return emailField.getText(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public String getConfirmPassword() { return new String(confirmPasswordField.getPassword()); }
    public void setError(String msg) { errorLabel.setText(msg); }
}
