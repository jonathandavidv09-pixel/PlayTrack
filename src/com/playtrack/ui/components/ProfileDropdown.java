package com.playtrack.ui.components;

import com.playtrack.model.User;
import com.playtrack.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
// Custom profile dropdown component.
public class ProfileDropdown extends JPopupMenu {
    private static final long serialVersionUID = 1L;

    private JLabel usernameLabel;
    private JLabel emailLabel;
    // Constructor.
    public ProfileDropdown(Runnable onProfile, Runnable onLogout) {
        setLightWeightPopupEnabled(true);
        setBackground(new Color(0, 0, 0, 0));
        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(false);
        setLayout(new BorderLayout());

        // Container panel with custom painting for the dropdown background and border.
        JPanel container = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(StyleConfig.withAlpha(Color.BLACK, 56));
                g2.fillRoundRect(0, 4, w, h - 2, 20, 20);

                g2.setPaint(new GradientPaint(0, 0, StyleConfig.SURFACE_ELEVATED, 0, h, StyleConfig.SURFACE_COLOR));
                g2.fillRoundRect(0, 0, w, h, 18, 18);

                g2.setPaint(new GradientPaint(0, 0, StyleConfig.withAlpha(Color.WHITE, 18), 0, 24,
                        StyleConfig.withAlpha(Color.WHITE, 0)));
                g2.fillRoundRect(0, 0, w, h, 18, 18);

                
                g2.setColor(new Color(255, 255, 255, 46));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);
                g2.dispose();
            }
        };
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setOpaque(false);
        userInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Labels for displaying the current user's username and email at the top of the dropdown.
        usernameLabel = new JLabel("username");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        usernameLabel.setForeground(StyleConfig.SECONDARY_COLOR);
        // Email label with default text "email" that will.
        emailLabel = new JLabel("email");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailLabel.setForeground(StyleConfig.TEXT_COLOR);

        userInfoPanel.add(usernameLabel);
        userInfoPanel.add(Box.createVerticalStrut(5));
        userInfoPanel.add(emailLabel);

        container.add(userInfoPanel);
        container.add(Box.createVerticalStrut(12));

        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 8), getWidth(), 0, new Color(255, 255, 255, 32)));
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(0, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(divider);
        container.add(Box.createVerticalStrut(12));

        // Profile shortcut button inside the account dropdown.
        PillButton profileBtn = new PillButton("Profile", 1, StyleConfig.TEXT_COLOR, StyleConfig.PRIMARY_COLOR, () -> {
                    setVisible(false);
                    onProfile.run();
                });
        profileBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(profileBtn);
        container.add(Box.createVerticalStrut(10));

        // Settings shortcut button inside the account dropdown.
        PillButton settingsBtn = new PillButton("Settings", 2, StyleConfig.TEXT_COLOR, StyleConfig.SECONDARY_COLOR, () -> {
                    Component invoker = getInvoker();
                    Window window = invoker != null ? SwingUtilities.getWindowAncestor(invoker) : null;
                    if (!(window instanceof Frame)) {
                        Window active = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
                        if (active instanceof Frame) {
                            window = active;
                        }
                    }

                    setVisible(false);
                    if (window instanceof Frame) {
                        Frame owner = (Frame) window;
                        SwingUtilities.invokeLater(() -> new com.playtrack.ui.settings.SettingsDialog(owner).setVisible(true));
                    }
                });
        settingsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(settingsBtn);
        container.add(Box.createVerticalStrut(10)); 

        // Logout action button inside the account dropdown.
        PillButton logoutBtn = new PillButton("Logout", 3, StyleConfig.ERROR_COLOR, StyleConfig.ERROR_COLOR, () -> {
                    setVisible(false);
                    onLogout.run();
                });
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(logoutBtn);

        Dimension pref = container.getPreferredSize();
        setPreferredSize(pref);
        setPopupSize(pref);
        add(container, BorderLayout.CENTER);
    }

    @Override
    public void show(Component invoker, int x, int y) {
        
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            usernameLabel.setText(user.getUsername());
            emailLabel.setText(
                    user.getEmail() != null && !user.getEmail().isEmpty() ? user.getEmail() : "no email provided");
        }
        setPopupSize(getPreferredSize());
        super.show(invoker, x, y);
    }

    // Custom button component for the dropdown options.
    private class PillButton extends JPanel {
        private static final long serialVersionUID = 1L;
        private final String text;
        private final int iconType;
        private final Color baseTextColor;
        private final Color accentColor;
        private final JLabel lbl;
        private boolean hovered = false;

        // Start: profile dropdown option button function.
        public PillButton(String text, int iconType, Color textColor, Color accentColor, Runnable action) {
            this.text = text;
            this.iconType = iconType;
            this.baseTextColor = textColor;
            this.accentColor = accentColor;
            setLayout(new BorderLayout());
            setOpaque(false);
            setMaximumSize(new Dimension(250, 45));
            setPreferredSize(new Dimension(250, 45));
            setMinimumSize(new Dimension(250, 45));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            lbl = new JLabel(text, createVectorIcon(iconType, textColor), SwingConstants.LEFT);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setForeground(textColor);
            lbl.setIconTextGap(15);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
            add(lbl, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    if ("Logout".equals(text)) {
                        lbl.setForeground(new Color(255, 120, 120));
                    } else {
                        lbl.setForeground(Color.WHITE);
                    }
                    lbl.setIcon(createVectorIcon(iconType, lbl.getForeground()));
                    repaint();
                }

                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    lbl.setForeground(baseTextColor);
                    lbl.setIcon(createVectorIcon(iconType, baseTextColor));
                    repaint();
                }

                public void mouseClicked(MouseEvent e) {
                    // Button action: run the selected profile dropdown command.
                    action.run();
                }
            });
        }
        // End: profile dropdown option button function.

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape shape = new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                if (hovered) {
                    if ("Logout".equals(text)) {
                        g2.setColor(new Color(StyleConfig.ERROR_COLOR.getRed(), StyleConfig.ERROR_COLOR.getGreen(), StyleConfig.ERROR_COLOR.getBlue(), 34));
                    } else {
                        g2.setPaint(new GradientPaint(0, 0, StyleConfig.SURFACE_SOFT, getWidth(), 0, StyleConfig.CARD_BACKGROUND));
                    }
                } else {
                    g2.setColor(StyleConfig.withAlpha(StyleConfig.BACKGROUND_LIGHT, 216));
                }
                g2.fill(shape);

            if (hovered) {
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 10, 4, getHeight() - 20, 4, 4);
            }

            g2.setColor(hovered ? new Color(255, 255, 255, 62) : new Color(255, 255, 255, 20));
            g2.draw(shape);
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
