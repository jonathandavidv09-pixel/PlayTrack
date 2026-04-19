package com.playtrack.ui.components;

import com.playtrack.util.SessionManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
// Navbar component.
public class Navbar extends JPanel {
    private static final long serialVersionUID = 1L;
    // Fixed navbar sizing so the controls stay vertically centered.
    private static final int NAV_HEIGHT = 64;
    private static final int NAV_RADIUS = 16;
    private static final int AVATAR_SIZE = 42;
    private static final int AVATAR_HIT_SIZE = 54;

    // Page navigation callbacks supplied by MainFrame.
    private Runnable onHome, onLibrary, onSummary, onProfile, onLogout;
    private ProfileDropdown dropdown;
    private String activeNav = "Home";
    private JPanel navLinksPanel;

    // Current user display state for the avatar menu trigger.
    private JLabel profileIcon;
    private String username = "User";
    private String avatarPath = null;
    private java.awt.Image avatarImage = null;
    private boolean profileHovered = false;

    // Builds the top navigation bar and wires all navigation/menu actions.
    public Navbar(Runnable onHome, Runnable onLibrary, Runnable onSummary, Runnable onProfile, Runnable onLogout) {
        this.onHome = onHome;
        this.onLibrary = onLibrary;
        this.onSummary = onSummary;
        this.onProfile = onProfile;
        this.onLogout = onLogout;

        // Base navbar sizing and horizontal page padding.
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(1400, NAV_HEIGHT));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        // Left cluster keeps the logo vertically centered inside the rounded bar.
        JPanel leftCluster = new JPanel(new GridBagLayout());
        leftCluster.setOpaque(false);
        leftCluster.add(createLogoLabel());
        add(leftCluster, BorderLayout.WEST);

        // Right-side action strip (navigation links + avatar) to keep controls together.
        JPanel rightCluster = new JPanel(new GridBagLayout());
        rightCluster.setOpaque(false);

        navLinksPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        navLinksPanel.setOpaque(false);
        // Main page links; each callback also updates the active nav style.
        navLinksPanel.add(createNavLink("Home", () -> {
            setActiveNav("Home");
            onHome.run();
        }));
        navLinksPanel.add(createNavLink("Library", () -> {
            setActiveNav("Library");
            onLibrary.run();
        }));
        navLinksPanel.add(createNavLink("Summary", () -> {
            setActiveNav("Summary");
            onSummary.run();
        }));

        // Center the nav link row next to the avatar with a consistent gap.
        GridBagConstraints navGbc = new GridBagConstraints();
        navGbc.gridx = 0;
        navGbc.gridy = 0;
        navGbc.insets = new Insets(0, 0, 0, 12);
        navGbc.anchor = GridBagConstraints.CENTER;
        rightCluster.add(navLinksPanel, navGbc);

        // User profile section on the right side of the navbar.
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        profilePanel.setOpaque(false);
        // Profile icon label that displays the user's avatar or a default placeholder.
        profileIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Custom avatar painting keeps the ring consistent with the profile header.
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int size = AVATAR_SIZE;
                int xPadding = (getWidth() - size) / 2;
                int yPadding = (getHeight() - size) / 2;

                // Soft hover halo behind the avatar trigger.
                if (profileHovered) {
                    g2.setColor(StyleConfig.withAlpha(StyleConfig.PRIMARY_COLOR, 22));
                    g2.fillOval(xPadding - 4, yPadding - 4, size + 8, size + 8);
                }

                g2.setColor(StyleConfig.SURFACE_ELEVATED);
                g2.fillOval(xPadding, yPadding, size, size);

