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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ProfilePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int PROFILE_CARD_WIDTH = 160;
    private static final int PROFILE_CARD_HEIGHT = 240;
    private static final int PROFILE_CARD_GAP = 20;
    private static final int PROFILE_CARD_ROW_LEFT_INSET = 24;
    private static final int PROFILE_SECTION_ARROW_WIDTH = 80;
    private static final int PROFILE_ARROW_ICON_SIZE = 28;
    private static final int PROFILE_COLLAPSED_ROWS = 2;
    private static final int PROFILE_COLLAPSED_COLUMNS = 7;
    private static final int RECENT_ACTIVITY_COLUMNS = PROFILE_COLLAPSED_COLUMNS;
    private static final int RECENT_ACTIVITY_ARROW_WIDTH = 128;
    private static final int RECENT_ACTIVITY_ARROW_GAP = 64;
    private static final int RECENT_ACTIVITY_SCROLLBAR_GAP = 28;
    private static final int RECENT_ACTIVITY_SCROLLBAR_HEIGHT = 10;
    private static final int RECENT_ACTIVITY_TRAILING_SPACE = 36;
    private ProfileService profileService = new ProfileService();
    private Profile profile;
    private JLabel usernameLabel;
    private JLabel bioLabel;
    private String favoritesFilterCategory;
    private String favoritesFilterGenre;
    private transient BufferedImage cachedBackground;
    private int cachedBackgroundW = -1;
    private int cachedBackgroundH = -1;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintCachedBackground(g2, getWidth(), getHeight());
        g2.dispose();
    }

    private void paintCachedBackground(Graphics2D g2, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        if (cachedBackground == null || cachedBackgroundW != w || cachedBackgroundH != h) {
            cachedBackground = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            cachedBackgroundW = w;
            cachedBackgroundH = h;

            Graphics2D bg = cachedBackground.createGraphics();
            bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            UIUtils.paintFadedAuthBackground(bg, w, h);

            int orbSize = 360;
            bg.setPaint(new RadialGradientPaint(
                    w - 120f, 100f, orbSize / 2f,
                    new float[] { 0f, 0.45f, 1f },
                    new Color[] {
                            StyleConfig.PANEL_GLOW_SECONDARY,
                            new Color(StyleConfig.PALETTE_PEACH.getRed(), StyleConfig.PALETTE_PEACH.getGreen(),
                                    StyleConfig.PALETTE_PEACH.getBlue(), 8),
                            new Color(0, 0, 0, 0)
                    }));
            bg.fillOval(w - 120 - orbSize / 2, 100 - orbSize / 2, orbSize, orbSize);
            bg.dispose();
        }
        g2.drawImage(cachedBackground, 0, 0, null);
    }

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

        final int avatarX = 40;
        final int avatarY = 20;
        final int avatarSize = 142;
        final int avatarInset = 4;
        final int avatarInnerSize = avatarSize - (avatarInset * 2);
        final int contentStartX = avatarX + avatarSize + 30;

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
                g2.setColor(StyleConfig.SURFACE_ELEVATED);
                g2.fill(new Ellipse2D.Float(0, 0, avatarSize, avatarSize));
                g2.setColor(StyleConfig.SURFACE_STROKE);
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(new Ellipse2D.Float(0.7f, 0.7f, avatarSize - 1.4f, avatarSize - 1.4f));

                g2.setColor(StyleConfig.TEXT_COLOR);
                g2.fill(new Ellipse2D.Float(avatarInset, avatarInset, avatarInnerSize, avatarInnerSize));

                if (finalCachedAvatar != null) {
                    g2.setClip(new Ellipse2D.Float(avatarInset, avatarInset, avatarInnerSize, avatarInnerSize));
                    int imgW = finalCachedAvatar.getWidth(null);
                    int imgH = finalCachedAvatar.getHeight(null);
                    double scale = Math.max((double) avatarInnerSize / imgW, (double) avatarInnerSize / imgH);
                    int dw = (int) (imgW * scale);
                    int dh = (int) (imgH * scale);
                    g2.drawImage(finalCachedAvatar, avatarInset + (avatarInnerSize - dw) / 2,
                            avatarInset + (avatarInnerSize - dh) / 2, dw, dh, null);
                    g2.setClip(null);
                } else {
                    String initial = profile.getUsername() != null && !profile.getUsername().isEmpty()
                            ? profile.getUsername().substring(0, 1).toUpperCase()
                            : "U";
                    g2.setColor(StyleConfig.BACKGROUND_COLOR);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 56));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(initial, (avatarSize - fm.stringWidth(initial)) / 2,
                            (avatarSize + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
        };
        avatar.setBounds(avatarX, avatarY, avatarSize, avatarSize);
        header.add(avatar);

        usernameLabel = new JLabel(profile.getUsername());
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        usernameLabel.setForeground(StyleConfig.TEXT_COLOR);
        usernameLabel.setBounds(contentStartX, 42, usernameLabel.getPreferredSize().width + 24, 34);
        header.add(usernameLabel);

        JLabel editBtn = new JLabel("Edit your profile", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getForeground().equals(StyleConfig.PRIMARY_COLOR)) {
                    g2.setColor(StyleConfig.withAlpha(StyleConfig.PRIMARY_COLOR, 24));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    g2.setColor(StyleConfig.PRIMARY_COLOR);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 15, 15));
                } else {
                    g2.setColor(StyleConfig.SURFACE_ELEVATED);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    g2.setColor(StyleConfig.SURFACE_STROKE);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 15, 15));
                }
                super.paintComponent(g);
                g2.dispose();
            }
        };
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        editBtn.setForeground(StyleConfig.TEXT_SECONDARY);
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        int editBtnWidth = 124;
        int editBtnHeight = 30;
        editBtn.setBounds(contentStartX + usernameLabel.getPreferredSize().width + 22, 44, editBtnWidth, editBtnHeight);

        editBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Button action: open the edit-profile dialog.
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
        joinedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        joinedLabel.setForeground(StyleConfig.TEXT_SECONDARY);
        joinedLabel.setBounds(contentStartX, 83, 420, 24);
        header.add(joinedLabel);

        String bioText = profile.getBio() != null ? profile.getBio() : "No bio yet";
        bioLabel = new JLabel("<html><p style='width: 600px;'>" + bioText + "</p></html>");
        bioLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        bioLabel.setForeground(StyleConfig.TEXT_SECONDARY);
        bioLabel.setBounds(contentStartX, 112, 640, 52);
        header.add(bioLabel);

        header.setMaximumSize(new Dimension(1400, 180));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        ReviewDAO reviewDAO = new ReviewDAO();
        MediaDAO mediaDAO = new MediaDAO();
        WatchlistDAO watchlistDAO = new WatchlistDAO();

        
        java.util.Map<Integer, MediaItem> mediaMap = new java.util.HashMap<>();
        for (MediaItem mi : mediaDAO.getMediaByUser(userId, "All")) {
            mediaMap.put(mi.getId(), mi);
        }

        List<MediaItem> watchlists = fetchMediaItems(reviewDAO.getWatchlists(userId, 50), mediaMap);

        List<WatchlistItem> textWatchlists = watchlistDAO.getWatchlistByUser(userId);
        for (WatchlistItem wi : textWatchlists) {
            watchlists.add(new MediaItem(-wi.getId(), userId, wi.getTitle(), wi.getCategory(), "Legacy", null, null,
                    wi.getAddedDate()));
        }

        List<MediaItem> favorites = fetchMediaItems(reviewDAO.getFavorites(userId, 50), mediaMap);
        List<MediaItem> recents = fetchMediaItems(reviewDAO.getRecentReviews(userId, 50), mediaMap);

        class ScrollablePanel extends JPanel implements Scrollable {
            private static final long serialVersionUID = 1L;
            @Override
            public Dimension getPreferredSize() {
                Dimension preferred = super.getPreferredSize();
                preferred.width = Math.max(preferred.width, getFixedProfileContentWidth());
                return preferred;
            }

            public Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }

            public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
                return 20;
            }

            public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
                return 60;
            }

            public boolean getScrollableTracksViewportWidth() {
                return false;
            }

            public boolean getScrollableTracksViewportHeight() {
                return false;
            }
        }

        JPanel content = new ScrollablePanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        
        
        content.setBorder(BorderFactory.createEmptyBorder(
                StyleConfig.PAGE_PAD_TOP, StyleConfig.PAGE_PAD_X, StyleConfig.PAGE_PAD_BOTTOM, StyleConfig.PAGE_PAD_X));

        content.add(header);
        content.add(Box.createVerticalStrut(30));
        content.add(createSection("Watchlists", true, watchlists, reviewDAO, userId));
        content.add(createSection("Favorites", false, favorites, reviewDAO, userId));
        content.add(createSection("Recent Activity", false, recents, reviewDAO, userId));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private List<MediaItem> fetchMediaItems(List<com.playtrack.model.Review> reviews,
            java.util.Map<Integer, MediaItem> mediaMap) {
        List<MediaItem> items = new ArrayList<>();
        for (com.playtrack.model.Review r : reviews) {
            MediaItem mi = mediaMap.get(r.getMediaId());
            if (mi != null)
                items.add(mi);
        }
        return items;
    }

    private JPanel createSection(String title, boolean hasAddBtn, List<MediaItem> items, ReviewDAO reviewDAO,
            int userId) {
        final boolean fixedRecentActivity = "Recent Activity".equals(title);
        final int maxVisibleCards = fixedRecentActivity
                ? getRecentActivityCollapsedCardLimit()
                : calculateCollapsedCardLimit();

        final List<MediaItem> sourceItems = new ArrayList<>(items);
        boolean favoritesFiltered = "Favorites".equals(title) && favoritesFilterCategory != null;
        List<MediaItem> visibleItems = "Favorites".equals(title) ? applyFavoritesFilter(sourceItems) : new ArrayList<>(sourceItems);
        final List<MediaItem> allItems = new ArrayList<>(visibleItems);
        boolean hasMore = allItems.size() > maxVisibleCards && !favoritesFiltered;
        if (visibleItems.size() > maxVisibleCards && !favoritesFiltered) {
            visibleItems = visibleItems.subList(0, maxVisibleCards);
        }
        items = visibleItems;

        JPanel wrapper = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getMaximumSize() {
                Dimension preferred = getPreferredSize();
                preferred.width = Integer.MAX_VALUE;
                return preferred;
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMinimumSize(new Dimension(Math.min(PROFILE_CARD_WIDTH, getFixedSectionWidth(fixedRecentActivity, hasMore)),
                0));

        JPanel topRow = new JPanel();
        topRow.setLayout(new BoxLayout(topRow, BoxLayout.Y_AXIS));
        topRow.setOpaque(false);
        
        
        topRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 24));
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(StyleConfig.TEXT_COLOR);
        titleRow.add(titleLbl);

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
                    // Button action: open the watchlist add dialog.
                    com.playtrack.ui.review.ReviewFormDialog dialog = new com.playtrack.ui.review.ReviewFormDialog(
                            (Frame) SwingUtilities.getWindowAncestor(ProfilePanel.this), "Watchlist",
                            ProfilePanel.this::refreshProfile);
                    dialog.setVisible(true);
                }
            });
            titleRow.add(addBtn);
        }

        if ("Favorites".equals(title)) {
            JPanel filterBtn = new JPanel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    UIUtils.drawMenuIcon(g2, getWidth() / 2, getHeight() / 2, 24, getForeground());
                    g2.dispose();
                }
            };
            filterBtn.setForeground(StyleConfig.TEXT_COLOR);
            filterBtn.setPreferredSize(new Dimension(30, 30));
            filterBtn.setOpaque(false);
            filterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            filterBtn.setToolTipText("Favorite Genres by Category");

            filterBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    filterBtn.setForeground(StyleConfig.SECONDARY_COLOR);
                    filterBtn.repaint();
                }

                public void mouseExited(java.awt.event.MouseEvent e) {
                    filterBtn.setForeground(StyleConfig.TEXT_COLOR);
                    filterBtn.repaint();
                }

                public void mouseClicked(java.awt.event.MouseEvent e) {
                    // Button action: open the favorites filter dropdown.
                    // Dropdown menu for filtering favorites by category/genre.
                    JPopupMenu popup = createFavoritesGenreMenu(sourceItems, (category, genre) -> {
                        favoritesFilterCategory = category;
                        favoritesFilterGenre = genre;
                        refreshProfile();
                    });
                    popup.show(filterBtn, 0, filterBtn.getHeight() + 6);
                }
            });
            titleRow.add(filterBtn);

            if (favoritesFilterCategory != null) {
                String filterText = favoritesFilterGenre == null
                        ? favoritesFilterCategory
                        : favoritesFilterCategory + " - " + favoritesFilterGenre;
                JLabel activeFilter = new JLabel(filterText);
                activeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                activeFilter.setForeground(StyleConfig.SECONDARY_COLOR);
                activeFilter.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));
                titleRow.add(activeFilter);
            }
        }

        
        JPanel titleRowWrapper = new JPanel(new BorderLayout());
        titleRowWrapper.setOpaque(false);
        titleRowWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRowWrapper.add(titleRow, BorderLayout.WEST);

        topRow.add(titleRowWrapper);

        topRow.add(Box.createVerticalStrut(8));

        
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.SECONDARY_COLOR, getWidth(), 0, new Color(0, 0, 0, 0)));
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

        int collapsedColumns = PROFILE_COLLAPSED_COLUMNS;
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setOpaque(false);
        rowPanel.setBorder(BorderFactory.createEmptyBorder(0, PROFILE_CARD_ROW_LEFT_INSET, 0, 0));

        JPanel cardGrid = createProfileCardGrid(items, collapsedColumns, 0, fixedRecentActivity);
        rowPanel.add(cardGrid, BorderLayout.WEST);

        wrapper.add(rowPanel, BorderLayout.CENTER);
        // the"Show more" control Button.
        if (hasMore) {
            JLabel arrowIcon = createProfileArrowControl(true, () -> false);
            arrowIcon.setForeground(StyleConfig.TEXT_SECONDARY);
            arrowIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
            arrowIcon.setToolTipText("Show more");

            JPanel arrowWrapper = createSectionArrowWrapper(arrowIcon, fixedRecentActivity);
            wrapper.add(arrowWrapper, BorderLayout.EAST);

            arrowIcon.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    arrowIcon.setForeground(StyleConfig.PRIMARY_COLOR);
                    arrowIcon.repaint();
                }

                public void mouseExited(java.awt.event.MouseEvent e) {
                    arrowIcon.setForeground(StyleConfig.TEXT_SECONDARY);
                    arrowIcon.repaint();
                }

                public void mouseClicked(java.awt.event.MouseEvent e) {
                    // Button action: expand the media row to show all items.
                    wrapper.remove(arrowWrapper);

                    int expandedColumns = Math.max(1,
                            (allItems.size() + PROFILE_COLLAPSED_ROWS - 1) / PROFILE_COLLAPSED_ROWS);
                    JPanel expandedRowPanel = createProfileCardGrid(
                            allItems, expandedColumns, RECENT_ACTIVITY_SCROLLBAR_GAP, fixedRecentActivity);
                    int expandedHeight = getRecentActivityContentHeight(RECENT_ACTIVITY_SCROLLBAR_GAP);
                    int expandedSectionHeight = expandedHeight + RECENT_ACTIVITY_SCROLLBAR_HEIGHT;
                    int expandedViewportWidth = getProfileCardViewportWidth(fixedRecentActivity);

                    JScrollPane scrollPane = createRecentActivityScrollPane(
                            expandedRowPanel,
                            expandedViewportWidth,
                            expandedSectionHeight,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

                    scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, RECENT_ACTIVITY_SCROLLBAR_HEIGHT));
                    scrollPane.getHorizontalScrollBar().setUnitIncrement(24);

                    JPanel scrollArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                    scrollArea.setOpaque(false);
                    scrollArea.setBorder(BorderFactory.createEmptyBorder(0, PROFILE_CARD_ROW_LEFT_INSET, 0, 0));
                    scrollArea.setPreferredSize(new Dimension(
                            expandedViewportWidth + PROFILE_CARD_ROW_LEFT_INSET, expandedSectionHeight));
                    scrollArea.setMinimumSize(new Dimension(
                            PROFILE_CARD_WIDTH + PROFILE_CARD_ROW_LEFT_INSET, expandedSectionHeight));
                    scrollArea.add(scrollPane);

                    
                    
                    java.awt.event.MouseAdapter dragScroll = new java.awt.event.MouseAdapter() {
                        private Point origin;

                        @Override
                        public void mousePressed(java.awt.event.MouseEvent e) {
                            origin = e.getLocationOnScreen();
                        }

                        @Override
                        public void mouseDragged(java.awt.event.MouseEvent e) {
                            if (origin != null) {
                                JViewport viewport = scrollPane.getViewport();
                                Point viewPos = viewport.getViewPosition();
                                int dx = origin.x - e.getLocationOnScreen().x;
                                int newX = Math.max(0, viewPos.x + dx);
                                int maxX = Math.max(0, viewport.getViewSize().width - viewport.getWidth());
                                newX = Math.min(newX, maxX);
                                viewport.setViewPosition(new Point(newX, viewPos.y));
                                origin = e.getLocationOnScreen();
                            }
                        }

                        @Override
                        public void mouseReleased(java.awt.event.MouseEvent e) {
                            origin = null;
                        }
                    };

                    expandedRowPanel.addMouseListener(dragScroll);
                    expandedRowPanel.addMouseMotionListener(dragScroll);

                    
                    for (Component c : expandedRowPanel.getComponents()) {
                        c.addMouseListener(dragScroll);
                        c.addMouseMotionListener(dragScroll);
                    }

                    // the "Show less" control Button
                    final boolean[] showLessHovered = { false };
                    JLabel showLessControl = createProfileArrowControl(false, () -> showLessHovered[0]);
                    showLessControl.setForeground(StyleConfig.TEXT_SECONDARY);
                    showLessControl.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    showLessControl.setToolTipText("Show less");

                    JPanel navArrows = createSectionArrowWrapper(showLessControl, fixedRecentActivity);

                    java.awt.event.MouseAdapter showLessMouse = new java.awt.event.MouseAdapter() {
                        public void mouseEntered(java.awt.event.MouseEvent e) {
                            showLessHovered[0] = true;
                            showLessControl.repaint();
                        }
                        public void mouseExited(java.awt.event.MouseEvent e) {
                            showLessHovered[0] = false;
                            showLessControl.repaint();
                        }
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            // Button action: collapse the media row back to the default state.
                            wrapper.remove(scrollArea);
                            wrapper.remove(navArrows);
                            wrapper.add(rowPanel, BorderLayout.CENTER);
                            wrapper.add(arrowWrapper, BorderLayout.EAST);
                            arrowIcon.setForeground(StyleConfig.TEXT_SECONDARY);
                            wrapper.revalidate();
                            wrapper.repaint();
                        }
                    };
                    showLessControl.addMouseListener(showLessMouse);

                    wrapper.remove(rowPanel);
                    wrapper.add(scrollArea, BorderLayout.CENTER);
                    wrapper.add(navArrows, BorderLayout.EAST);

                    wrapper.revalidate();
                    wrapper.repaint();
                }
            });
        }

        return wrapper;
    }

    private JLabel createProfileArrowControl(boolean right, java.util.function.BooleanSupplier hovered) {
        Dimension arrowSize = new Dimension(PROFILE_ARROW_ICON_SIZE, PROFILE_ARROW_ICON_SIZE);
        JLabel arrowControl = new JLabel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color = hovered.getAsBoolean() ? StyleConfig.PRIMARY_COLOR : getForeground();
                UIUtils.drawArrowIcon(g2, 0, 0, PROFILE_ARROW_ICON_SIZE, color, right);
                g2.dispose();
            }

            @Override
            public boolean contains(int x, int y) {
                return createArrowHitShape(right, getWidth(), getHeight()).contains(x, y);
            }
        };
        arrowControl.setOpaque(false);
        arrowControl.setPreferredSize(arrowSize);
        arrowControl.setMinimumSize(arrowSize);
        arrowControl.setMaximumSize(arrowSize);
        return arrowControl;
    }

    private Shape createArrowHitShape(boolean right, int width, int height) {
        int w = Math.max(PROFILE_ARROW_ICON_SIZE, width);
        int h = Math.max(PROFILE_ARROW_ICON_SIZE, height);
        int midY = h / 2;
        int pad = 2;
        int notch = Math.max(6, w / 4);

        Polygon arrow = new Polygon();
        if (right) {
            arrow.addPoint(pad, pad);
            arrow.addPoint(w - pad, midY);
            arrow.addPoint(pad, h - pad);
            arrow.addPoint(pad + notch, midY);
        } else {
            arrow.addPoint(w - pad, pad);
            arrow.addPoint(pad, midY);
            arrow.addPoint(w - pad, h - pad);
            arrow.addPoint(w - pad - notch, midY);
        }
        return arrow;
    }

    private JPanel createSectionArrowWrapper(JLabel arrowControl, boolean fixedRecentActivity) {
        int sectionArrowWidth = fixedRecentActivity ? getRecentActivityArrowWidth() : 80;
        int sectionArrowGap = fixedRecentActivity ? getRecentActivityArrowGap() : 0;
        JPanel arrowWrapper = new JPanel(null) {
            private static final long serialVersionUID = 1L;

            @Override
            public void doLayout() {
                Insets insets = getInsets();
                Dimension iconSize = arrowControl.getPreferredSize();
                int x = Math.max(insets.left, getWidth() - insets.right - iconSize.width);
                int y = Math.max(0,
                        (getRecentActivityContentHeight(RECENT_ACTIVITY_SCROLLBAR_GAP) - iconSize.height) / 2);
                arrowControl.setBounds(x, y, iconSize.width, iconSize.height);
            }
        };
        arrowWrapper.setOpaque(false);
        int sectionHeight = getRecentActivityContentHeight(0);
        arrowWrapper.setPreferredSize(new Dimension(sectionArrowWidth, sectionHeight));
        arrowWrapper.setMinimumSize(new Dimension(sectionArrowWidth, sectionHeight));
        arrowWrapper.setMaximumSize(new Dimension(sectionArrowWidth, sectionHeight));
        arrowWrapper.setBorder(BorderFactory.createEmptyBorder(0, sectionArrowGap, 0, 0));
        arrowWrapper.add(arrowControl);
        return arrowWrapper;
    }

    private int calculateCollapsedCardLimit() {
        return getStandardSectionVisibleColumns() * PROFILE_COLLAPSED_ROWS;
    }

    private int getFixedProfileContentWidth() {
        return getFixedSectionWidth(true, true) + (StyleConfig.PAGE_PAD_X * 2);
    }

    private int getFixedSectionWidth(boolean fixedRecentActivity, boolean hasMore) {
        int arrowWidth = hasMore
                ? (fixedRecentActivity ? RECENT_ACTIVITY_ARROW_WIDTH : PROFILE_SECTION_ARROW_WIDTH)
                : 0;
        return PROFILE_CARD_ROW_LEFT_INSET
                + getRecentActivityWidthForColumns(PROFILE_COLLAPSED_COLUMNS)
                + RECENT_ACTIVITY_TRAILING_SPACE
                + arrowWidth;
    }

    // Shared card grid keeps every profile section on the same invisible columns.
    private JPanel createProfileCardGrid(List<MediaItem> mediaItems, int columns, int bottomGap,
            boolean forceReviewControls) {
        JPanel strip = new JPanel(null);
        strip.setOpaque(false);
        strip.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        int safeColumns = Math.max(1, columns);
        int rows = Math.max(1, (mediaItems.size() + safeColumns - 1) / safeColumns);
        int width = getRecentActivityWidthForColumns(safeColumns);
        int height = (rows * PROFILE_CARD_HEIGHT) + ((rows - 1) * PROFILE_CARD_GAP) + bottomGap;
        strip.setPreferredSize(new Dimension(width, height));
        strip.setMinimumSize(new Dimension(width, height));
        strip.setMaximumSize(new Dimension(width, height));

        for (int i = 0; i < mediaItems.size(); i++) {
            MediaCard card = new MediaCard(
                    mediaItems.get(i), true, false, ProfilePanel.this::refreshProfile, forceReviewControls);

            int column = i % safeColumns;
            int row = i / safeColumns;
            int x = column * (PROFILE_CARD_WIDTH + PROFILE_CARD_GAP);
            int y = row * (PROFILE_CARD_HEIGHT + PROFILE_CARD_GAP);
            card.setBounds(x, y, PROFILE_CARD_WIDTH, PROFILE_CARD_HEIGHT);
            strip.add(card);
        }

        return strip;
    }

    private int getStandardSectionVisibleColumns() {
        return PROFILE_COLLAPSED_COLUMNS;
    }

    private JScrollPane createRecentActivityScrollPane(JPanel cardStrip, int viewportWidth, int viewportHeight,
            int horizontalPolicy) {
        JScrollPane scrollPane = new JScrollPane();
        JViewport viewport = new JViewport() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Point p = SwingUtilities.convertPoint(this, 0, 0, ProfilePanel.this);
                g2.translate(-p.x, -p.y);
                paintCachedBackground(g2, ProfilePanel.this.getWidth(), ProfilePanel.this.getHeight());
                g2.dispose();
            }
        };
        viewport.setOpaque(true);
        viewport.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scrollPane.setViewport(viewport);
        scrollPane.setViewportView(cardStrip);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(horizontalPolicy);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(viewportWidth, viewportHeight));
        scrollPane.setMinimumSize(new Dimension(PROFILE_CARD_WIDTH, viewportHeight));
        scrollPane.setMaximumSize(new Dimension(viewportWidth, viewportHeight));
        allowPageWheelScroll(scrollPane);
        return scrollPane;
    }

    private void allowPageWheelScroll(JScrollPane innerScrollPane) {
        innerScrollPane.setWheelScrollingEnabled(false);
        java.awt.event.MouseWheelListener forwardWheel = evt -> {
            JScrollPane pageScrollPane = findPageScrollPane(innerScrollPane);
            if (pageScrollPane == null) {
                return;
            }

            JScrollBar pageBar = pageScrollPane.getVerticalScrollBar();
            int increment = evt.getScrollType() == java.awt.event.MouseWheelEvent.WHEEL_BLOCK_SCROLL
                    ? pageBar.getBlockIncrement()
                    : pageBar.getUnitIncrement() * Math.max(1, evt.getScrollAmount());
            int delta = (int) Math.round(evt.getPreciseWheelRotation() * increment);
            if (delta == 0 && evt.getPreciseWheelRotation() != 0) {
                delta = evt.getPreciseWheelRotation() > 0 ? 1 : -1;
            }
            int max = pageBar.getMaximum() - pageBar.getVisibleAmount();
            pageBar.setValue(Math.max(pageBar.getMinimum(), Math.min(max, pageBar.getValue() + delta)));
            evt.consume();
        };
        addWheelForwarder(innerScrollPane, forwardWheel);
    }

    private JScrollPane findPageScrollPane(JScrollPane innerScrollPane) {
        Container parent = innerScrollPane.getParent();
        while (parent != null) {
            if (parent instanceof JScrollPane) {
                return (JScrollPane) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private void addWheelForwarder(Component component, java.awt.event.MouseWheelListener forwardWheel) {
        component.addMouseWheelListener(forwardWheel);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                addWheelForwarder(child, forwardWheel);
            }
        }
    }

    private int getRecentActivityContentHeight(int bottomGap) {
        return (PROFILE_COLLAPSED_ROWS * PROFILE_CARD_HEIGHT)
                + ((PROFILE_COLLAPSED_ROWS - 1) * PROFILE_CARD_GAP)
                + bottomGap;
    }

    private int getRecentActivityCollapsedCardLimit() {
        return getRecentActivityVisibleColumns() * PROFILE_COLLAPSED_ROWS;
    }

    private int getProfileCardViewportWidth(boolean fixedRecentActivity) {
        int columns = fixedRecentActivity ? getRecentActivityVisibleColumns() : getStandardSectionVisibleColumns();
        return getRecentActivityWidthForColumns(columns);
    }

    private int getRecentActivityVisibleColumns() {
        return RECENT_ACTIVITY_COLUMNS;
    }

    private int getRecentActivityWidthForColumns(int columns) {
        int safeColumns = Math.max(1, columns);
        return (PROFILE_CARD_WIDTH * safeColumns)
                + ((safeColumns - 1) * PROFILE_CARD_GAP);
    }

    private int getRecentActivityArrowWidth() {
        return RECENT_ACTIVITY_ARROW_WIDTH;
    }

    private int getRecentActivityArrowGap() {
        return RECENT_ACTIVITY_ARROW_GAP;
    }

    private JPopupMenu createFavoritesGenreMenu(List<MediaItem> favorites, BiConsumer<String, String> onFilterSelect) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(StyleConfig.BACKGROUND_LIGHT);
        popup.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 42), 1));

        JMenuItem allFavorites = new JMenuItem("All Favorites");
        allFavorites.setFont(new Font("Segoe UI", Font.BOLD, 12));
        allFavorites.setForeground(StyleConfig.TEXT_COLOR);
        allFavorites.setBackground(StyleConfig.BACKGROUND_LIGHT);
        allFavorites.setOpaque(true);
        // Button action: clear favorites filter.
        allFavorites.addActionListener(e -> onFilterSelect.accept(null, null));
        popup.add(allFavorites);
        popup.addSeparator();

        Map<String, Map<String, Integer>> byCategory = buildFavoriteGenreCounts(favorites);
        addCategoryGenreMenuItem(popup, "Films", byCategory.get("Films"), onFilterSelect);
        addCategoryGenreMenuItem(popup, "Games", byCategory.get("Games"), onFilterSelect);
        addCategoryGenreMenuItem(popup, "Books", byCategory.get("Books"), onFilterSelect);

        return popup;
    }

    private List<MediaItem> applyFavoritesFilter(List<MediaItem> favorites) {
        if (favoritesFilterCategory == null) {
            return new ArrayList<>(favorites);
        }

        List<MediaItem> filtered = new ArrayList<>();
        for (MediaItem item : favorites) {
            if (item == null) {
                continue;
            }
            if (!favoritesFilterCategory.equals(item.getCategory())) {
                continue;
            }
            if (favoritesFilterGenre == null || favoriteMatchesGenre(item, favoritesFilterGenre)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private boolean favoriteMatchesGenre(MediaItem item, String targetGenre) {
        if (targetGenre == null) {
            return true;
        }
        String rawGenre = item.getGenre();
        if (rawGenre == null || rawGenre.trim().isEmpty()) {
            return "Unknown".equalsIgnoreCase(targetGenre);
        }
        String[] parts = rawGenre.split("\\s*,\\s*");
        for (String part : parts) {
            if (part != null && part.trim().equalsIgnoreCase(targetGenre)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Map<String, Integer>> buildFavoriteGenreCounts(List<MediaItem> favorites) {
        Map<String, Map<String, Integer>> byCategory = new LinkedHashMap<>();
        byCategory.put("Films", new LinkedHashMap<>());
        byCategory.put("Games", new LinkedHashMap<>());
        byCategory.put("Books", new LinkedHashMap<>());

        for (MediaItem item : favorites) {
            if (item == null) {
                continue;
            }
            String category = item.getCategory();
            if (category == null) {
                continue;
            }
            if (!byCategory.containsKey(category)) {
                continue;
            }

            String rawGenre = item.getGenre();
            if (rawGenre == null || rawGenre.trim().isEmpty()) {
                byCategory.get(category).merge("Unknown", 1, Integer::sum);
                continue;
            }

            String[] parts = rawGenre.split("\\s*,\\s*");
            for (String part : parts) {
                String genre = part.trim();
                if (genre.isEmpty()) {
                    genre = "Unknown";
                }
                byCategory.get(category).merge(genre, 1, Integer::sum);
            }
        }

        return byCategory;
    }

    private void addCategoryGenreMenuItem(JPopupMenu popup, String category, Map<String, Integer> genres,
            BiConsumer<String, String> onFilterSelect) {
        Map<String, Integer> safeGenres = genres != null ? genres : new LinkedHashMap<>();
        int total = safeGenres.values().stream().mapToInt(Integer::intValue).sum();

        JMenu categoryMenu = new JMenu(category + " (" + total + ")");
        categoryMenu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        categoryMenu.setForeground(StyleConfig.TEXT_COLOR);
        categoryMenu.setOpaque(true);
        categoryMenu.setBackground(StyleConfig.BACKGROUND_LIGHT);

        JMenuItem allInCategory = new JMenuItem("All " + category + " (" + total + ")");
        allInCategory.setFont(new Font("Segoe UI", Font.BOLD, 12));
        allInCategory.setForeground(StyleConfig.TEXT_COLOR);
        allInCategory.setOpaque(true);
        allInCategory.setBackground(StyleConfig.BACKGROUND_LIGHT);
        // Button action: filter favorites to this category.
        allInCategory.addActionListener(e -> onFilterSelect.accept(category, null));
        categoryMenu.add(allInCategory);
        categoryMenu.addSeparator();

        if (safeGenres.isEmpty()) {
            JMenuItem empty = new JMenuItem("No favorites yet");
            empty.setEnabled(false);
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            empty.setForeground(StyleConfig.TEXT_LIGHT);
            empty.setOpaque(true);
            empty.setBackground(StyleConfig.BACKGROUND_LIGHT);
            categoryMenu.add(empty);
        } else {
            safeGenres.entrySet().stream()
                    .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed()
                            .thenComparing(Map.Entry::getKey))
                    .forEach(entry -> {
                        JMenuItem genreItem = new JMenuItem(entry.getKey() + " (" + entry.getValue() + ")");
                        genreItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        genreItem.setForeground(Color.WHITE);
                        genreItem.setOpaque(true);
                        genreItem.setBackground(StyleConfig.BACKGROUND_LIGHT);
                        // Button action: filter favorites to this category and genre.
                        genreItem.addActionListener(e -> onFilterSelect.accept(category, entry.getKey()));
                        categoryMenu.add(genreItem);
                    });
        }

        popup.add(categoryMenu);
    }

    
    

    private void showEditDialog() {
        JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Profile", true);
        editDialog.setSize(560, 620);
        editDialog.setLocationRelativeTo(this);
        editDialog.setUndecorated(true);
        editDialog.setBackground(new Color(0, 0, 0, 0));

        
        JPanel container = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                
                for (int i = 4; i > 0; i--) {
                    g2.setColor(StyleConfig.withAlpha(StyleConfig.PRIMARY_COLOR, 8 * i));
                    g2.fill(new RoundRectangle2D.Float(4 - i, 4 - i,
                            getWidth() - 8 + 2 * i, getHeight() - 8 + 2 * i, 28 + i * 2, 28 + i * 2));
                }

                
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.SURFACE_ELEVATED, 0, getHeight(), StyleConfig.SURFACE_COLOR));
                g2.fill(new RoundRectangle2D.Float(4, 4, getWidth() - 8, getHeight() - 8, 28, 28));

                
                g2.setColor(StyleConfig.SURFACE_STROKE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(4.5f, 4.5f, getWidth() - 9, getHeight() - 9, 27, 27));
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        
        JPanel accentBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.PRIMARY_COLOR,
                        getWidth(), 0, new Color(94, 194, 255, 120)));
                
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), 28, 24, 24));
                g2.fillRect(0, 0, getWidth(), 4);
                g2.dispose();
            }
        };
        accentBar.setOpaque(false);
        accentBar.setPreferredSize(new Dimension(0, 4));

        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(32, 42, 32, 42));

        
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

                
                if (avatarHovered[0]) {
                    for (int i = 3; i > 0; i--) {
                        g2.setColor(StyleConfig.withAlpha(StyleConfig.PRIMARY_COLOR, 20 * i));
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.draw(new Ellipse2D.Float(pad - i, -i, sz + 2 * i, sz + 2 * i));
                    }
                }

                
                g2.setColor(StyleConfig.INPUT_BG);
                g2.fill(new Ellipse2D.Float(pad, 0, sz, sz));

                
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

                    
                    g2.setColor(StyleConfig.INPUT_BG);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new Ellipse2D.Float(pad + 3, 3, sz - 6, sz - 6));
                } else {
                    String initial = profile.getUsername() != null && !profile.getUsername().isEmpty()
                            ? profile.getUsername().substring(0, 1).toUpperCase()
                            : "U";
                    g2.setColor(StyleConfig.TEXT_SECONDARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 42));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(initial, pad + (sz - fm.stringWidth(initial)) / 2,
                            (sz + fm.getAscent() - fm.getDescent()) / 2);
                }

                
                g2.setColor(new Color(255, 255, 255, avatarHovered[0] ? 52 : 20));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new Ellipse2D.Float(pad + 1, 1, sz - 2, sz - 2));

                
                int bx = pad + sz - 30, by = sz - 30, bsz = 28;
                g2.setColor(avatarHovered[0] ? StyleConfig.PRIMARY_COLOR : StyleConfig.SURFACE_SOFT);
                g2.fill(new Ellipse2D.Float(bx, by, bsz, bsz));
                g2.setColor(new Color(255, 255, 255, avatarHovered[0] ? 220 : 160));
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
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
                // Button action: choose a new profile avatar image.
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

        
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        
        JLabel userFieldLabel = new JLabel("<html><span style='letter-spacing: 1.5px;'>USERNAME</span></html>");
        userFieldLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 10));
        userFieldLabel.setForeground(StyleConfig.TEXT_LIGHT);
        userFieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(userFieldLabel);
        form.add(Box.createVerticalStrut(6));

        
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

                
                g2.setColor(focused ? StyleConfig.INPUT_BG_FOCUS : StyleConfig.INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));

                
                if (focused) {
                    g2.setColor(StyleConfig.INPUT_FOCUS);
                    g2.setStroke(new BasicStroke(2f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 13, 13));
                } else {
                    g2.setColor(StyleConfig.SURFACE_STROKE);
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

        
        JLabel bioCountLabel = new JLabel("0/150");
        bioCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bioCountLabel.setForeground(StyleConfig.TEXT_LIGHT);

        JPanel bioLabelRow = new JPanel(new BorderLayout());
        bioLabelRow.setOpaque(false);
        bioLabelRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bioLabelRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));

        JLabel bioFieldLabel = new JLabel("<html><span style='letter-spacing: 1.5px;'>BIO</span></html>");
        bioFieldLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 10));
        bioFieldLabel.setForeground(StyleConfig.TEXT_LIGHT);
        bioLabelRow.add(bioFieldLabel, BorderLayout.WEST);
        bioLabelRow.add(bioCountLabel, BorderLayout.EAST);
        form.add(bioLabelRow);
        form.add(Box.createVerticalStrut(6));

        
        JTextArea bioField = new JTextArea(profile.getBio()) {
            {
                addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent e) {
                        getParent().getParent().repaint(); 
                    }

                    public void focusLost(java.awt.event.FocusEvent e) {
                        getParent().getParent().repaint();
                    }
                });
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

        
        String bioText = profile.getBio() != null ? profile.getBio() : "";
        bioCountLabel.setText(bioText.length() + "/150");
        bioField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                int len = bioField.getText().length();
                bioCountLabel.setText(len + "/150");
                bioCountLabel.setForeground(len > 150 ? StyleConfig.ERROR_COLOR : StyleConfig.TEXT_LIGHT);
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }
        });

        JScrollPane bioScroll = new JScrollPane(bioField) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean focused = bioField.hasFocus();
                g2.setColor(focused ? StyleConfig.INPUT_BG_FOCUS : StyleConfig.INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));

                if (focused) {
                    g2.setColor(StyleConfig.INPUT_FOCUS);
                    g2.setStroke(new BasicStroke(2f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 13, 13));
                } else {
                    g2.setColor(StyleConfig.SURFACE_STROKE);
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

        
        JPanel btnDivider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(StyleConfig.DIVIDER_COLOR);
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

        
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        // Secondary button to close the edit-profile dialog without saving.
        RoundedButton cancelBtn = new RoundedButton("Cancel", StyleConfig.SURFACE_SOFT, 14);
        cancelBtn.setPreferredSize(new Dimension(110, 42));
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Button action: close profile editor without saving.
        cancelBtn.addActionListener(e -> editDialog.dispose());
        buttons.add(cancelBtn);

        // Primary button that saves profile updates.
        RoundedButton saveBtn = new RoundedButton("Save Changes", StyleConfig.PRIMARY_COLOR, 14);
        saveBtn.setGradient(StyleConfig.PRIMARY_DARK);
        saveBtn.setPreferredSize(new Dimension(150, 42));
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        // Button action: validate and save profile edits.
        saveBtn.addActionListener(e -> {
            String newUsername = userField.getText().trim();
            if (!newUsername.equals(profile.getUsername())) {
                AuthService authService = new AuthService();
                if (authService.isUsernameTaken(newUsername)) {
                    JOptionPane.showMessageDialog(editDialog, "the username is already taken", "Error",
                            JOptionPane.ERROR_MESSAGE);
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
