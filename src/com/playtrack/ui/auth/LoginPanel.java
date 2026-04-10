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
    private JCheckBox rememberMeCheck;
    private JLabel forgotPassword;

    public LoginPanel(ActionListener loginAction, ActionListener switchToRegisterAction, Runnable forgotPasswordAction) {
        setLayout(new GridBagLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(420, 530));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        // ========== TAB HEADER: LOGIN | REGISTER ==========
        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        tabHeader.setOpaque(false);

        JLabel loginTab = new JLabel("LOGIN");
        loginTab.setFont(new Font("Segoe UI", Font.BOLD, 22));
        loginTab.setForeground(StyleConfig.PRIMARY_COLOR);
        loginTab.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel divider = new JLabel("  |  ");
        divider.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        divider.setForeground(new Color(100, 100, 120));

        JLabel registerTab = new JLabel("REGISTER");
        registerTab.setFont(new Font("Segoe UI", Font.BOLD, 22));
        registerTab.setForeground(new Color(160, 160, 175));
        registerTab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerTab.addMouseListener(new MouseAdapter() {
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

        // ========== Email or Username ==========
        JLabel emailLabel = new JLabel("Email or Username");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailLabel.setForeground(new Color(200, 200, 210));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 35, 5, 35);
        add(emailLabel, gbc);

        usernameField = new PlaceholderTextField("Enter your Email or Username", "✉️");
        usernameField.setPreferredSize(new Dimension(330, 42));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 35, 12, 35);
        add(usernameField, gbc);

        // ========== Password ==========
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passLabel.setForeground(new Color(200, 200, 210));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 35, 5, 35);
        add(passLabel, gbc);

        passwordField = new PlaceholderPasswordField("Enter your Password", "🔒");
        passwordField.setPreferredSize(new Dimension(330, 42));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 35, 8, 35);
        add(passwordField, gbc);

        // ========== Remember Me + Forgot Password row ==========
        JPanel rememberForgotRow = new JPanel(new BorderLayout());
        rememberForgotRow.setOpaque(false);

        rememberMeCheck = new JCheckBox("Remember Me");
        rememberMeCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMeCheck.setForeground(new Color(170, 170, 185));
        rememberMeCheck.setOpaque(false);
        rememberMeCheck.setFocusPainted(false);
        rememberForgotRow.add(rememberMeCheck, BorderLayout.WEST);

        forgotPassword = new JLabel("Forgot Password?");
        forgotPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPassword.setForeground(new Color(211, 64, 69));
        forgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (forgotPasswordAction != null) {
            forgotPassword.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    forgotPasswordAction.run();
                }
            });
        }
        rememberForgotRow.add(forgotPassword, BorderLayout.EAST);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 35, 15, 35);
        add(rememberForgotRow, gbc);

        // ========== Error label ==========
        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setForeground(StyleConfig.ERROR_COLOR);
        errorLabel.setFont(StyleConfig.FONT_SMALL);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 35, 5, 35);
        add(errorLabel, gbc);

        // ========== SIGN IN button (gradient red) ==========
        loginButton = new RoundedButton("SIGN IN", StyleConfig.PRIMARY_COLOR, 22);
        loginButton.setGradient(new Color(220, 130, 50));
        loginButton.setPreferredSize(new Dimension(330, 44));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(loginAction);
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 35, 15, 35);
        add(loginButton, gbc);

        JPanel createAccRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        createAccRow.setOpaque(false);

        JLabel newTo = new JLabel("New to Playtrack?");
        newTo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        newTo.setForeground(new Color(160, 160, 175));
        createAccRow.add(newTo);

        final String createAccountText = "Create an account.";
        JLabel createAccLink = new JLabel(createAccountText);
        createAccLink.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 13));
        createAccLink.setForeground(new Color(80, 160, 230));
        createAccLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createAccLink.addMouseListener(new MouseAdapter() {
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

    private JButton createSocialButton(String iconLetter, String name, Color iconColor) {
        JButton btn = new JButton(name) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2.setColor(new Color(35, 32, 45));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                // Border
                g2.setColor(new Color(70, 65, 85));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));

                // Icon circle
                g2.setColor(iconColor);
                g2.fillOval(16, getHeight() / 2 - 8, 16, 16);

                // Icon letter
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                if (!iconLetter.isEmpty()) {
                    g2.drawString(iconLetter, 16 + (16 - fm.stringWidth(iconLetter)) / 2,
                            getHeight() / 2 + fm.getAscent() / 2 - 1);
                } else {
                    // Apple logo approximation
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    fm = g2.getFontMetrics();
                    g2.drawString("⌘", 16 + (16 - fm.stringWidth("⌘")) / 2,
                            getHeight() / 2 + fm.getAscent() / 2 - 1);
                }

                // Text
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

    public String getIdentifier() {
        return usernameField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public boolean isRememberMeSelected() {
        return rememberMeCheck != null && rememberMeCheck.isSelected();
    }

    public void setError(String msg) {
        errorLabel.setText(msg);
    }
}
