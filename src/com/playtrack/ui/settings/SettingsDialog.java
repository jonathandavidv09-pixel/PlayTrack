package com.playtrack.ui.settings;

import com.playtrack.model.User;
import com.playtrack.service.AuthService;
import com.playtrack.ui.components.RoundedButton;
import com.playtrack.ui.components.StyleConfig;
import com.playtrack.ui.components.UIUtils;
import com.playtrack.util.SessionManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.text.JTextComponent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

// Settings modal for account info and password updates.
public class SettingsDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final char PASSWORD_MASK = '\u25CF';
    private AuthService authService = new AuthService();
    private JTextField emailField;
    private JTextField usernameField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private ChecklistRow lenRow;
    private ChecklistRow complexityRow;
    private ChecklistRow commonRow;
    private ChecklistRow matchRow;

    private static final Color DIALOG_BG_TOP = new Color(30, 35, 64);
    private static final Color DIALOG_BG_BOTTOM = new Color(20, 24, 46);
    private static final Color FIELD_BG = new Color(42, 52, 84);
    private static final Color FIELD_BORDER = new Color(255, 255, 255, 48);
    private static final Color FIELD_BORDER_FOCUS = new Color(253, 164, 129, 185);
    private static final Set<String> COMMON_PASSWORDS = new HashSet<>(Arrays.asList(
        "password", "password123", "123456", "12345678", "123456789", "qwerty",
        "abc123", "111111", "letmein", "admin", "welcome", "iloveyou"
    ));

    // Builds the settings dialog UI.
    public SettingsDialog(Frame parent) {
        super(parent, "SETTINGS", true);
        setSize(450, 600);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                Shape card = new RoundRectangle2D.Float(0, 0, w, h, 20, 20);
                g2.setPaint(new GradientPaint(0, 0, DIALOG_BG_TOP, 0, h, DIALOG_BG_BOTTOM));
                g2.fill(card);

                g2.setColor(new Color(255, 255, 255, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(card);
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        
        
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(22, 24, 16, 24));
        
        JLabel title = new JLabel("SETTINGS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(StyleConfig.TEXT_COLOR);
        header.add(title, BorderLayout.CENTER);
        
        final boolean[] closeHovered = { false };
        JLabel closeBtn = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int boxSize = Math.min(getWidth(), getHeight()) - 2;
                int bx = (getWidth() - boxSize) / 2;
                int by = (getHeight() - boxSize) / 2;

                if (closeHovered[0]) {
                    g2.setColor(new Color(255, 255, 255, 24));
                    g2.fillRoundRect(bx, by, boxSize, boxSize, 10, 10);
                }
                UIUtils.drawCloseIcon(g2, bx + 2, by + 2, boxSize - 4, getForeground(), 2.2f);
                g2.dispose();
            }
        };
        closeBtn.setPreferredSize(new Dimension(30, 30));
        closeBtn.setMinimumSize(new Dimension(30, 30));
        closeBtn.setMaximumSize(new Dimension(30, 30));
        closeBtn.setForeground(StyleConfig.TEXT_SECONDARY);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dispose(); }
            public void mouseEntered(MouseEvent e) { closeHovered[0] = true; closeBtn.setForeground(Color.WHITE); closeBtn.repaint(); }
            public void mouseExited(MouseEvent e) { closeHovered[0] = false; closeBtn.setForeground(StyleConfig.TEXT_SECONDARY); closeBtn.repaint(); }
        });
        header.add(closeBtn, BorderLayout.EAST);
        container.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(6, 30, 30, 30));

        User user = SessionManager.getCurrentUser();

        
        JLabel accInfoTitle = new JLabel("Account Information");
        accInfoTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
        accInfoTitle.setForeground(StyleConfig.TEXT_COLOR);
        content.add(accInfoTitle);
        content.add(Box.createVerticalStrut(12));

        emailField = createField("Email", user.getEmail(), content);
        usernameField = createField("Username", user.getUsername(), content);
        
        content.add(Box.createVerticalStrut(8));
        
        
        JLabel passTitle = new JLabel("Change Password");
        passTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
        passTitle.setForeground(StyleConfig.TEXT_COLOR);
        content.add(passTitle);
        content.add(Box.createVerticalStrut(12));

        currentPasswordField = createPasswordField("Current Password", content);
        newPasswordField = createPasswordField("New Password", content);
        confirmPasswordField = createPasswordField("Confirm Password", content);
        content.add(createPasswordChecklistPanel());
        bindPasswordChecklistListeners();
        updatePasswordChecklist();
        content.add(Box.createVerticalStrut(10));

        JScrollPane contentScroll = new JScrollPane(content);
        contentScroll.setBorder(null);
        contentScroll.setOpaque(false);
        contentScroll.getViewport().setOpaque(false);
        contentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentScroll.getVerticalScrollBar().setUnitIncrement(16);
        contentScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 30, 16, 30));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        // Save button for account changes.
        RoundedButton saveBtn = new RoundedButton("Save changes", StyleConfig.PRIMARY_COLOR, 12);
        saveBtn.setGradient(StyleConfig.PRIMARY_DARK);
        saveBtn.setPreferredSize(new Dimension(150, 38));
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        saveBtn.addActionListener(e -> saveSettings());
        btnRow.add(saveBtn);

        footer.add(btnRow, BorderLayout.CENTER);

        container.add(contentScroll, BorderLayout.CENTER);
        container.add(footer, BorderLayout.SOUTH);
        add(container);
    }

    // Creates a styled text input row.
    private JTextField createField(String label, String value, JPanel parent) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(220, 229, 248));
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(6));

        JTextField field = new JTextField(value);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setBackground(FIELD_BG);
        field.setForeground(StyleConfig.TEXT_COLOR);
        field.setCaretColor(StyleConfig.TEXT_COLOR);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        parent.add(createInputShell(field));
        parent.add(Box.createVerticalStrut(16));
        return field;
    }

    // Creates a styled password input row.
    private JPasswordField createPasswordField(String label, JPanel parent) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(220, 229, 248));
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(6));

        JPanel fieldWrapper = new JPanel(new BorderLayout());
        fieldWrapper.setBackground(FIELD_BG);
        fieldWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        fieldWrapper.setBorder(createInputBorder(false));

        JPasswordField field = new JPasswordField();
        field.setEchoChar(PASSWORD_MASK);
        field.setBackground(FIELD_BG);
        field.setForeground(StyleConfig.TEXT_COLOR);
        field.setCaretColor(StyleConfig.TEXT_COLOR);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        fieldWrapper.add(field, BorderLayout.CENTER);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                fieldWrapper.setBorder(createInputBorder(true));
            }

            @Override
            public void focusLost(FocusEvent e) {
                fieldWrapper.setBorder(createInputBorder(false));
            }
        });

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
                field.setEchoChar(!visible ? (char)0 : PASSWORD_MASK);
                eye.repaint();
            }
        });
        fieldWrapper.add(eye, BorderLayout.EAST);

        parent.add(fieldWrapper);
        parent.add(Box.createVerticalStrut(16));
        return field;
    }

    // Wraps an input in a focus-aware shell.
    private JPanel createInputShell(JTextComponent field) {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(FIELD_BG);
        shell.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        shell.setBorder(createInputBorder(false));
        shell.add(field, BorderLayout.CENTER);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                shell.setBorder(createInputBorder(true));
            }

            @Override
            public void focusLost(FocusEvent e) {
                shell.setBorder(createInputBorder(false));
            }
        });

        return shell;
    }

    // Returns the border style for input focus state.
    private Border createInputBorder(boolean focused) {
        Color stroke = focused ? FIELD_BORDER_FOCUS : FIELD_BORDER;
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(stroke, 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        );
    }

    // Creates the password rules checklist panel.
    private JPanel createPasswordChecklistPanel() {
        JPanel checklist = new JPanel();
        checklist.setLayout(new BoxLayout(checklist, BoxLayout.Y_AXIS));
        checklist.setOpaque(false);
        checklist.setAlignmentX(Component.LEFT_ALIGNMENT);
        checklist.setBorder(BorderFactory.createEmptyBorder(2, 0, 10, 0));

        lenRow = new ChecklistRow("At least 8 characters");
        complexityRow = new ChecklistRow("Uppercase, lowercase, numbers & symbols");
        commonRow = new ChecklistRow("Not a common password");
        matchRow = new ChecklistRow("Passwords match");

        checklist.add(lenRow);
        checklist.add(Box.createVerticalStrut(4));
        checklist.add(complexityRow);
        checklist.add(Box.createVerticalStrut(4));
        checklist.add(commonRow);
        checklist.add(Box.createVerticalStrut(4));
        checklist.add(matchRow);
        return checklist;
    }

    // Binds listeners that refresh checklist state.
    private void bindPasswordChecklistListeners() {
        DocumentListener listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePasswordChecklist(); }
            public void removeUpdate(DocumentEvent e) { updatePasswordChecklist(); }
            public void changedUpdate(DocumentEvent e) { updatePasswordChecklist(); }
        };
        newPasswordField.getDocument().addDocumentListener(listener);
        confirmPasswordField.getDocument().addDocumentListener(listener);
    }

    // Updates checklist status from current inputs.
    private void updatePasswordChecklist() {
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        boolean hasLen = newPass.length() >= 8;
        boolean hasComplexity = isComplexPassword(newPass);
        boolean isNotCommon = !newPass.isEmpty() && !isCommonPassword(newPass);
        boolean matches = !newPass.isEmpty() && newPass.equals(confirmPass);

        lenRow.setMet(hasLen);
        complexityRow.setMet(hasComplexity);
        commonRow.setMet(isNotCommon);
        matchRow.setMet(matches);
    }

    // Checks password complexity requirements.
    private boolean isComplexPassword(String pass) {
        if (pass == null || pass.isEmpty()) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;

        for (char ch : pass.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                hasUpper = true;
            } else if (Character.isLowerCase(ch)) {
                hasLower = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else {
                hasSymbol = true;
            }
        }
        return hasUpper && hasLower && hasDigit && hasSymbol;
    }

    // Checks if a password is in a common list.
    private boolean isCommonPassword(String pass) {
        if (pass == null || pass.isEmpty()) {
            return false;
        }
        return COMMON_PASSWORDS.contains(pass.toLowerCase(Locale.ROOT));
    }

    // Validates full password rule set.
    private boolean passesPasswordRules(String newPass, String confirmPass) {
        return newPass.length() >= 8
            && isComplexPassword(newPass)
            && !isCommonPassword(newPass)
            && newPass.equals(confirmPass);
    }

    // Row widget for one password rule.
    private static class ChecklistRow extends JPanel {
        private static final long serialVersionUID = 1L;
        private final JLabel textLabel;
        private boolean met;

        // Builds one checklist row.
        ChecklistRow(String text) {
            super(new BorderLayout(8, 0));
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            setPreferredSize(new Dimension(0, 20));

            JComponent statusIcon = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Color stroke = met ? new Color(97, 224, 154) : new Color(189, 200, 226);
                    g2.setColor(stroke);
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawOval(2, 2, 10, 10);
                    if (met) {
                        g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(5, 8, 7, 10);
                        g2.drawLine(7, 10, 11, 5);
                    }
                    g2.dispose();
                }
            };
            statusIcon.setPreferredSize(new Dimension(14, 14));

            textLabel = new JLabel(text);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            textLabel.setForeground(new Color(205, 214, 236));

            add(statusIcon, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);
        }

        // Marks this rule as met/unmet.
        void setMet(boolean met) {
            this.met = met;
            textLabel.setForeground(met ? new Color(180, 240, 204) : new Color(205, 214, 236));
            repaint();
        }
    }

    // Saves settings after validation.
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
            if (!passesPasswordRules(newPass, confPass)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Password must be 8+ chars, include uppercase/lowercase/number/symbol,\nnot be a common password, and match confirmation.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
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
