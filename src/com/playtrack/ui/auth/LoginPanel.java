package com.playtrack.ui.auth;

import com.playtrack.ui.components.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class LoginPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private PlaceholderTextField usernameField;
    private PlaceholderPasswordField passwordField;
    private RoundedButton loginButton;
    private JLabel errorLabel;
    private JLabel forgotPassword;

    public LoginPanel(ActionListener loginAction, ActionListener switchToRegisterAction, Runnable forgotPasswordAction) {
        setLayout(new GridBagLayout());
        setOpaque(false);
        Dimension fixedPanel = new Dimension(420, 530);
        setPreferredSize(fixedPanel);
        setMinimumSize(fixedPanel);
        setMaximumSize(fixedPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        
        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        tabHeader.setOpaque(false);
        Dimension tabHeaderSize = new Dimension(330, 36);
        tabHeader.setPreferredSize(tabHeaderSize);
        tabHeader.setMinimumSize(tabHeaderSize);
        tabHeader.setMaximumSize(tabHeaderSize);

        JLabel loginTab = new JLabel("LOGIN");
        loginTab.setFont(new Font("Segoe UI", Font.BOLD, 22));
        loginTab.setForeground(StyleConfig.PRIMARY_COLOR);
        loginTab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginTab.setPreferredSize(new Dimension(118, 32));
        loginTab.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel divider = new JLabel("  |  ");
        divider.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        divider.setForeground(StyleConfig.TEXT_LIGHT);
        divider.setPreferredSize(new Dimension(34, 32));
        divider.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel registerTab = new JLabel("REGISTER");
        registerTab.setFont(new Font("Segoe UI", Font.BOLD, 22));
        registerTab.setForeground(StyleConfig.TEXT_LIGHT);
        registerTab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerTab.setPreferredSize(new Dimension(168, 32));
        registerTab.setHorizontalAlignment(SwingConstants.LEFT);
        registerTab.addMouseListener(new MouseAdapter() {
            // Button action: switch from login to registration.
            public void mouseClicked(MouseEvent e) {
                switchToRegisterAction.actionPerformed(null);
            }
        });

        tabHeader.add(loginTab);
        tabHeader.add(divider);
        tabHeader.add(registerTab);

        gbc.gridy = 0;
        gbc.insets = new Insets(30, 30, 20, 30);
        add(tabHeader, gbc);

        
        JLabel emailLabel = new JLabel("Email or Username");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailLabel.setForeground(StyleConfig.TEXT_SECONDARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 35, 5, 35);
        add(emailLabel, gbc);

        usernameField = new PlaceholderTextField("Enter your Email or Username", "EMAIL");
        usernameField.setPreferredSize(new Dimension(330, 42));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 35, 12, 35);
        add(usernameField, gbc);

        
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passLabel.setForeground(StyleConfig.TEXT_SECONDARY);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 35, 5, 35);
        add(passLabel, gbc);

        passwordField = new PlaceholderPasswordField("Enter your Password", "LOCK");
        passwordField.setPreferredSize(new Dimension(330, 42));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 35, 8, 35);
        add(passwordField, gbc);

        
        JPanel rememberForgotRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rememberForgotRow.setOpaque(false);
        Dimension forgotRowSize = new Dimension(330, 22);
        rememberForgotRow.setPreferredSize(forgotRowSize);
        rememberForgotRow.setMinimumSize(forgotRowSize);
        // Forgot Password? link.
        final String forgotPasswordText = "Forgot Password?";
        forgotPassword = new JLabel(forgotPasswordText, SwingConstants.RIGHT);
        forgotPassword.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 13));
        forgotPassword.setForeground(StyleConfig.SECONDARY_COLOR);
        forgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPassword.setPreferredSize(new Dimension(160, 22));
        forgotPassword.setMinimumSize(new Dimension(160, 22));
        forgotPassword.addMouseListener(new MouseAdapter() {
            // Button action: open forgot-password flow.
            public void mouseClicked(MouseEvent e) {
                if (forgotPasswordAction != null) {
                    forgotPasswordAction.run();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                forgotPassword.setText("<html><u>" + forgotPasswordText + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                forgotPassword.setText(forgotPasswordText);
            }
        });
        rememberForgotRow.add(forgotPassword);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 35, 15, 35);
        add(rememberForgotRow, gbc);

        // Error message label (initially empty).
        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setForeground(StyleConfig.ERROR_COLOR);
        errorLabel.setFont(StyleConfig.FONT_SMALL);
        errorLabel.setPreferredSize(new Dimension(330, 32));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 35, 5, 35);
        add(errorLabel, gbc);

        
        // Primary login action button.
        loginButton = new RoundedButton("SIGN IN", StyleConfig.PRIMARY_COLOR, 22);
        loginButton.setGradient(StyleConfig.SECONDARY_COLOR);
        loginButton.setPreferredSize(new Dimension(330, 44));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginButton.setForeground(Color.WHITE);
        // Button action: submit login credentials.
        loginButton.addActionListener(loginAction);
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 35, 15, 35);
        add(loginButton, gbc);

        JPanel createAccRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        createAccRow.setOpaque(false);
        // "New to Playtrack?" label.
        JLabel newTo = new JLabel("New to Playtrack?");
        newTo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        newTo.setForeground(StyleConfig.TEXT_LIGHT);
        createAccRow.add(newTo);
        // Create an account button.
        final String createAccountText = "Create an account.";
        JLabel createAccLink = new JLabel(createAccountText);
        createAccLink.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 13));
        createAccLink.setForeground(StyleConfig.SECONDARY_COLOR);
        createAccLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createAccLink.addMouseListener(new MouseAdapter() {
            // Button action: switch from login to account creation.
            public void mouseClicked(MouseEvent e) {
                switchToRegisterAction.actionPerformed(null);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                createAccLink.setText("<html><u>" + createAccountText + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                createAccLink.setText(createAccountText);
            }
        });
        createAccRow.add(createAccLink);

        gbc.gridy = 8;
        gbc.insets = new Insets(6, 35, 25, 35);
        add(createAccRow, gbc);
    }

    // Start: social sign-in button function.
    private JButton createSocialButton(String iconLetter, String name, Color iconColor) {
        // Reusable social sign-in style button.
        JButton btn = new JButton(name) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                
                g2.setColor(new Color(35, 32, 45));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                
                g2.setColor(new Color(70, 65, 85));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));

                
                g2.setColor(iconColor);
                g2.fillOval(16, getHeight() / 2 - 8, 16, 16);

                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                if (!iconLetter.isEmpty()) {
                    g2.drawString(iconLetter, 16 + (16 - fm.stringWidth(iconLetter)) / 2,
                            getHeight() / 2 + fm.getAscent() / 2 - 1);
                } else {
                    
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    fm = g2.getFontMetrics();
                    g2.drawString("⌘", 16 + (16 - fm.stringWidth("⌘")) / 2,
                            getHeight() / 2 + fm.getAscent() / 2 - 1);
                }

                
                g2.setColor(new Color(200, 200, 215));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                fm = g2.getFontMetrics();
                g2.drawString(name, 40, getHeight() / 2 + fm.getAscent() / 2 - 1);

                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(0, 38));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    // End: social sign-in button function.
    // Custom panel background with rounded corners and semi-transparent fill.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = StyleConfig.PANEL_RADIUS;
        g2.setColor(StyleConfig.withAlpha(Color.BLACK, 50));
        g2.fill(new RoundRectangle2D.Float(0, 6, getWidth(), getHeight() - 4, arc, arc));

        g2.setPaint(new GradientPaint(0, 0, StyleConfig.withAlpha(StyleConfig.SURFACE_ELEVATED, 232),
                0, getHeight(), StyleConfig.withAlpha(StyleConfig.BACKGROUND_LIGHT, 238)));
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));

        g2.setPaint(new GradientPaint(0, 0, StyleConfig.withAlpha(Color.WHITE, 22), 0, 38,
                StyleConfig.withAlpha(Color.WHITE, 0)));
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));

        g2.setColor(StyleConfig.withAlpha(Color.WHITE, 44));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, arc, arc));

        g2.dispose();
    }

    public String getIdentifier() {
        return usernameField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

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
