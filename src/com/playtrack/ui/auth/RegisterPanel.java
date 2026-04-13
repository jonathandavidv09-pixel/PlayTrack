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
    private static final long serialVersionUID = 1L;
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
    // Constructor for the registration panel.
    public RegisterPanel(ActionListener registerAction, ActionListener switchToLoginAction) {
        setLayout(new GridBagLayout());
        setOpaque(false);
        Dimension fixedPanel = new Dimension(420, 600);
        setPreferredSize(fixedPanel);
        setMinimumSize(fixedPanel);
        setMaximumSize(fixedPanel);
        // GridBagConstraints for consistent component placement and spacing.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        // Tab header with "Login" and "Register" options.
        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        tabHeader.setOpaque(false);
        Dimension tabHeaderSize = new Dimension(330, 36);
        tabHeader.setPreferredSize(tabHeaderSize);
        tabHeader.setMinimumSize(tabHeaderSize);
        tabHeader.setMaximumSize(tabHeaderSize);
        // "Login" tab label with click action to switch to the login form.
        JLabel loginTab = new JLabel("LOGIN");
        loginTab.setFont(new Font("Segoe UI", Font.BOLD, 22));
        loginTab.setForeground(new Color(160, 160, 175));
        loginTab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginTab.setPreferredSize(new Dimension(118, 32));
        loginTab.setHorizontalAlignment(SwingConstants.RIGHT);
        loginTab.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                switchToLoginAction.actionPerformed(null);
            }
        });
        // Divider between the "Login" and "Register" tabs.
        JLabel divider = new JLabel("  |  ");
        divider.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        divider.setForeground(new Color(100, 100, 120));
        divider.setPreferredSize(new Dimension(34, 32));
        divider.setHorizontalAlignment(SwingConstants.CENTER);
        // "Register" tab label (active) with primary color.
        JLabel registerTab = new JLabel("REGISTER");
        registerTab.setFont(new Font("Segoe UI", Font.BOLD, 22));
        registerTab.setForeground(StyleConfig.PRIMARY_COLOR);
        registerTab.setPreferredSize(new Dimension(168, 32));
        registerTab.setHorizontalAlignment(SwingConstants.LEFT);

        tabHeader.add(loginTab);
        tabHeader.add(divider);
        tabHeader.add(registerTab);

        gbc.gridy = 0;
        gbc.insets = new Insets(25, 30, 15, 30);
        add(tabHeader, gbc);

        // Email label and input field with placeholder text.
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailLabel.setForeground(new Color(200, 200, 210));
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 35, 4, 35);
        add(emailLabel, gbc);
        // Email input field with placeholder and preferred size.
        emailField = new PlaceholderTextField("Enter your Email", "EMAIL");
        emailField.setPreferredSize(new Dimension(330, 42));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 35, 10, 35);
        add(emailField, gbc);

        // Username label and input field with placeholder text.
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(new Color(200, 200, 210));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 35, 4, 35);
        add(userLabel, gbc);
        // Username input field with placeholder and preferred size.
        usernameField = new PlaceholderTextField("Choose a Username", "USER");
        usernameField.setPreferredSize(new Dimension(330, 42));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 35, 10, 35);
        add(usernameField, gbc);

        // Password label and input field with placeholder text.
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passLabel.setForeground(new Color(200, 200, 210));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 35, 4, 35);
        add(passLabel, gbc);
        // Password input field with placeholder and preferred size.
        passwordField = new PlaceholderPasswordField("Create a Password", "LOCK");
        passwordField.setPreferredSize(new Dimension(330, 42));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 35, 4, 35);
        add(passwordField, gbc);

        // Checklist panel to display password requirement validations in real-time.
        JPanel checklistPanel = new JPanel();
        checklistPanel.setLayout(new BoxLayout(checklistPanel, BoxLayout.Y_AXIS));
        checklistPanel.setOpaque(false);
        checklistPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        // Individual checklist items for password requirements (length.
        lengthCheckLabel = createCheckLabel("At least 8 characters");
        complexCheckLabel = createCheckLabel("Uppercase, lowercase, numbers & symbols");
        commonCheckLabel = createCheckLabel("Not a common password");
        matchCheckLabel = createCheckLabel("Passwords match");
        // Adding checklist items to the checklist panel with spacing in between.
        checklistPanel.add(lengthCheckLabel);
        checklistPanel.add(Box.createVerticalStrut(4));
        checklistPanel.add(complexCheckLabel);
        checklistPanel.add(Box.createVerticalStrut(4));
        checklistPanel.add(commonCheckLabel);
        checklistPanel.add(Box.createVerticalStrut(4));
        checklistPanel.add(matchCheckLabel);

        // Confirm Password label and input field with placeholder text.
        JLabel confirmLabel = new JLabel("Confirm Password");
        confirmLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        confirmLabel.setForeground(new Color(200, 200, 210));
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 35, 4, 35);
        add(confirmLabel, gbc);
        
        confirmPasswordField = new PlaceholderPasswordField("Confirm your Password", "LOCK");
        confirmPasswordField.setPreferredSize(new Dimension(330, 42));
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 35, 12, 35);
        add(confirmPasswordField, gbc);

        gbc.gridy = 9;
        gbc.insets = new Insets(0, 35, 10, 35);
        add(checklistPanel, gbc);

        // Error message label to display registration errors.
        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setForeground(StyleConfig.ERROR_COLOR);
        errorLabel.setFont(StyleConfig.FONT_SMALL);
        errorLabel.setPreferredSize(new Dimension(330, 32));
        gbc.gridy = 10;
        gbc.insets = new Insets(0, 35, 5, 35);
        add(errorLabel, gbc);

        
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonsPanel.setOpaque(false);

        // Secondary button to switch back to the login form.
        RoundedButton backButton = new RoundedButton("Back", new Color(45, 42, 55), 22);
        backButton.setForeground(new Color(200, 200, 215));
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backButton.addActionListener(switchToLoginAction);
        buttonsPanel.add(backButton);

        // Primary button for creating a new account.
        registerButton = new RoundedButton("Create Account", StyleConfig.PRIMARY_COLOR, 22);
        registerButton.setGradient(new Color(220, 130, 50));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerButton.addActionListener(registerAction);
        buttonsPanel.add(registerButton);

        gbc.gridy = 11;
        gbc.insets = new Insets(5, 35, 15, 35);
        add(buttonsPanel, gbc);

        
        JPanel signInRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        signInRow.setOpaque(false);
        // "Already have an account?" label.
        JLabel alreadyHave = new JLabel("Already have an account?");
        alreadyHave.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        alreadyHave.setForeground(new Color(160, 160, 175));
        signInRow.add(alreadyHave);
        // "Sign in" link label with click action to switch to the login form.
        final String signInText = "Sign in.";
        JLabel signInLink = new JLabel(signInText);
        signInLink.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 13));
        signInLink.setForeground(new Color(80, 160, 230));
        signInLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signInLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                switchToLoginAction.actionPerformed(null);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                signInLink.setText("<html><u>" + signInText + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                signInLink.setText(signInText);
            }
        });
        signInRow.add(signInLink);

        gbc.gridy = 12;
        gbc.insets = new Insets(0, 35, 20, 35);
        add(signInRow, gbc);

        // Document listeners for real-time validation of password requirements as the user types in the password.
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
        // Custom icon painting based on the check state (neutral.
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

        
        g2.setColor(new Color(28, 25, 35, 180));
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));

        
        g2.setColor(new Color(80, 50, 55, 120));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 18, 18));

        g2.dispose();
    }
    // Getter methods for retrieving user input from the registration form fields.
    public String getUsername() { return usernameField.getText(); }
    public String getEmail() { return emailField.getText(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public String getConfirmPassword() { return new String(confirmPasswordField.getPassword()); }
    public void setError(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            errorLabel.setText("");
            return;
        }
        if (msg.startsWith("<html>")) {
            errorLabel.setText(msg);
            return;
        }
        String safe = msg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        errorLabel.setText("<html><center>" + wrapForLabel(safe, 44) + "</center></html>");
    }

    private String wrapForLabel(String text, int maxCharsPerLine) {
        String[] words = text.split("\\s+");
        StringBuilder wrapped = new StringBuilder();
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (line.length() == 0) {
                line.append(word);
                continue;
            }
            if (line.length() + 1 + word.length() > maxCharsPerLine) {
                if (wrapped.length() > 0) wrapped.append("<br>");
                wrapped.append(line);
                line.setLength(0);
                line.append(word);
            } else {
                line.append(' ').append(word);
            }
        }
        if (line.length() > 0) {
            if (wrapped.length() > 0) wrapped.append("<br>");
            wrapped.append(line);
        }
        return wrapped.toString();
    }
}
