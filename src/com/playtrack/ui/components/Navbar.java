package com.playtrack.ui.components;

import com.playtrack.util.SessionManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class Navbar extends JPanel {
    private Runnable onHome, onLibrary, onSummary, onProfile, onLogout;
    private ProfileDropdown dropdown;
    private String activeNav = "Home";

    private JLabel usernameLabel;
    private JLabel profileIcon;
    private String username = "User";
    private String avatarPath = null;
    private java.awt.Image avatarImage = null;
    private boolean profileHovered = false;

    public Navbar(Runnable onHome, Runnable onLibrary, Runnable onSummary, Runnable onProfile, Runnable onLogout) {
        this.onHome = onHome;
        this.onLibrary = onLibrary;
        this.onSummary = onSummary;
        this.onProfile = onProfile;
        this.onLogout = onLogout;

        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(1400, 65));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        logoPanel.setOpaque(false);

        JLabel logo = new JLabel();
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.File("src/resources/logo.png"));
            int targetHeight = 50;
            int targetWidth = (int) (img.getWidth() * ((double) targetHeight / img.getHeight()));
            java.awt.Image scaled = img.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH);
            logo.setIcon(new ImageIcon(scaled));
        } catch (Exception ex) {
            logo.setText("PlayTrack");
            logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
            logo.setForeground(StyleConfig.SECONDARY_COLOR);
            ex.printStackTrace();
        }
        logo.setBorder(BorderFactory.createEmptyBorder(7, 5, 0, 0));
        logoPanel.add(logo);
        add(logoPanel, BorderLayout.WEST);

        // Navigation links
        JPanel navLinks = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navLinks.setOpaque(false);

        navLinks.add(createNavLink("Home", () -> {
            setActiveNav("Home");
            onHome.run();
        }));
        navLinks.add(createNavLink("Library", () -> {
            setActiveNav("Library");
            onLibrary.run();
        }));
        navLinks.add(createNavLink("Summary", () -> {
            setActiveNav("Summary");
            onSummary.run();
        }));

        add(navLinks, BorderLayout.CENTER);

        // Profile area
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        profilePanel.setOpaque(false);

        usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        usernameLabel.setForeground(StyleConfig.TEXT_SECONDARY);
        // Removed profilePanel.add(usernameLabel) to hide the username as requested

        profileIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int size = 40;
                int xPadding = (getWidth() - size) / 2;
                int yPadding = (getHeight() - size) / 2;

                if (profileHovered) {
                    g2.setColor(new Color(StyleConfig.PALETTE_PEACH.getRed(), StyleConfig.PALETTE_PEACH.getGreen(),
                            StyleConfig.PALETTE_PEACH.getBlue(), 35));
                    g2.fillOval(xPadding - 3, yPadding - 3, size + 6, size + 6);
                }

                if (avatarImage != null) {
                    // Clip the image drawing
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(xPadding, yPadding, size, size));
                    int imgW = avatarImage.getWidth(null);
                    int imgH = avatarImage.getHeight(null);
                    double scaleX = (double) size / imgW;
                    double scaleY = (double) size / imgH;
                    double scale = Math.max(scaleX, scaleY);
                    int dw = (int) (imgW * scale);
                    int dh = (int) (imgH * scale);
                    g2.drawImage(avatarImage, xPadding + (size - dw) / 2, yPadding + (size - dh) / 2, dw, dh, null);
                    g2.setClip(null);

                    // Draw anti-aliased rings over the edge to perfectly hide jagged clipping pixels!
                    g2.setColor(new Color(255, 255, 255, profileHovered ? 80 : 50));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new java.awt.geom.Ellipse2D.Float(xPadding + 0.5f, yPadding + 0.5f, size - 1, size - 1));

                    g2.setColor(profileHovered ? StyleConfig.SECONDARY_COLOR : StyleConfig.BORDER_COLOR);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.draw(new java.awt.geom.Ellipse2D.Float(xPadding, yPadding, size, size));
                } else {
                    g2.setPaint(new GradientPaint(xPadding, yPadding, StyleConfig.PRIMARY_COLOR, xPadding + size, yPadding + size, StyleConfig.SECONDARY_COLOR));
                    g2.fillOval(xPadding, yPadding, size, size);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                    FontMetrics fm = g2.getFontMetrics();
                    String initial = username.length() > 0 ? username.substring(0, 1).toUpperCase() : "U";
                    g2.drawString(initial, xPadding + (size - fm.stringWidth(initial)) / 2,
                            yPadding + (size + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
        };
        profileIcon.setPreferredSize(new Dimension(50, 65));
        profileIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profilePanel.add(profileIcon);

        refreshUser();

        add(profilePanel, BorderLayout.EAST);

        Runnable wrappedOnProfile = () -> {
            setActiveNav("Profile");
            onProfile.run();
        };
        dropdown = new ProfileDropdown(wrappedOnProfile, onLogout);

        profileIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showDropdown(profileIcon);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                profileHovered = true;
                profileIcon.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                profileHovered = false;
                profileIcon.repaint();
            }
        });
    }

    private JPanel createNavLink(String text, Runnable action) {
        final boolean[] hovered = { false };
        JPanel linkPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean active = activeNav.equals(text);
                if (active) {
                    g2.setPaint(new GradientPaint(0, 0,
                            new Color(StyleConfig.PALETTE_PEACH.getRed(), StyleConfig.PALETTE_PEACH.getGreen(),
                                    StyleConfig.PALETTE_PEACH.getBlue(), 35),
                            getWidth(), 0, new Color(StyleConfig.PALETTE_RED.getRed(),
                                    StyleConfig.PALETTE_RED.getGreen(), StyleConfig.PALETTE_RED.getBlue(), 28)));
                    g2.fill(new RoundRectangle2D.Float(8, 13, getWidth() - 16, 39, 18, 18));
                    g2.setColor(new Color(255, 255, 255, 95));
                    g2.draw(new RoundRectangle2D.Float(8.5f, 13.5f, getWidth() - 17, 38, 17, 17));
                    g2.setColor(StyleConfig.SECONDARY_COLOR);
                    g2.fillRoundRect(14, 30, 4, 12, 4, 4);
                } else if (hovered[0]) {
                    g2.setColor(new Color(255, 255, 255, 16));
                    g2.fill(new RoundRectangle2D.Float(8, 13, getWidth() - 16, 39, 18, 18));
                    g2.setColor(new Color(255, 255, 255, 26));
                    g2.draw(new RoundRectangle2D.Float(8.5f, 13.5f, getWidth() - 17, 38, 17, 17));
                }
                g2.dispose();
            }
        };
        linkPanel.setOpaque(false);
        linkPanel.setLayout(new GridBagLayout());
        linkPanel.setPreferredSize(new Dimension(110, 65));
        linkPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel link = new JLabel(text, SwingConstants.CENTER);
        link.setFont(new Font("Segoe UI", Font.BOLD, 14));
        link.setForeground(activeNav.equals(text) ? Color.WHITE : StyleConfig.TEXT_SECONDARY);
        linkPanel.add(link);

        linkPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered[0] = true;
                link.setForeground(activeNav.equals(text) ? Color.WHITE : StyleConfig.TEXT_COLOR);
                linkPanel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered[0] = false;
                link.setForeground(activeNav.equals(text) ? Color.WHITE : StyleConfig.TEXT_SECONDARY);
                linkPanel.repaint();
            }
        });
        return linkPanel;
    }

    public void setActiveNav(String nav) {
        this.activeNav = nav;
        Component center = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (center instanceof JPanel) {
            JPanel navLinks = (JPanel) center;
            for (Component c : navLinks.getComponents()) {
                if (c instanceof JPanel) {
                    JPanel p = (JPanel) c;
                    for (Component cc : p.getComponents()) {
                        if (cc instanceof JLabel) {
                            JLabel l = (JLabel) cc;
                            l.setForeground(activeNav.equals(l.getText()) ? Color.WHITE
                                    : StyleConfig.TEXT_SECONDARY);
                        }
                    }
                    p.repaint();
                }
            }
        }
    }

    private void showDropdown(JLabel icon) {
        Point p = icon.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, this);
        int dropdownWidth = dropdown.getPreferredSize().width;
        if (dropdownWidth == 0)
            dropdownWidth = 290;
        dropdown.show(this, p.x + icon.getWidth() - dropdownWidth, p.y + icon.getHeight());
    }

    public void refreshUser() {
        if (SessionManager.getCurrentUser() != null) {
            this.username = SessionManager.getCurrentUser().getUsername();
            com.playtrack.model.Profile p = new com.playtrack.service.ProfileService()
                    .getProfile(SessionManager.getCurrentUser().getId());
            if (p != null) {
                this.avatarPath = p.getAvatarPath();
                if (this.avatarPath != null && !this.avatarPath.isEmpty()) {
                    try {
                        java.awt.image.BufferedImage rawImg = javax.imageio.ImageIO
                                .read(new java.io.File(this.avatarPath));
                        if (rawImg != null) {
                            this.avatarImage = rawImg.getScaledInstance(128, 128, java.awt.Image.SCALE_SMOOTH);
                        } else {
                            this.avatarImage = null;
                        }
                    } catch (Exception e) {
                        this.avatarImage = null;
                    }
                } else {
                    this.avatarImage = null;
                }
            }
            if (usernameLabel != null) {
                usernameLabel.setText(this.username);
            }
            if (profileIcon != null) {
                profileIcon.repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setPaint(new GradientPaint(0, 0, new Color(StyleConfig.PALETTE_INDIGO.getRed(), StyleConfig.PALETTE_INDIGO.getGreen(),
                StyleConfig.PALETTE_INDIGO.getBlue(), 232), 0, h, new Color(StyleConfig.PALETTE_NAVY.getRed(),
                StyleConfig.PALETTE_NAVY.getGreen(), StyleConfig.PALETTE_NAVY.getBlue(), 238)));
        g2.fillRect(0, 0, w, h);

        g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 28), 0, 18, new Color(255, 255, 255, 0)));
        g2.fillRect(0, 0, w, 18);

        g2.setColor(new Color(255, 255, 255, 36));
        g2.drawLine(0, h - 1, w, h - 1);

        g2.dispose();
    }
}