                if (avatarImage != null) {
                    // Crop the selected image into a circle while preserving aspect ratio.
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

                    // Ring color matches the rest of the app's subtle line color.
                    g2.setColor(profileHovered ? StyleConfig.withAlpha(StyleConfig.PRIMARY_COLOR, 150)
                            : StyleConfig.SURFACE_STROKE);
                    g2.setStroke(new BasicStroke(profileHovered ? 1.8f : 1.2f));
                    g2.draw(new java.awt.geom.Ellipse2D.Float(xPadding + 0.7f, yPadding + 0.7f,
                            size - 1.4f, size - 1.4f));
                } else {
                    // Fallback avatar when no image has been selected.
                    g2.setPaint(new GradientPaint(xPadding, yPadding, StyleConfig.PRIMARY_COLOR,
                            xPadding + size, yPadding + size, StyleConfig.SECONDARY_COLOR));
                    g2.fillOval(xPadding, yPadding, size, size);
                    g2.setColor(profileHovered ? StyleConfig.withAlpha(Color.WHITE, 110)
                            : StyleConfig.SURFACE_STROKE);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new java.awt.geom.Ellipse2D.Float(xPadding + 0.7f, yPadding + 0.7f,
                            size - 1.4f, size - 1.4f));
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 21));
                    FontMetrics fm = g2.getFontMetrics();
                    String initial = username.length() > 0 ? username.substring(0, 1).toUpperCase() : "U";
                    g2.drawString(initial, xPadding + (size - fm.stringWidth(initial)) / 2,
                            yPadding + (size + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
        };
        profileIcon.setPreferredSize(new Dimension(AVATAR_HIT_SIZE, AVATAR_HIT_SIZE));
        profileIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profilePanel.add(profileIcon);

        // Avatar wrapper is centered separately so it lines up with the nav links.
        GridBagConstraints profileGbc = new GridBagConstraints();
        profileGbc.gridx = 1;
        profileGbc.gridy = 0;
        profileGbc.anchor = GridBagConstraints.CENTER;
        rightCluster.add(profilePanel, profileGbc);

        refreshUser();

        add(rightCluster, BorderLayout.EAST);
        // Wrap the onProfile action to also set the active navigation state to "Profile" when the profile dropdown option is selected.
        Runnable wrappedOnProfile = () -> {
            setActiveNav("Profile");
            onProfile.run();
        };
        // Dropdown opened from the avatar icon (Profile / Settings / Logout).
        dropdown = new ProfileDropdown(wrappedOnProfile, onLogout);

        profileIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Button action: open profile dropdown from the avatar button.
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

    // Loads the app logo from packaged resources first, then from the local src folder.
    private JLabel createLogoLabel() {
        JLabel logoLabel = new JLabel();
        logoLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        // Logo loading.
        String[] candidates = {"resources/logo.png" };
        BufferedImage logoImage = null;
        for (String path : candidates) {
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
                if (stream != null) {
                    logoImage = ImageIO.read(stream);
                    if (logoImage != null) {
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (logoImage == null) {
            // File fallback keeps the logo visible when running directly from the project folder.
            for (String path : new String[] { "src/resources/LogoDarkMode.png", "src/resources/logo.png" }) {
                try {
                    File file = new File(path);
                    if (file.exists()) {
                        logoImage = ImageIO.read(file);
                        if (logoImage != null) {
                            break;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (logoImage != null) {
            // Preserve the logo aspect ratio at the navbar height.
            int targetHeight = 46;
            int targetWidth = (int) Math.round((double) logoImage.getWidth() / logoImage.getHeight() * targetHeight);
            Image scaled = logoImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaled));
        } else {
            // Text fallback for missing logo assets.
            logoLabel.setText("PlayTrack");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            logoLabel.setForeground(StyleConfig.TEXT_COLOR);
        }
        return logoLabel;
    }

    // Start: navigation button function.
    private JPanel createNavLink(String text, Runnable action) {
        final boolean[] hovered = { false };
        JPanel linkPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Paint active and hover states directly on the link panel.
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean active = activeNav.equals(text);
                int pillX = 5;
                int pillY = 6;
                int pillW = getWidth() - 10;
                int pillH = getHeight() - 12;
                int arc = 12;
                if (active) {
                    g2.setColor(StyleConfig.withAlpha(StyleConfig.PRIMARY_COLOR, 54));
                    g2.fill(new RoundRectangle2D.Float(pillX, pillY, pillW, pillH, arc, arc));
                    g2.setColor(StyleConfig.withAlpha(StyleConfig.PRIMARY_LIGHT, 112));
                    g2.draw(new RoundRectangle2D.Float(pillX + 0.5f, pillY + 0.5f, pillW - 1, pillH - 1, arc, arc));
                } else if (hovered[0]) {
                    g2.setColor(StyleConfig.withAlpha(StyleConfig.SURFACE_SOFT, 135));
                    g2.fill(new RoundRectangle2D.Float(pillX, pillY, pillW, pillH, arc, arc));
                    g2.setColor(StyleConfig.SURFACE_STROKE);
                    g2.draw(new RoundRectangle2D.Float(pillX + 0.5f, pillY + 0.5f, pillW - 1, pillH - 1, arc, arc));
                }
                g2.dispose();
            }
        };
        linkPanel.setOpaque(false);
        linkPanel.setLayout(new GridBagLayout());
        linkPanel.setPreferredSize(new Dimension(104, 46));
        linkPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel link = new JLabel(text, SwingConstants.CENTER);
        link.setFont(new Font("Segoe UI", Font.BOLD, 14));
        link.setForeground(activeNav.equals(text) ? StyleConfig.TEXT_COLOR : StyleConfig.TEXT_SECONDARY);
        linkPanel.add(link);

        linkPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Button action: switch to the selected main page.
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered[0] = true;
                link.setForeground(StyleConfig.TEXT_COLOR);
                linkPanel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered[0] = false;
                link.setForeground(activeNav.equals(text) ? StyleConfig.TEXT_COLOR : StyleConfig.TEXT_SECONDARY);
                linkPanel.repaint();
            }
        });
        return linkPanel;
    }
    // End: navigation button function.
    
    // Updates which nav item is highlighted after page changes.
    public void setActiveNav(String nav) {
        this.activeNav = nav;
        if (navLinksPanel != null) {
            for (Component c : navLinksPanel.getComponents()) {
                if (c instanceof JPanel) {
                    JPanel p = (JPanel) c;
                    for (Component cc : p.getComponents()) {
                        if (cc instanceof JLabel) {
                            JLabel l = (JLabel) cc;
                            l.setForeground(activeNav.equals(l.getText()) ? StyleConfig.TEXT_COLOR
                                    : StyleConfig.TEXT_SECONDARY);
                        }
                    }
                    p.repaint();
                }
            }
        }
    }

    // Opens the profile dropdown aligned to the right edge of the avatar trigger.
    private void showDropdown(JLabel icon) {
        // Position and show the profile dropdown under the avatar trigger.
        Point p = icon.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, this);
        int dropdownWidth = dropdown.getPreferredSize().width;
        if (dropdownWidth == 0)
            dropdownWidth = 290;
        dropdown.show(this, p.x + icon.getWidth() - dropdownWidth, p.y + icon.getHeight());
    }

    // Reloads the current user's name and avatar after profile/settings changes.
    public void refreshUser() {
        if (SessionManager.getCurrentUser() != null) {
            this.username = SessionManager.getCurrentUser().getUsername();
            com.playtrack.model.Profile p = new com.playtrack.service.ProfileService()
                    .getProfile(SessionManager.getCurrentUser().getId());
            if (p != null) {
                this.avatarPath = p.getAvatarPath();
                if (this.avatarPath != null && !this.avatarPath.isEmpty()) {
                    try {
                        // Cache a scaled avatar for faster repainting.
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
            if (profileIcon != null) {
                profileIcon.repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Paint the page background image first, then the rounded navbar shell.
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 10;
        int y = 4;
        int w = getWidth() - 20;
        int h = getHeight() - 8;
        RoundRectangle2D.Float navbarShape = new RoundRectangle2D.Float(x, y, w, h, NAV_RADIUS, NAV_RADIUS);

        UIUtils.paintFadedAuthBackground(g2, getWidth(), getHeight());

        // Solid translucent shell keeps the image visible around the rounded corners.
        g2.setColor(StyleConfig.withAlpha(StyleConfig.SURFACE_ELEVATED, 224));
        g2.fill(navbarShape);

        // Thin top highlight gives the bar definition without a second color band.
        g2.setColor(StyleConfig.withAlpha(Color.WHITE, 24));
        g2.fillRoundRect(x + 12, y + 1, w - 24, 1, 1, 1);

        // Outer stroke matches the app's subtle line style.
        g2.setColor(StyleConfig.withAlpha(StyleConfig.TEXT_COLOR, 42));
        g2.setStroke(new BasicStroke(1.1f));
        g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, NAV_RADIUS, NAV_RADIUS));
        g2.dispose();
    }
}
