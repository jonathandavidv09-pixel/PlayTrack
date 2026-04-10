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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ProfilePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private ProfileService profileService = new ProfileService();
    private Profile profile;
    private JLabel usernameLabel;
    private JLabel bioLabel;
    private String favoritesFilterCategory;
    private String favoritesFilterGenre;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        UIUtils.paintFadedAuthBackground(g2, getWidth(), getHeight());

        int orbSize = 360;
        g2.setPaint(new RadialGradientPaint(
                getWidth() - 120f, 100f, orbSize / 2f,
                new float[] { 0f, 0.45f, 1f },
                new Color[] { StyleConfig.PANEL_GLOW_SECONDARY, new Color(StyleConfig.PALETTE_PEACH.getRed(), StyleConfig.PALETTE_PEACH.getGreen(), StyleConfig.PALETTE_PEACH.getBlue(), 8), new Color(0, 0, 0, 0) }));
        g2.fillOval(getWidth() - 120 - orbSize / 2, 100 - orbSize / 2, orbSize, orbSize);
        g2.dispose();
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
                    g2.setColor(new Color(255, 92, 109, 24));
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

        // Batch fetch all media items once to avoid N+1 DB queries
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
                return true;
            }

            public boolean getScrollableTracksViewportHeight() {
                return false;
            }
        }

        JPanel content = new ScrollablePanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        // Remove the 50px right margin from the parent container so we can use it for
        // the arrow
        content.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 0));

        content.add(header);
        content.add(Box.createVerticalStrut(30));
        content.add(createSection("Watchlists", true, watchlists, reviewDAO, userId));
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
        final int MAX_VISIBLE_CARDS = 16;
        final int TWO_ROW_HEIGHT = 500;

        final List<MediaItem> sourceItems = new ArrayList<>(items);
        boolean favoritesFiltered = "Favorites".equals(title) && favoritesFilterCategory != null;
        List<MediaItem> visibleItems = "Favorites".equals(title) ? applyFavoritesFilter(sourceItems) : new ArrayList<>(sourceItems);
        final List<MediaItem> allItems = new ArrayList<>(visibleItems);

        boolean hasMore = allItems.size() >= MAX_VISIBLE_CARDS && !favoritesFiltered;
        if (visibleItems.size() > MAX_VISIBLE_CARDS && !favoritesFiltered) {
            visibleItems = visibleItems.subList(0, MAX_VISIBLE_CARDS);
        }
        items = visibleItems;

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel topRow = new JPanel();
        topRow.setLayout(new BoxLayout(topRow, BoxLayout.Y_AXIS));
        topRow.setOpaque(false);
        // Apply the 50px right margin locally to topRow so the divider aligns with the
        // 8th card
        topRow.setBorder(BorderFactory.createEmptyBorder(0, 5, 15, 55));
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

        // Wrap titleRow in a BorderLayout to make it expand to full width
        JPanel titleRowWrapper = new JPanel(new BorderLayout());
        titleRowWrapper.setOpaque(false);
        titleRowWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRowWrapper.add(titleRow, BorderLayout.WEST);

        topRow.add(titleRowWrapper);

        topRow.add(Box.createVerticalStrut(8));

        // Gradient divider line
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

        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(
                new com.playtrack.ui.components.WrapLayout(com.playtrack.ui.components.WrapLayout.LEFT, 20, 20));
        rowPanel.setOpaque(false);
        rowPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        for (MediaItem mi : items) {
            rowPanel.add(new MediaCard(mi, true, false, this::refreshProfile));
        }

        wrapper.add(rowPanel, BorderLayout.CENTER);

        if (hasMore) {
            JLabel arrowIcon = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int size = 28;
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    UIUtils.drawArrowIcon(g2, cx - size / 2, cy - size / 2, size, getForeground(), true);
                    g2.dispose();
                }
            };
            arrowIcon.setForeground(StyleConfig.TEXT_SECONDARY);
            arrowIcon.setPreferredSize(new Dimension(80, 40));
            arrowIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30)); // Move arrow left within icon without
                                                                               // squishing it
            arrowIcon.setHorizontalAlignment(SwingConstants.CENTER);
            arrowIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
            arrowIcon.setToolTipText("Show more");

            JPanel arrowWrapper = new JPanel(new GridBagLayout());
            arrowWrapper.setOpaque(false);
            arrowWrapper.setPreferredSize(new Dimension(80, TWO_ROW_HEIGHT));
            arrowWrapper.add(arrowIcon);
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
                    wrapper.remove(arrowWrapper);

                    JPanel expandedRowPanel = new JPanel();
                    expandedRowPanel.setLayout(new GridLayout(2, 0, 20, 20));
                    expandedRowPanel.setOpaque(false);
                    expandedRowPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

                    for (MediaItem mi : allItems) {
                        expandedRowPanel.add(new MediaCard(mi, true, false, ProfilePanel.this::refreshProfile));
                    }

                    JPanel alignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
                    alignPanel.setOpaque(false);
                    alignPanel.add(expandedRowPanel);

                    JScrollPane scrollPane = new JScrollPane(alignPanel);
                    scrollPane.setOpaque(false);
                    scrollPane.getViewport().setOpaque(false);

                    // Add the margin back strictly to match the divider's 55px right margin,
                    // preventing the carousel from expanding past the layout bounds!
                    scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 55));
                    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

                    scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
                    scrollPane.getHorizontalScrollBar().setUnitIncrement(24);

                    // Disable mouse wheel integration so vertical scrolling passes through
                    scrollPane.setWheelScrollingEnabled(false);
                    scrollPane.addMouseWheelListener(evt -> {
                        JScrollPane parentScroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class,
                                scrollPane);
                        if (parentScroll != null) {
                            java.awt.event.MouseWheelEvent mwe = (java.awt.event.MouseWheelEvent) evt;
                            java.awt.event.MouseWheelEvent cloned = new java.awt.event.MouseWheelEvent(
                                    parentScroll, mwe.getID(), mwe.getWhen(), mwe.getModifiersEx(),
                                    0, 0, mwe.getXOnScreen(), mwe.getYOnScreen(),
                                    mwe.getClickCount(), mwe.isPopupTrigger(), mwe.getScrollType(),
                                    mwe.getScrollAmount(), mwe.getWheelRotation(), mwe.getPreciseWheelRotation());
                            parentScroll.dispatchEvent(cloned);
                        }
                    });

                    // Implementing smooth hold-and-drag panning
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
                                newX = Math.min(newX, viewport.getViewSize().width - viewport.getWidth());
                                viewport.setViewPosition(new Point(newX, viewPos.y));
                                origin = e.getLocationOnScreen();
                            }
                        }

                        @Override
                        public void mouseReleased(java.awt.event.MouseEvent e) {
                            origin = null;
                        }
                    };

                    alignPanel.addMouseListener(dragScroll);
                    alignPanel.addMouseMotionListener(dragScroll);
                    expandedRowPanel.addMouseListener(dragScroll);
                    expandedRowPanel.addMouseMotionListener(dragScroll);

                    // Apply drag panning locally to all cards so grabbhing a card works
                    for (Component c : expandedRowPanel.getComponents()) {
                        c.addMouseListener(dragScroll);
                        c.addMouseMotionListener(dragScroll);
                    }

                    wrapper.remove(rowPanel);
                    wrapper.add(scrollPane, BorderLayout.CENTER);

                    wrapper.revalidate();
                    wrapper.repaint();
                }
            });
        }

        return wrapper;
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
                        genreItem.addActionListener(e -> onFilterSelect.accept(category, entry.getKey()));
                        categoryMenu.add(genreItem);
                    });
        }

        popup.add(categoryMenu);
    }

    // Removed redundant showAddWatchlistDialog as it now uses the unified
    // ReviewFormDialog

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
                    g2.setColor(new Color(255, 92, 109, 8 * i));
                    g2.fill(new RoundRectangle2D.Float(4 - i, 4 - i,
                            getWidth() - 8 + 2 * i, getHeight() - 8 + 2 * i, 28 + i * 2, 28 + i * 2));
                }

                // Main body
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.SURFACE_ELEVATED, 0, getHeight(), StyleConfig.SURFACE_COLOR));
                g2.fill(new RoundRectangle2D.Float(4, 4, getWidth() - 8, getHeight() - 8, 28, 28));

                // Subtle inner border
                g2.setColor(StyleConfig.SURFACE_STROKE);
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
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.PRIMARY_COLOR,
                        getWidth(), 0, new Color(94, 194, 255, 120)));
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
                        g2.setColor(new Color(255, 92, 109, 20 * i));
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.draw(new Ellipse2D.Float(pad - i, -i, sz + 2 * i, sz + 2 * i));
                    }
                }

                // Background circle
                g2.setColor(StyleConfig.INPUT_BG);
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

                // Subtle ring
                g2.setColor(new Color(255, 255, 255, avatarHovered[0] ? 52 : 20));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new Ellipse2D.Float(pad + 1, 1, sz - 2, sz - 2));

                // Camera badge (bottom-right)
                int bx = pad + sz - 30, by = sz - 30, bsz = 28;
                g2.setColor(avatarHovered[0] ? StyleConfig.PRIMARY_COLOR : StyleConfig.SURFACE_SOFT);
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
        userFieldLabel.setForeground(StyleConfig.TEXT_LIGHT);
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
                g2.setColor(focused ? StyleConfig.INPUT_BG_FOCUS : StyleConfig.INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));

                // Focus glow border
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
        bioFieldLabel.setForeground(StyleConfig.TEXT_LIGHT);
        bioLabelRow.add(bioFieldLabel, BorderLayout.WEST);
        bioLabelRow.add(bioCountLabel, BorderLayout.EAST);
        form.add(bioLabelRow);
        form.add(Box.createVerticalStrut(6));

        // Bio field with focus glow
        JTextArea bioField = new JTextArea(profile.getBio()) {
            {
                addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent e) {
                        getParent().getParent().repaint(); // repaint scroll pane wrapper
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

        // Update character count
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

        // ── Divider before buttons ──
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

        // ── Buttons (right-aligned) ──
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        RoundedButton cancelBtn = new RoundedButton("Cancel", StyleConfig.SURFACE_SOFT, 14);
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
