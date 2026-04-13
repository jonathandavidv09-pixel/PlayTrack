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
        divider.setForeground(new Color(100, 100, 120));
        divider.setPreferredSize(new Dimension(34, 32));
        divider.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel registerTab = new JLabel("REGISTER");
        registerTab.setFont(new Font("Segoe UI", Font.BOLD, 22));
        registerTab.setForeground(new Color(160, 160, 175));
        registerTab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerTab.setPreferredSize(new Dimension(168, 32));
        registerTab.setHorizontalAlignment(SwingConstants.LEFT);
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

        
        JLabel emailLabel = new JLabel("Email or Username");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailLabel.setForeground(new Color(200, 200, 210));
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
        passLabel.setForeground(new Color(200, 200, 210));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 35, 5, 35);
        add(passLabel, gbc);

        passwordField = new PlaceholderPasswordField("Enter your Password", "LOCK");
        passwordField.setPreferredSize(new Dimension(330, 42));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 35, 8, 35);
        add(passwordField, gbc);

        
        JPanel rememberForgotRow = new JPanel(new BorderLayout());
        rememberForgotRow.setOpaque(false);

        rememberMeCheck = new JCheckBox("Remember Me");
        rememberMeCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMeCheck.setForeground(new Color(170, 170, 185));
        rememberMeCheck.setOpaque(false);
        rememberMeCheck.setFocusPainted(false);
        rememberForgotRow.add(rememberMeCheck, BorderLayout.WEST);
        // Forgot Password? link.
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
        // "New to Playtrack?" label.
        JLabel newTo = new JLabel("New to Playtrack?");
        newTo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        newTo.setForeground(new Color(160, 160, 175));
        createAccRow.add(newTo);
        // Create an account button.
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
    // Custom panel background with rounded corners and semi-transparent fill.
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
