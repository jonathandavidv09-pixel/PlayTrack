package com.playtrack.ui.settings;

import com.playtrack.model.User;
import com.playtrack.service.AuthService;
import com.playtrack.ui.components.RoundedButton;
import com.playtrack.ui.components.StyleConfig;
import com.playtrack.ui.components.UIUtils;
import com.playtrack.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SettingsDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private AuthService authService = new AuthService();
    private JTextField emailField;
    private JTextField usernameField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public SettingsDialog(Frame parent) {
        super(parent, "SETTINGS", true);
        setSize(450, 600);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(StyleConfig.BACKGROUND_COLOR);
        container.setBorder(BorderFactory.createLineBorder(StyleConfig.BORDER_COLOR, 1));
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("SETTINGS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(StyleConfig.TEXT_COLOR);
        header.add(title, BorderLayout.CENTER);
        
        JLabel closeBtn = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIUtils.drawCloseIcon(g2, 0, 0, getWidth(), getForeground(), 2.0f);
                g2.dispose();
            }
        };
        closeBtn.setPreferredSize(new Dimension(20, 20));
        closeBtn.setForeground(StyleConfig.TEXT_SECONDARY);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dispose(); }
            public void mouseEntered(MouseEvent e) { closeBtn.setForeground(StyleConfig.PRIMARY_COLOR); }
            public void mouseExited(MouseEvent e) { closeBtn.setForeground(StyleConfig.TEXT_SECONDARY); }
        });
        header.add(closeBtn, BorderLayout.EAST);
        container.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(StyleConfig.BACKGROUND_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(10, 30, 30, 30));

        User user = SessionManager.getCurrentUser();

        // Account Information
        JLabel accInfoTitle = new JLabel("Account Information");
        accInfoTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        accInfoTitle.setForeground(StyleConfig.TEXT_COLOR);
        content.add(accInfoTitle);
        content.add(Box.createVerticalStrut(15));

        emailField = createField("Email", user.getEmail(), content);
        usernameField = createField("Username", user.getUsername(), content);
        
        content.add(Box.createVerticalStrut(15));
        
        // Change Password
        JLabel passTitle = new JLabel("Change Password");
        passTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passTitle.setForeground(StyleConfig.TEXT_COLOR);
        content.add(passTitle);
        content.add(Box.createVerticalStrut(15));

        currentPasswordField = createPasswordField("Current Password", content);
        newPasswordField = createPasswordField("New Password", content);
        confirmPasswordField = createPasswordField("Confirm Password", content);

        content.add(Box.createVerticalGlue());

        // Save Button Row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        RoundedButton saveBtn = new RoundedButton("Save changes", StyleConfig.PRIMARY_COLOR, 12);
        saveBtn.setPreferredSize(new Dimension(140, 36));
        saveBtn.addActionListener(e -> saveSettings());
        btnRow.add(saveBtn);
        
        content.add(btnRow);

        container.add(content, BorderLayout.CENTER);
        add(container);
    }

    private JTextField createField(String label, String value, JPanel parent) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(StyleConfig.TEXT_SECONDARY);
        parent.add(lbl);

        JTextField field = new JTextField(value);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBackground(StyleConfig.BACKGROUND_LIGHT);
        field.setForeground(StyleConfig.TEXT_COLOR);
        field.setCaretColor(StyleConfig.TEXT_COLOR);
        field.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        parent.add(field);
        parent.add(Box.createVerticalStrut(15));
        return field;
    }

    private JPasswordField createPasswordField(String label, JPanel parent) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(StyleConfig.TEXT_SECONDARY);
        parent.add(lbl);

        JPanel fieldWrapper = new JPanel(new BorderLayout());
        fieldWrapper.setBackground(StyleConfig.BACKGROUND_LIGHT);
        fieldWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPasswordField field = new JPasswordField();
        field.setBackground(StyleConfig.BACKGROUND_LIGHT);
        field.setForeground(StyleConfig.TEXT_COLOR);
        field.setCaretColor(StyleConfig.TEXT_COLOR);
        field.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        fieldWrapper.add(field, BorderLayout.CENTER);

        JLabel eye = new JLabel() {
            boolean isHovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                boolean visible = field.getEchoChar() == (char)0;
                Color c = visible ? StyleConfig.PRIMARY_COLOR : (isHovered ? StyleConfig.TEXT_COLOR : StyleConfig.TEXT_SECONDARY);
                UIUtils.drawEyeIcon(g2, 0, 0, getWidth(), getHeight(), c, visible);
                g2.dispose();
            }
        };
        eye.setPreferredSize(new Dimension(24, 24));
        eye.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eye.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        eye.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                boolean visible = field.getEchoChar() == (char)0;
                field.setEchoChar(!visible ? (char)0 : '•');
                eye.repaint();
            }
        });
        fieldWrapper.add(eye, BorderLayout.EAST);

        parent.add(fieldWrapper);
        parent.add(Box.createVerticalStrut(15));
        return field;
    }

    private void saveSettings() {
        User user = SessionManager.getCurrentUser();
        String mail = emailField.getText().trim();
        String userNm = usernameField.getText().trim();
        
        String curPass = new String(currentPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confPass = new String(confirmPasswordField.getPassword());
        
        if (mail.isEmpty() || userNm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email and Username cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!userNm.equals(user.getUsername()) && authService.isUsernameTaken(userNm)) {
            JOptionPane.showMessageDialog(this, "the username is already taken", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean changingPassword = !newPass.isEmpty();
        if (changingPassword) {
            if (curPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your current password to set a new one.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newPass.equals(confPass)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        user.setEmail(mail);
        user.setUsername(userNm);
        
        boolean success = authService.updateSettings(user, curPass, newPass);
        if (success) {
            JOptionPane.showMessageDialog(this, "Settings updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update settings. Please verify your current password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
