package com.playtrack.ui.home;

import com.playtrack.dao.MediaDAO;
import com.playtrack.dao.ReviewDAO;
import com.playtrack.model.MediaItem;
import com.playtrack.model.User;
import com.playtrack.service.SummaryService;
import com.playtrack.ui.components.*;
import com.playtrack.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
// Home panel.
public class HomePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private SummaryService summaryService = new SummaryService();
    private JPanel statsContainer;
    private JPanel recentCardsPanel;
    private JLabel downArrow;
    private boolean recentExpanded = false;
    private String username;
    private int userId;

    // Constants for layout and sizing of recent activity cards and statistics cards.
    private static final int RECENT_COLUMNS = 8;
    private static final int RECENT_COLLAPSED_ROWS = 2;
    private static final int RECENT_CARD_WIDTH = 160;
    private static final int RECENT_CARD_HEIGHT = 240;
    private static final int RECENT_CARD_GAP = 20;
    private static final int MAX_VISIBLE_CARDS = RECENT_COLUMNS * RECENT_COLLAPSED_ROWS;
    private static final int STAT_CARD_WIDTH = 240;
    private static final int STAT_CARD_HEIGHT = 80;
    private static final int STAT_CARD_GAP = 30;
    private static final int STAT_CARD_COUNT = 3;
    private static final int STATS_ROW_WIDTH =
        (STAT_CARD_WIDTH * STAT_CARD_COUNT) + (STAT_CARD_GAP * (STAT_CARD_COUNT - 1));
    // Method to refresh the statistics displayed in the stats row.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        UIUtils.paintFadedAuthBackground(g2, getWidth(), getHeight());

        // Draw decorative gradient orbs in the background for visual interest.
        int orbSize = 400;
        g2.setPaint(new RadialGradientPaint(
            getWidth() - 100f, 80f, orbSize / 2f,
            new float[]{0f, 0.4f, 1f},
            new Color[]{StyleConfig.PANEL_GLOW_SECONDARY, new Color(StyleConfig.PALETTE_PEACH.getRed(), StyleConfig.PALETTE_PEACH.getGreen(), StyleConfig.PALETTE_PEACH.getBlue(), 8), new Color(0, 0, 0, 0)}
        ));
        g2.fillOval(getWidth() - 100 - orbSize / 2, 80 - orbSize / 2, orbSize, orbSize);

        
        int orb2 = 300;
        g2.setPaint(new RadialGradientPaint(
            120f, getHeight() - 120f, orb2 / 2f,
            new float[]{0f, 0.5f, 1f},
            new Color[]{StyleConfig.PANEL_GLOW_PRIMARY, new Color(StyleConfig.PALETTE_RED.getRed(), StyleConfig.PALETTE_RED.getGreen(), StyleConfig.PALETTE_RED.getBlue(), 8), new Color(0, 0, 0, 0)}
        ));
        g2.fillOval(120 - orb2 / 2, getHeight() - 120 - orb2 / 2, orb2, orb2);

        g2.dispose();
    }
    // Method to refresh the statistics displayed in the stats row.
    private Consumer<String> onNavigate;

    public HomePanel(Consumer<String> onAddMedia, Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setLayout(new BorderLayout());
        setBackground(StyleConfig.BACKGROUND_COLOR);

        User user = SessionManager.getCurrentUser();
        username = (user != null) ? user.getUsername() : "User";
        userId = (user != null) ? user.getId() : 0;

        // Inner class for a scrollable panel with a custom background.
        class ScrollablePanel extends JPanel implements Scrollable {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Point p = SwingUtilities.convertPoint(this, 0, 0, HomePanel.this);
                g2.translate(-p.x, -p.y);
                UIUtils.paintFadedAuthBackground(g2, HomePanel.this.getWidth(), HomePanel.this.getHeight());
                g2.dispose();
            }
            public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
            public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 20; }
            public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 60; }
            public boolean getScrollableTracksViewportWidth() { return true; }
            public boolean getScrollableTracksViewportHeight() { return false; }
        }
        // Top section with welcome message and stats row.
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.setBorder(BorderFactory.createEmptyBorder(20, 50, 0, 50));
        // Main content area with recent activity.
        JPanel mainContent = new ScrollablePanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(0, 50, 40, 50));
        // Welcome message at the top of the home panel.
        JPanel welcomeRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        welcomeRow.setOpaque(false);
        welcomeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        welcomeRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        // Welcome message at the top of the home panel.
        JLabel welcomeLabel = new JLabel("Welcome Back, " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(StyleConfig.FONT_TITLE);
        welcomeLabel.setForeground(StyleConfig.TEXT_COLOR);
        welcomeRow.add(welcomeLabel);

        topSection.add(welcomeRow);
        topSection.add(Box.createVerticalStrut(20));

        
        // Stats row with key metrics.
        JPanel statsRowWrapper = new JPanel() {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        statsRowWrapper.setLayout(new OverlayLayout(statsRowWrapper));
        statsRowWrapper.setOpaque(false);
        statsRowWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsContainer = new JPanel();
        statsContainer.setLayout(new BoxLayout(statsContainer, BoxLayout.X_AXIS));
        statsContainer.setOpaque(false);
        statsContainer.setAlignmentX(0.5f);
        statsContainer.setAlignmentY(0.5f);
        Dimension fixedStatsRow = new Dimension(STATS_ROW_WIDTH, STAT_CARD_HEIGHT);
        statsContainer.setPreferredSize(fixedStatsRow);
        statsContainer.setMinimumSize(fixedStatsRow);
        statsContainer.setMaximumSize(fixedStatsRow);
        refreshStats();

        // Main CTA button for opening the add-media dropdown.
        RoundedButton addBtn = new RoundedButton("+ New", StyleConfig.PRIMARY_COLOR, 45);
        addBtn.setGradient(StyleConfig.PRIMARY_DARK);
        addBtn.setPreferredSize(new Dimension(120, 45));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        addBtn.setForeground(Color.WHITE);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Dropdown menu that lets users choose what type of item to add.
        AddMediaDropdown addMenu = new AddMediaDropdown(onAddMedia);

        addBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int popupWidth = addMenu.getPreferredSize().width;
                if (popupWidth == 0) popupWidth = 150;
                addMenu.show(addBtn, addBtn.getWidth() - popupWidth, addBtn.getHeight() + 8);
            }
        });

        JPanel addBtnLayer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        addBtnLayer.setOpaque(false);
        addBtnLayer.setAlignmentX(0.5f);
        addBtnLayer.setAlignmentY(0.5f);
        addBtnLayer.add(addBtn);

        statsRowWrapper.add(addBtnLayer);
        statsRowWrapper.add(statsContainer);
        topSection.add(statsRowWrapper);
        topSection.add(Box.createVerticalStrut(30));

        add(topSection, BorderLayout.NORTH);

        // Recent activity section with expandable cards.
        JPanel recentActivityWrapper = new JPanel(new BorderLayout());
        recentActivityWrapper.setOpaque(false);
        recentActivityWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Header for the recent activity section.
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Title for the recent activity section.
        JLabel recentTitle = new JLabel("Recent Activity");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        recentTitle.setForeground(StyleConfig.TEXT_SECONDARY);
        recentTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(recentTitle);

        headerPanel.add(Box.createVerticalStrut(8));

        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 0), w / 2, 0, StyleConfig.SECONDARY_COLOR));
                g2.fillRect(0, 0, w / 2, 2);
                g2.setPaint(new GradientPaint(w / 2, 0, StyleConfig.SECONDARY_COLOR, w, 0, new Color(0, 0, 0, 0)));
                g2.fillRect(w / 2, 0, w / 2, 2);
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        divider.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(divider);

        recentActivityWrapper.add(headerPanel, BorderLayout.NORTH);

        
        recentCardsPanel = new JPanel(new GridBagLayout());
        recentCardsPanel.setOpaque(false);
        recentCardsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JPanel cardsCenterWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        cardsCenterWrapper.setOpaque(false);
        cardsCenterWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cardsCenterWrapper.add(recentCardsPanel);

        recentActivityWrapper.add(cardsCenterWrapper, BorderLayout.CENTER);

        // Down arrow button for expanding recent activity.
        downArrow = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 28;
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                
                UIUtils.drawVerticalArrowIcon(g2, cx - size / 2, cy - size / 2, size, size, getForeground(), !recentExpanded);
                g2.dispose();
            }
        };
        downArrow.setForeground(StyleConfig.TEXT_SECONDARY);
        downArrow.setPreferredSize(new Dimension(100, 40));
        downArrow.setHorizontalAlignment(SwingConstants.CENTER);
        downArrow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        downArrow.setToolTipText("Show more");
        downArrow.setVisible(false);

        downArrow.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                recentExpanded = !recentExpanded;
                loadRecentCards();
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                downArrow.setForeground(StyleConfig.PRIMARY_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                downArrow.setForeground(StyleConfig.TEXT_SECONDARY);
            }
        });

        JPanel arrowWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        arrowWrapper.setOpaque(false);
        arrowWrapper.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        arrowWrapper.add(downArrow);

        recentActivityWrapper.add(arrowWrapper, BorderLayout.SOUTH);

        loadRecentCards();

        recentActivityWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        mainContent.add(recentActivityWrapper);

        JScrollPane mainScroll = new JScrollPane(mainContent);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        mainScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainScroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        mainScroll.getViewport().addChangeListener(e -> {
            Rectangle vr = mainScroll.getViewport().getViewRect();
            mainContent.repaint(vr.x, vr.y, vr.width, vr.height);
        });
        add(mainScroll, BorderLayout.CENTER);
    }
    // Method to load recent activity cards.
    private void loadRecentCards() {
        recentCardsPanel.removeAll();
        ReviewDAO reviewDAO = new ReviewDAO();
        MediaDAO mediaDAO = new MediaDAO();
        List<com.playtrack.model.Review> recentReviews = reviewDAO.getRecentReviews(userId, 50);
        // Create a map of media items for quick lookup based on media ID.
        java.util.Map<Integer, MediaItem> mediaMap = new java.util.HashMap<>();
        for (MediaItem mi : mediaDAO.getMediaByUser(userId, "All")) {
            mediaMap.put(mi.getId(), mi);
        }
        List<MediaItem> recentItems = new ArrayList<>();
        for (com.playtrack.model.Review review : recentReviews) {
            MediaItem mediaItem = mediaMap.get(review.getMediaId());
            if (mediaItem != null) {
                recentItems.add(mediaItem);
            }
        }

        if (recentItems.isEmpty()) {
            JPanel emptyState = new JPanel();
            emptyState.setLayout(new BoxLayout(emptyState, BoxLayout.Y_AXIS));
            emptyState.setOpaque(false);
            emptyState.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));
            // Display an empty state message when there are no recent activity items to show.
            JLabel emptyTitle = new JLabel("No activity yet");
            emptyTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
            emptyTitle.setForeground(StyleConfig.TEXT_SECONDARY);
            emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyState.add(emptyTitle);
            // Display an empty state message when there are no recent activity items to show.
            JLabel emptyHint = new JLabel("Start logging films, games, or books.");
            emptyHint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyHint.setForeground(StyleConfig.TEXT_LIGHT);
            emptyHint.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyState.add(Box.createVerticalStrut(8));
            emptyState.add(emptyHint);
            // Display an empty state message when there are no recent activity items to show.
            GridBagConstraints emptyGbc = new GridBagConstraints();
            emptyGbc.gridx = 0;
            emptyGbc.gridy = 0;
            emptyGbc.anchor = GridBagConstraints.CENTER;
            recentCardsPanel.add(emptyState, emptyGbc);

            int emptyWidth = Math.max(780, getWidth() - 140);
            Dimension emptySize = new Dimension(emptyWidth, 170);
            recentCardsPanel.setPreferredSize(emptySize);
            recentCardsPanel.setMinimumSize(emptySize);

            if (downArrow != null) {
                downArrow.setVisible(false);
            }

            recentCardsPanel.revalidate();
            recentCardsPanel.repaint();
            return;
        }

        boolean hasOverflow = recentItems.size() > MAX_VISIBLE_CARDS;
        if (!hasOverflow) {
            recentExpanded = false;
        }

        int visibleCount = recentExpanded ? recentItems.size() : Math.min(MAX_VISIBLE_CARDS, recentItems.size());
        int rows = Math.max(1, (visibleCount + RECENT_COLUMNS - 1) / RECENT_COLUMNS);

        for (int i = 0; i < visibleCount; i++) {
            int col = i % RECENT_COLUMNS;
            int row = i / RECENT_COLUMNS;

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = col;
            gbc.gridy = row;
            gbc.insets = new Insets(
                0,
                col > 0 ? RECENT_CARD_GAP : 0,
                row < rows - 1 ? RECENT_CARD_GAP : 0,
                0
            );
            gbc.anchor = GridBagConstraints.NORTHWEST;

            recentCardsPanel.add(new MediaCard(recentItems.get(i), false, false, this::refreshRecentActivity), gbc);
        }

        int gridWidth = (RECENT_COLUMNS * RECENT_CARD_WIDTH) + ((RECENT_COLUMNS - 1) * RECENT_CARD_GAP);
        int gridHeight = (rows * RECENT_CARD_HEIGHT) + (Math.max(0, rows - 1) * RECENT_CARD_GAP);
        Dimension fixedGridSize = new Dimension(gridWidth, gridHeight);
        recentCardsPanel.setPreferredSize(fixedGridSize);
        recentCardsPanel.setMinimumSize(fixedGridSize);

        // Update visibility and tooltip of the down arrow based on whether there are more items to show and the current expanded state.
        if (downArrow != null) {
            downArrow.setVisible(hasOverflow);
            downArrow.setToolTipText(recentExpanded ? "Show less" : "Show more");
            downArrow.repaint();
        }

        recentCardsPanel.revalidate();
        recentCardsPanel.repaint();
    }

    public void refreshRecentActivity() {
        loadRecentCards();
    }

    public void refreshStats() {
        statsContainer.removeAll();
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }

        Map<String, Integer> counts = summaryService.getCategoryCounts(user.getId());
        // Create and add StatsCard components for each category (Films.
        statsContainer.add(createHomeStatCard("Films", counts.get("Films")));
        statsContainer.add(Box.createHorizontalStrut(STAT_CARD_GAP));
        statsContainer.add(createHomeStatCard("Games Played", counts.get("Games")));
        statsContainer.add(Box.createHorizontalStrut(STAT_CARD_GAP));
        statsContainer.add(createHomeStatCard("Books Read", counts.get("Books")));
        statsContainer.revalidate();
        statsContainer.repaint();
    }

    private JPanel createHomeStatCard(String label, int count) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                for (int i = 3; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 10 * i));
                    g2.fill(new RoundRectangle2D.Float(1 - i, 1 - i, getWidth() - 2 + i * 2, getHeight() - 2 + i * 2, 24 + i, 24 + i));
                }

                
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.SURFACE_ELEVATED, 0, getHeight(), StyleConfig.SURFACE_COLOR));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));

                
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 16), 0, getHeight() / 2, new Color(255, 255, 255, 0)));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));

                
                g2.setColor(StyleConfig.SURFACE_STROKE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 24, 24));
                g2.dispose();
            }
        };
        card.setLayout(null);
        card.setOpaque(false);
        Dimension cardSize = new Dimension(STAT_CARD_WIDTH, STAT_CARD_HEIGHT);
        card.setPreferredSize(cardSize);
        card.setMinimumSize(cardSize);
        card.setMaximumSize(cardSize);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Determine the category label for the tooltip based on the input label.
        String catLabel = label.contains("Films") ? "Films" : (label.contains("Games") ? "Games" : "Books");
        card.setToolTipText("Open " + catLabel + " in Library");
        
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (onNavigate != null) {
                    onNavigate.accept(catLabel);
                }
            }
        });
        // Icon for the stats card.
        JPanel icon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                String cat = label.contains("Films") ? "Films" : (label.contains("Games") ? "Games" : "Books");
                UIUtils.drawCategoryIcon(g2, cat, cx, cy, StyleConfig.PRIMARY_COLOR, 3f, 1.6);
                g2.dispose();
            }
        };
        icon.setOpaque(false);
        icon.setBounds(16, 10, 60, 60);
        card.add(icon);

        JLabel title = new JLabel(label);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(StyleConfig.TEXT_COLOR);
        title.setBounds(88, 18, 145, 22);
        card.add(title);
        // Value label for the stats card.
        JLabel val = new JLabel(count + " Logged");
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setForeground(StyleConfig.TEXT_SECONDARY);
        val.setBounds(88, 42, 145, 20);
        card.add(val);

        return card;
    }
}
