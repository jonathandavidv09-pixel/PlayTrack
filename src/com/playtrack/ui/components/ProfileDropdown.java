package com.playtrack.ui.components;

import com.playtrack.model.User;
import com.playtrack.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProfileDropdown extends JPopupMenu {

    private JLabel usernameLabel;
    private JLabel emailLabel;

    public ProfileDropdown(Runnable onProfile, Runnable onLogout) {
        setBackground(StyleConfig.BACKGROUND_COLOR);
        setBorder(BorderFactory.createLineBorder(StyleConfig.BORDER_COLOR, 1));
        setOpaque(true);

        // Main container
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // User Info Section
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setOpaque(false);
        userInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameLabel = new JLabel("username");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        usernameLabel.setForeground(new Color(245, 218, 140)); // gold-ish color

        emailLabel = new JLabel("email");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailLabel.setForeground(StyleConfig.TEXT_COLOR);

        userInfoPanel.add(usernameLabel);
        userInfoPanel.add(Box.createVerticalStrut(5));
        userInfoPanel.add(emailLabel);

        container.add(userInfoPanel);
        container.add(Box.createVerticalStrut(20));

        // Menu Items
        PillButton profileBtn = new PillButton("Profile", createVectorIcon(1, StyleConfig.TEXT_COLOR),
                StyleConfig.TEXT_COLOR, () -> {
                    setVisible(false);
                    onProfile.run();
                });
        profileBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(profileBtn);
        container.add(Box.createVerticalStrut(10));

        PillButton settingsBtn = new PillButton("Settings", createVectorIcon(2, StyleConfig.TEXT_COLOR),
                StyleConfig.TEXT_COLOR, () -> {
                    setVisible(false);
                    Window window = SwingUtilities.getWindowAncestor(this.getInvoker());
                    if (window instanceof Frame) {
                        new com.playtrack.ui.settings.SettingsDialog((Frame) window).setVisible(true);
                    }
                });
        settingsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(settingsBtn);
        container.add(Box.createVerticalStrut(10)); // Space before logout

        // Logout
        PillButton logoutBtn = new PillButton("Logout", createVectorIcon(3, StyleConfig.ERROR_COLOR),
                StyleConfig.ERROR_COLOR, () -> {
                    setVisible(false);
                    onLogout.run();
                });
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(logoutBtn);

        add(container);
    }

    @Override
    public void show(Component invoker, int x, int y) {
        // Refresh user details before showing
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            usernameLabel.setText(user.getUsername());
            emailLabel.setText(
                    user.getEmail() != null && !user.getEmail().isEmpty() ? user.getEmail() : "no email provided");
        }
        super.show(invoker, x, y);
    }

    // Custom Pill Button
    private class PillButton extends JPanel {
        private boolean hovered = false;

        public PillButton(String text, Icon icon, Color textColor, Runnable action) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setMaximumSize(new Dimension(250, 45));
            setPreferredSize(new Dimension(250, 45));
            setMinimumSize(new Dimension(250, 45));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel lbl = new JLabel(text, icon, SwingConstants.LEFT);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setForeground(textColor);
            lbl.setIconTextGap(15);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
            add(lbl, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                public void mouseClicked(MouseEvent e) {
                    action.run();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (hovered) {
                g2.setColor(StyleConfig.CARD_BACKGROUND);
            } else {
                g2.setColor(StyleConfig.BACKGROUND_LIGHT);
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
        }
    }

    private Icon createVectorIcon(int type, Color color) {
        return new VectorIcon(type, color);
    }

    private static class VectorIcon implements Icon {
        private final int type;
        private final Color color;

        public VectorIcon(int type, Color color) {
            this.type = type;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.translate(x, y);

            if (type == 1) { 
                g2.drawOval(4, 2, 8, 8);
                g2.drawArc(1, 12, 14, 10, 0, 180);
            } else if (type == 2) { 
                g2.drawOval(5, 5, 6, 6);
                for (int pt = 0; pt < 8; pt++) {
                    g2.rotate(Math.PI / 4, 8, 8);
                    g2.drawLine(8, 1, 8, 3);
                }
            } else if (type == 3) {
                g2.drawRect(2, 2, 8, 12);
                g2.drawLine(10, 2, 14, 4);
                g2.drawLine(14, 4, 14, 14);
                g2.drawLine(10, 14, 14, 14);
                g2.drawLine(6, 8, 6, 8); 
            }

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 16; }

        @Override
        public int getIconHeight() { return 16; }
    }
}
