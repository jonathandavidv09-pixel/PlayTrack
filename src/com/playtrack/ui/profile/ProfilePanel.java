package com.playtrack.ui.profile;

import com.playtrack.dao.MediaDAO;
import com.playtrack.dao.ReviewDAO;
import com.playtrack.dao.WatchlistDAO;
import com.playtrack.model.MediaItem;
import com.playtrack.model.Profile;
import com.playtrack.model.User;
import com.playtrack.model.WatchlistItem;
import com.playtrack.service.AuthService;
import com.playtrack.service.ProfileService;
import com.playtrack.ui.components.MediaCard;
import com.playtrack.ui.components.RoundedButton;
import com.playtrack.ui.components.StyleConfig;
import com.playtrack.ui.components.UIUtils;
import com.playtrack.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ProfilePanel extends JPanel {
    private ProfileService profileService = new ProfileService();
    private Profile profile;
    private JLabel usernameLabel;
    private JLabel bioLabel;

    public ProfilePanel() {
        setLayout(new BorderLayout());
        setBackground(StyleConfig.BACKGROUND_COLOR);

        refreshProfile();
    }

    public void refreshProfile() {
        removeAll();
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null)
            return;
        int userId = currentUser.getId();
        profile = profileService.getProfile(userId);

        java.awt.Image cachedAvatar = null;
        if (profile.getAvatarPath() != null && !profile.getAvatarPath().isEmpty()) {
            try {
                java.awt.image.BufferedImage rawImg = javax.imageio.ImageIO
                        .read(new java.io.File(profile.getAvatarPath()));
                if (rawImg != null)
                    cachedAvatar = rawImg.getScaledInstance(256, 256, java.awt.Image.SCALE_SMOOTH);
            } catch (Exception e) {
            }
        }
        final java.awt.Image finalCachedAvatar = cachedAvatar;

        JPanel header = new JPanel();
        header.setLayout(null);
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(1400, 180));

        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setColor(StyleConfig.BACKGROUND_COLOR);
                g2.fill(new Ellipse2D.Float(0, 0, 120, 120));

                g2.setColor(StyleConfig.TEXT_COLOR);
                g2.fill(new Ellipse2D.Float(4, 4, 112, 112));

                if (finalCachedAvatar != null) {
                    g2.setClip(new Ellipse2D.Float(4, 4, 112, 112));
                    int imgW = finalCachedAvatar.getWidth(null);
                    int imgH = finalCachedAvatar.getHeight(null);
                    double scale = Math.max((double) 112 / imgW, (double) 112 / imgH);
                    int dw = (int) (imgW * scale);
                    int dh = (int) (imgH * scale);
                    g2.drawImage(finalCachedAvatar, 4 + (112 - dw) / 2, 4 + (112 - dh) / 2, dw, dh, null);
                    g2.setClip(null);
                    
                    // Draw anti-aliased mask border to cover jagged clipping pixels
                    g2.setColor(StyleConfig.BACKGROUND_COLOR);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new Ellipse2D.Float(4, 4, 112, 112));
                } else {
                    String initial = profile.getUsername() != null && !profile.getUsername().isEmpty()
                            ? profile.getUsername().substring(0, 1).toUpperCase()
                            : "U";
                    g2.setColor(StyleConfig.BACKGROUND_COLOR);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 50));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(initial, (120 - fm.stringWidth(initial)) / 2,
                            (120 + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
        };
        avatar.setBounds(40, 30, 120, 120);
        header.add(avatar);

        usernameLabel = new JLabel(profile.getUsername());
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        usernameLabel.setForeground(StyleConfig.TEXT_COLOR);
        usernameLabel.setBounds(190, 45, usernameLabel.getPreferredSize().width + 20, 30);
        header.add(usernameLabel);

        JLabel editBtn = new JLabel("Edit your profile", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getForeground().equals(StyleConfig.PRIMARY_COLOR)) {
                    g2.setColor(new Color(211, 64, 69, 20));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    g2.setColor(StyleConfig.PRIMARY_COLOR);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 15, 15));
                } else {
                    g2.setColor(new Color(255, 255, 255, 10));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 15, 15));
                }
                super.paintComponent(g);
                g2.dispose();
            }
        };
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editBtn.setForeground(StyleConfig.TEXT_SECONDARY);
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        int editBtnWidth = 115;
        int editBtnHeight = 28;
        editBtn.setBounds(190 + usernameLabel.getPreferredSize().width + 20, 46, editBtnWidth, editBtnHeight);
        
        editBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showEditDialog();
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                editBtn.setForeground(StyleConfig.PRIMARY_COLOR);
                editBtn.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                editBtn.setForeground(StyleConfig.TEXT_SECONDARY);
                editBtn.repaint();
            }
        });
        header.add(editBtn);

        String joinedText = "Joined "
                + (profile.getJoinedDate() != null ? profile.getJoinedDate().toString().substring(0, 10) : "Unknown");
        JLabel joinedLabel = new JLabel(joinedText);
        joinedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        joinedLabel.setForeground(StyleConfig.TEXT_SECONDARY);
        joinedLabel.setBounds(190, 80, 400, 20);
        header.add(joinedLabel);

        String bioText = profile.getBio() != null ? profile.getBio() : "No bio yet";
        bioLabel = new JLabel("<html><p style='width: 600px;'>" + bioText + "</p></html>");
        bioLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        bioLabel.setForeground(StyleConfig.TEXT_LIGHT);
        bioLabel.setBounds(190, 105, 600, 50);
        header.add(bioLabel);

        header.setMaximumSize(new Dimension(1400, 180));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        ReviewDAO reviewDAO = new ReviewDAO();
        MediaDAO mediaDAO = new MediaDAO();
        WatchlistDAO watchlistDAO = new WatchlistDAO();

        List<MediaItem> watchlists = fetchMediaItems(reviewDAO.getWatchlists(userId, 50), mediaDAO);

        List<WatchlistItem> textWatchlists = watchlistDAO.getWatchlistByUser(userId);
        for (WatchlistItem wi : textWatchlists) {
            watchlists.add(new MediaItem(-wi.getId(), userId, wi.getTitle(), wi.getCategory(), "Legacy", null, null,
                    wi.getAddedDate()));
        }

        List<MediaItem> favorites = fetchMediaItems(reviewDAO.getFavorites(userId, 50), mediaDAO);
        List<MediaItem> recents = fetchMediaItems(reviewDAO.getRecentReviews(userId, 50), mediaDAO);

        class ScrollablePanel extends JPanel implements Scrollable {
            public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
            public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 20; }
            public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 60; }
            public boolean getScrollableTracksViewportWidth() { return true; }
            public boolean getScrollableTracksViewportHeight() { return false; }
        }

        JPanel content = new ScrollablePanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        // Remove the 50px right margin from the parent container so we can use it for the arrow
        content.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 0));

        content.add(header);
        content.add(Box.createVerticalStrut(30));
        content.add(createSection("Watchlists", false, watchlists, reviewDAO, userId));
        content.add(createSection("Favorites", false, favorites, reviewDAO, userId));
        content.add(createSection("Recent Activity", false, recents, reviewDAO, userId));
        content.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        // Make the vertical scrollbar completely invisible natively like LibraryPanel!
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private List<MediaItem> fetchMediaItems(List<com.playtrack.model.Review> reviews, MediaDAO mediaDAO) {
        List<MediaItem> items = new ArrayList<>();
        for (com.playtrack.model.Review r : reviews) {
            MediaItem mi = mediaDAO.getMediaById(r.getMediaId());
            if (mi != null)
                items.add(mi);
        }
        return items;
    }

    private JPanel createSection(String title, boolean hasAddBtn, List<MediaItem> items, ReviewDAO reviewDAO,
            int userId) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel topRow = new JPanel();
        topRow.setLayout(new BoxLayout(topRow, BoxLayout.Y_AXIS));
        topRow.setOpaque(false);
        // Apply the 50px right margin locally to topRow so the divider aligns with the 8th card
        topRow.setBorder(BorderFactory.createEmptyBorder(0, 5, 15, 55));
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(StyleConfig.TEXT_COLOR);
        titleRow.add(titleLbl, BorderLayout.WEST);

        if (hasAddBtn) {
            JPanel addBtn = new JPanel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    
                    int size = 26;
                    UIUtils.drawPlusIcon(g2, getWidth() / 2, getHeight() / 2, size, getForeground());
                    g2.dispose();
                }
            };
            addBtn.setForeground(StyleConfig.TEXT_SECONDARY);
            addBtn.setPreferredSize(new Dimension(30, 30));
            addBtn.setOpaque(false);
            addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            addBtn.setToolTipText("Add to Watchlists");

            addBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    addBtn.setForeground(StyleConfig.PRIMARY_COLOR);
                    addBtn.repaint();
                }
                public void mouseExited(java.awt.event.MouseEvent e) {
                    addBtn.setForeground(StyleConfig.TEXT_SECONDARY);
                    addBtn.repaint();
                }
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    com.playtrack.ui.review.ReviewFormDialog dialog = new com.playtrack.ui.review.ReviewFormDialog(
                        (Frame) SwingUtilities.getWindowAncestor(ProfilePanel.this), "Watchlist", ProfilePanel.this::refreshProfile);
                    dialog.setVisible(true);
                }
            });
            titleRow.add(addBtn, BorderLayout.EAST);
        }
        topRow.add(titleRow);

        topRow.add(Box.createVerticalStrut(8));

        // Gradient divider line
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.PRIMARY_COLOR, getWidth(), 0, new Color(0, 0, 0, 0)));
                g2.fillRect(0, 0, getWidth(), 2);
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        topRow.add(divider);

        wrapper.add(topRow, BorderLayout.NORTH);

        if (items.isEmpty()) {
            JLabel emptyLabel = new JLabel("no activity yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            emptyLabel.setForeground(StyleConfig.TEXT_SECONDARY);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
            wrapper.add(emptyLabel, BorderLayout.CENTER);
            return wrapper;
        }

        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setOpaque(false);
        rowPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        for (MediaItem mi : items) {
            rowPanel.add(new MediaCard(mi, true, false, this::refreshProfile));
            rowPanel.add(Box.createHorizontalStrut(20));
        }

        JScrollPane scrollPane = new JScrollPane(rowPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        // Restore horizontal scrollbar thickness to 8 so it is visible when activated
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scrollPane.setPreferredSize(new Dimension(1420, 320)); 
        scrollPane.setMaximumSize(new Dimension(1420, 320));

        // Forward vertical mouse wheel events to parent (main page) to prevent blocking
        scrollPane.addMouseWheelListener(e -> {
            Container parent = scrollPane.getParent();
            while (parent != null && !(parent instanceof JScrollPane)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                parent.dispatchEvent(SwingUtilities.convertMouseEvent(scrollPane, e, parent));
            }
        });

        // Exact match with LibraryPanel contentWrapper spacing
        JPanel contentWrapper = new JPanel(new BorderLayout(0, 0));
        contentWrapper.setOpaque(false);

        class ScrollArrow extends JLabel {
            private final boolean isRight;

            public ScrollArrow(boolean isRight) {
                super(" ");
                this.isRight = isRight;
                setForeground(Color.WHITE); // Make arrow highly visible
                setPreferredSize(new Dimension(45, 60));
                setMaximumSize(new Dimension(45, 60));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setToolTipText("See more");
                
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                        updateVisibility(false); // Hide arrow once scrollbar appears
                        scrollPane.revalidate();
                        scrollPane.repaint();
                        JScrollBar hb = scrollPane.getHorizontalScrollBar();
                        hb.setValue(hb.getValue() + 555);
                    }
                    public void mouseEntered(java.awt.event.MouseEvent e) { 
                        setForeground(StyleConfig.PRIMARY_COLOR); 
                    }
                    public void mouseExited(java.awt.event.MouseEvent e) { 
                        setForeground(Color.WHITE); 
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                // Use discrete native png resolution by utilizing the larger size-bound method
                int iconSize = 40;
                int x = (getWidth() - iconSize) / 2;
                int y = (getHeight() - iconSize) / 2;
                UIUtils.drawArrowIcon(g2, x, y, iconSize, getForeground(), isRight);
                g2.dispose();
            }
            
            public void updateVisibility(boolean shouldShow) {
                if (isVisible() != shouldShow) {
                    setVisible(shouldShow);
                    getParent().revalidate();
                    getParent().repaint();
                }
            }
        }
        
        ScrollArrow rightArrow = new ScrollArrow(true);
        
        JPanel arrowContainer = new JPanel();
        arrowContainer.setLayout(new BoxLayout(arrowContainer, BoxLayout.Y_AXIS));
        // Restore transparency so it does not render a default gray block
        arrowContainer.setOpaque(false);
        // Tightly align arrow the same as Library panel (thick right padding 80px)
        arrowContainer.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 80));
        arrowContainer.add(Box.createVerticalGlue());
        arrowContainer.add(rightArrow);
        arrowContainer.add(Box.createVerticalGlue());

        scrollPane.setPreferredSize(new Dimension(1420, 320)); 
        scrollPane.setMaximumSize(new Dimension(1420, 320));

        contentWrapper.add(scrollPane, BorderLayout.CENTER);
        contentWrapper.add(arrowContainer, BorderLayout.EAST);
        
        // Initial visibility configuration
        rightArrow.updateVisibility(items.size() > 7);

        // Setup scroll listener to toggle arrow visibility dynamically
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(e -> {
            if (scrollPane.getHorizontalScrollBarPolicy() == JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
                rightArrow.updateVisibility(false);
            } else {
                int val = e.getValue();
                int max = scrollPane.getHorizontalScrollBar().getMaximum() - scrollPane.getHorizontalScrollBar().getVisibleAmount();
                rightArrow.updateVisibility(val < max && items.size() > 7);
            }
        });

        // Tightly constrained container matching library widths perfectly
        contentWrapper.setPreferredSize(new Dimension(1550, 320)); 
        contentWrapper.setMaximumSize(new Dimension(1550, 320));
        
        wrapper.add(contentWrapper, BorderLayout.WEST);
        return wrapper;
    }

    // Removed redundant showAddWatchlistDialog as it now uses the unified ReviewFormDialog

    private void showEditDialog() {
        JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Profile", true);
        editDialog.setSize(560, 620);
        editDialog.setLocationRelativeTo(this);
        editDialog.setUndecorated(true);
        editDialog.setBackground(new Color(0, 0, 0, 0));

        // ── Glassmorphism container with rounded corners & soft glow ──
        JPanel container = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Outer soft glow
                for (int i = 4; i > 0; i--) {
                    g2.setColor(new Color(211, 64, 69, 8 * i));
                    g2.fill(new RoundRectangle2D.Float(4 - i, 4 - i,
                            getWidth() - 8 + 2 * i, getHeight() - 8 + 2 * i, 28 + i * 2, 28 + i * 2));
                }

                // Main body
                g2.setColor(new Color(34, 42, 58));
                g2.fill(new RoundRectangle2D.Float(4, 4, getWidth() - 8, getHeight() - 8, 28, 28));

                // Subtle inner border
                g2.setColor(new Color(255, 255, 255, 12));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(4.5f, 4.5f, getWidth() - 9, getHeight() - 9, 27, 27));
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // ── Gradient accent bar at top ──
        JPanel accentBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(211, 64, 69),
                        getWidth(), 0, new Color(180, 50, 55, 120)));
                // Top rounded rect clipped to only show top 4px stripe
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), 28, 24, 24));
                g2.fillRect(0, 0, getWidth(), 4);
                g2.dispose();
            }
        };
        accentBar.setOpaque(false);
        accentBar.setPreferredSize(new Dimension(0, 4));

        // ── Content wrapper with padding ──
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(32, 42, 32, 42));

        // ── Dialog title ──
        JLabel dialogTitle = new JLabel("Edit Profile");
        dialogTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        dialogTitle.setForeground(StyleConfig.TEXT_COLOR);
        dialogTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(dialogTitle);

        JLabel dialogSubtitle = new JLabel("Customize how others see you");
        dialogSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dialogSubtitle.setForeground(StyleConfig.TEXT_LIGHT);
        dialogSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(dialogSubtitle);
        content.add(Box.createVerticalStrut(24));

        // ── Avatar section (centered) ──
        final String[] tempAvatarPath = { profile.getAvatarPath() };
        final java.awt.Image[] tempAvatarImg = { null };
        if (tempAvatarPath[0] != null && !tempAvatarPath[0].isEmpty()) {
            try {
                java.awt.image.BufferedImage rawImg = javax.imageio.ImageIO.read(new java.io.File(tempAvatarPath[0]));
                if (rawImg != null)
                    tempAvatarImg[0] = rawImg.getScaledInstance(256, 256, java.awt.Image.SCALE_SMOOTH);
            } catch (Exception e) {
            }
        }

        final boolean[] avatarHovered = { false };
        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int sz = 110;
                int pad = (getWidth() - sz) / 2;

                // Outer ring glow on hover
                if (avatarHovered[0]) {
                    for (int i = 3; i > 0; i--) {
                        g2.setColor(new Color(211, 64, 69, 20 * i));
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.draw(new Ellipse2D.Float(pad - i, -i, sz + 2 * i, sz + 2 * i));
                    }
                }

                // Background circle
                g2.setColor(new Color(30, 36, 50));
                g2.fill(new Ellipse2D.Float(pad, 0, sz, sz));

                // Image or initial
                if (tempAvatarImg[0] != null) {
                    g2.setClip(new Ellipse2D.Float(pad + 3, 3, sz - 6, sz - 6));
                    int imgW = tempAvatarImg[0].getWidth(null);
                    int imgH = tempAvatarImg[0].getHeight(null);
                    double scale = Math.max((double) (sz - 6) / imgW, (double) (sz - 6) / imgH);
                    int dw = (int) (imgW * scale);
                    int dh = (int) (imgH * scale);
                    g2.drawImage(tempAvatarImg[0], pad + 3 + (sz - 6 - dw) / 2,
                            3 + (sz - 6 - dh) / 2, dw, dh, null);
                    g2.setClip(null);
                    
                    // Mask jagged clip edges
                    g2.setColor(new Color(30, 36, 50));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new Ellipse2D.Float(pad + 3, 3, sz - 6, sz - 6));
                } else {
                    String initial = profile.getUsername() != null && !profile.getUsername().isEmpty()
                            ? profile.getUsername().substring(0, 1).toUpperCase() : "U";
                    g2.setColor(StyleConfig.TEXT_SECONDARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 42));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(initial, pad + (sz - fm.stringWidth(initial)) / 2,
                            (sz + fm.getAscent() - fm.getDescent()) / 2);
                }

                // Subtle ring
                g2.setColor(new Color(255, 255, 255, avatarHovered[0] ? 40 : 18));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new Ellipse2D.Float(pad + 1, 1, sz - 2, sz - 2));

                // Camera badge (bottom-right)
                int bx = pad + sz - 30, by = sz - 30, bsz = 28;
                g2.setColor(avatarHovered[0] ? new Color(211, 64, 69) : new Color(60, 72, 92));
                g2.fill(new Ellipse2D.Float(bx, by, bsz, bsz));
                g2.setColor(new Color(255, 255, 255, avatarHovered[0] ? 220 : 160));
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Camera icon
                int cx = bx + bsz / 2, cy = by + bsz / 2;
                g2.drawRoundRect(cx - 6, cy - 4, 12, 9, 3, 3);
                g2.draw(new Ellipse2D.Float(cx - 3, cy - 2, 6, 6));
                g2.drawLine(cx - 3, cy - 4, cx - 1, cy - 7);
                g2.drawLine(cx - 1, cy - 7, cx + 3, cy - 7);
                g2.drawLine(cx + 3, cy - 7, cx + 5, cy - 4);

                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(140, 120));
        avatar.setMaximumSize(new Dimension(140, 120));
        avatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                avatarHovered[0] = true;
                avatar.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                avatarHovered[0] = false;
                avatar.repaint();
            }

            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(
                        new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "webp"));
                if (fc.showOpenDialog(editDialog) == JFileChooser.APPROVE_OPTION) {
                    tempAvatarPath[0] = fc.getSelectedFile().getAbsolutePath();
                    try {
                        java.awt.image.BufferedImage rawImg = javax.imageio.ImageIO
                                .read(new java.io.File(tempAvatarPath[0]));
                        if (rawImg != null)
                            tempAvatarImg[0] = rawImg.getScaledInstance(256, 256, java.awt.Image.SCALE_SMOOTH);
                    } catch (Exception ex) {
                        tempAvatarImg[0] = null;
                    }
                    avatar.repaint();
                }
            }
        });

        // Avatar hint label
        JLabel avatarHint = new JLabel("Click to change avatar");
        avatarHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        avatarHint.setForeground(StyleConfig.TEXT_LIGHT);
        avatarHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel avatarSection = new JPanel();
        avatarSection.setLayout(new BoxLayout(avatarSection, BoxLayout.Y_AXIS));
        avatarSection.setOpaque(false);
        avatarSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        avatarSection.add(avatar);
        avatarSection.add(Box.createVerticalStrut(6));
        avatarSection.add(avatarHint);

        content.add(avatarSection);
        content.add(Box.createVerticalStrut(22));

        // ── Form fields ──
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username label
        JLabel userFieldLabel = new JLabel("<html><span style='letter-spacing: 1.5px;'>USERNAME</span></html>");
        userFieldLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 10));
        userFieldLabel.setForeground(new Color(140, 150, 170));
        userFieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(userFieldLabel);
        form.add(Box.createVerticalStrut(6));

        // Username field with focus glow
        JTextField userField = new JTextField(profile.getUsername()) {
            private boolean focused = false;

            {
                addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent e) {
                        focused = true;
                        repaint();
                    }

                    public void focusLost(java.awt.event.FocusEvent e) {
                        focused = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2.setColor(focused ? new Color(36, 43, 60) : new Color(28, 35, 48));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));

                // Focus glow border
                if (focused) {
                    g2.setColor(new Color(211, 64, 69, 180));
                    g2.setStroke(new BasicStroke(2f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 13, 13));
                } else {
                    g2.setColor(new Color(255, 255, 255, 15));
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 13, 13));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        userField.setOpaque(false);
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        userField.setPreferredSize(new Dimension(0, 46));
        userField.setForeground(Color.WHITE);
        userField.setCaretColor(StyleConfig.PRIMARY_COLOR);
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        userField.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        userField.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(userField);
        form.add(Box.createVerticalStrut(18));

        // Bio label with character counter
        JLabel bioCountLabel = new JLabel("0/150");
        bioCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bioCountLabel.setForeground(StyleConfig.TEXT_LIGHT);

        JPanel bioLabelRow = new JPanel(new BorderLayout());
        bioLabelRow.setOpaque(false);
        bioLabelRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bioLabelRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));

        JLabel bioFieldLabel = new JLabel("<html><span style='letter-spacing: 1.5px;'>BIO</span></html>");
        bioFieldLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 10));
        bioFieldLabel.setForeground(new Color(140, 150, 170));
        bioLabelRow.add(bioFieldLabel, BorderLayout.WEST);
        bioLabelRow.add(bioCountLabel, BorderLayout.EAST);
        form.add(bioLabelRow);
        form.add(Box.createVerticalStrut(6));

        // Bio field with focus glow
        JTextArea bioField = new JTextArea(profile.getBio()) {
            private boolean focused = false;

            {
                addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent e) {
                        focused = true;
                        getParent().getParent().repaint(); // repaint scroll pane wrapper
                    }

                    public void focusLost(java.awt.event.FocusEvent e) {
                        focused = false;
                        getParent().getParent().repaint();
                    }
                });
            }

            public boolean isFocusHighlighted() {
                return focused;
            }
        };
        bioField.setRows(3);
        bioField.setOpaque(false);
        bioField.setForeground(Color.WHITE);
        bioField.setCaretColor(StyleConfig.PRIMARY_COLOR);
        bioField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        bioField.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        bioField.setLineWrap(true);
        bioField.setWrapStyleWord(true);

        // Update character count
        String bioText = profile.getBio() != null ? profile.getBio() : "";
        bioCountLabel.setText(bioText.length() + "/150");
        bioField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                int len = bioField.getText().length();
                bioCountLabel.setText(len + "/150");
                bioCountLabel.setForeground(len > 150 ? StyleConfig.ERROR_COLOR : StyleConfig.TEXT_LIGHT);
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        JScrollPane bioScroll = new JScrollPane(bioField) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean focused = bioField.hasFocus();
                g2.setColor(focused ? new Color(36, 43, 60) : new Color(28, 35, 48));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));

                if (focused) {
                    g2.setColor(new Color(211, 64, 69, 180));
                    g2.setStroke(new BasicStroke(2f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 13, 13));
                } else {
                    g2.setColor(new Color(255, 255, 255, 15));
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 13, 13));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bioScroll.setOpaque(false);
        bioScroll.getViewport().setOpaque(false);
        bioScroll.setBorder(null);
        bioScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        bioScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        bioScroll.setPreferredSize(new Dimension(0, 100));
        form.add(bioScroll);

        content.add(form);
        content.add(Box.createVerticalGlue());

        // ── Divider before buttons ──
        JPanel btnDivider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        btnDivider.setOpaque(false);
        btnDivider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        btnDivider.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(Box.createVerticalStrut(20));
        content.add(btnDivider);
        content.add(Box.createVerticalStrut(18));

        // ── Buttons (right-aligned) ──
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        RoundedButton cancelBtn = new RoundedButton("Cancel", new Color(58, 68, 86), 14);
        cancelBtn.setPreferredSize(new Dimension(110, 42));
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelBtn.addActionListener(e -> editDialog.dispose());
        buttons.add(cancelBtn);

        RoundedButton saveBtn = new RoundedButton("Save Changes", StyleConfig.PRIMARY_COLOR, 14);
        saveBtn.setGradient(StyleConfig.PRIMARY_DARK);
        saveBtn.setPreferredSize(new Dimension(150, 42));
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.addActionListener(e -> {
            String newUsername = userField.getText().trim();
            if (!newUsername.equals(profile.getUsername())) {
                AuthService authService = new AuthService();
                if (authService.isUsernameTaken(newUsername)) {
                    JOptionPane.showMessageDialog(editDialog, "the username is already taken", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            profile.setUsername(newUsername);
            profile.setBio(bioField.getText());
            profile.setAvatarPath(tempAvatarPath[0]);
            if (profileService.updateProfile(profile)) {
                User sessionUser = SessionManager.getCurrentUser();
                sessionUser.setUsername(profile.getUsername());
                AuthService authService = new AuthService();
                authService.updateSettings(sessionUser, "", "");

                Window window = SwingUtilities.getWindowAncestor(ProfilePanel.this);
                if (window instanceof com.playtrack.ui.main.MainFrame) {
                    ((com.playtrack.ui.main.MainFrame) window).refreshAll();
                } else {
                    refreshProfile();
                }
                editDialog.dispose();
            }
        });
        buttons.add(saveBtn);

        content.add(buttons);

        container.add(accentBar, BorderLayout.NORTH);
        container.add(content, BorderLayout.CENTER);

        editDialog.add(container);
        editDialog.setVisible(true);
    }
}