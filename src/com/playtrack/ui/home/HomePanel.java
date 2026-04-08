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

public class HomePanel extends JPanel {
    private SummaryService summaryService = new SummaryService();
    private JPanel statsContainer;
    private JPanel recentCardsPanel;
    private JPanel cardClipWrapper;
    private JLabel downArrow;
    private String username;
    private int userId;

    // 8 cards per row x 2 rows = 16 cards max visible
    private static final int MAX_VISIBLE_CARDS = 16;
    // Card height=240, vgap=20 → 2 rows = 240 + 20 + 240 = 500
    private static final int TWO_ROW_HEIGHT = 500;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Subtle glowing orb top-right
        int orbSize = 400;
        g2.setPaint(new RadialGradientPaint(
            getWidth() - 100f, 80f, orbSize / 2f,
            new float[]{0f, 0.4f, 1f},
            new Color[]{new Color(211, 64, 69, 25), new Color(211, 64, 69, 8), new Color(0, 0, 0, 0)}
        ));
        g2.fillOval(getWidth() - 100 - orbSize / 2, 80 - orbSize / 2, orbSize, orbSize);

        // Subtle secondary orb bottom-left
        int orb2 = 300;
        g2.setPaint(new RadialGradientPaint(
            120f, getHeight() - 120f, orb2 / 2f,
            new float[]{0f, 0.5f, 1f},
            new Color[]{new Color(211, 64, 69, 15), new Color(211, 64, 69, 5), new Color(0, 0, 0, 0)}
        ));
        g2.fillOval(120 - orb2 / 2, getHeight() - 120 - orb2 / 2, orb2, orb2);

        g2.dispose();
    }

    private Consumer<String> onNavigate;

    public HomePanel(Consumer<String> onAddMedia, Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setLayout(new BorderLayout());
        setBackground(StyleConfig.BACKGROUND_COLOR);

        User user = SessionManager.getCurrentUser();
        username = (user != null) ? user.getUsername() : "User";
        userId = (user != null) ? user.getId() : 0;

        // Custom panel to ensure the content stretches to the full width of the window
        class ScrollablePanel extends JPanel implements Scrollable {
            public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
            public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 20; }
            public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 60; }
            public boolean getScrollableTracksViewportWidth() { return true; }
            public boolean getScrollableTracksViewportHeight() { return false; }
        }

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.setBorder(BorderFactory.createEmptyBorder(20, 50, 0, 50));

        JPanel mainContent = new ScrollablePanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(0, 50, 40, 50));

        JPanel welcomeRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        welcomeRow.setOpaque(false);
        welcomeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        welcomeRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JLabel welcomeLabel = new JLabel("Welcome Back, " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(StyleConfig.FONT_TITLE);
        welcomeLabel.setForeground(StyleConfig.TEXT_COLOR);
        welcomeRow.add(welcomeLabel);

        topSection.add(welcomeRow);
        topSection.add(Box.createVerticalStrut(20));

        // Use OverlayLayout so the stats are truly centered across the full width,
        // while the "+" button floats on top at the right edge.
        JPanel statsRowWrapper = new JPanel() {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        statsRowWrapper.setLayout(new OverlayLayout(statsRowWrapper));
        statsRowWrapper.setOpaque(false);
        statsRowWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        statsContainer.setOpaque(false);
        statsContainer.setAlignmentX(0.5f);
        statsContainer.setAlignmentY(0.5f);
        refreshStats();

        RoundedButton addBtn = new RoundedButton("+ New", StyleConfig.PRIMARY_COLOR, 45);
        addBtn.setPreferredSize(new Dimension(120, 45));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        addBtn.setForeground(Color.WHITE);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

        // ── Recent Activity Section ──
        JPanel recentActivityWrapper = new JPanel(new BorderLayout());
        recentActivityWrapper.setOpaque(false);
        recentActivityWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel recentTitle = new JLabel("Recent Activity");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        recentTitle.setForeground(StyleConfig.TEXT_COLOR);
        recentTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(recentTitle);

        headerPanel.add(Box.createVerticalStrut(8));

        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 0), w / 2, 0, StyleConfig.PRIMARY_COLOR));
                g2.fillRect(0, 0, w / 2, 2);
                g2.setPaint(new GradientPaint(w / 2, 0, StyleConfig.PRIMARY_COLOR, w, 0, new Color(0, 0, 0, 0)));
                g2.fillRect(w / 2, 0, w / 2, 2);
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        divider.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(divider);

        recentActivityWrapper.add(headerPanel, BorderLayout.NORTH);

        // Cards panel — uses WrapLayout to wrap naturally based on available width
        recentCardsPanel = new JPanel();
        recentCardsPanel.setLayout(new WrapLayout(WrapLayout.CENTER, 20, 20));
        recentCardsPanel.setOpaque(false);
        recentCardsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Clip wrapper — limits visible height to 2 rows of cards
        cardClipWrapper = new JPanel(new BorderLayout());
        cardClipWrapper.setOpaque(false);
        cardClipWrapper.add(recentCardsPanel, BorderLayout.CENTER);

        recentActivityWrapper.add(cardClipWrapper, BorderLayout.CENTER);

        // Down-arrow icon — appears when there are more than 2 rows of cards
        final boolean[] expanded = {false};

        downArrow = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 28;
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                // down arrow when collapsed, up arrow when expanded
                UIUtils.drawVerticalArrowIcon(g2, cx - size / 2, cy - size / 2, size, size, getForeground(), !expanded[0]);
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
                expanded[0] = !expanded[0];
                if (expanded[0]) {
                    // Remove height constraint — show all cards
                    cardClipWrapper.setPreferredSize(null);
                    cardClipWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
                    downArrow.setToolTipText("Show less");
                } else {
                    // Collapse back to 2 rows
                    cardClipWrapper.setPreferredSize(new Dimension(0, TWO_ROW_HEIGHT));
                    cardClipWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, TWO_ROW_HEIGHT));
                    downArrow.setToolTipText("Show more");
                }
                downArrow.repaint();
                recentActivityWrapper.revalidate();
                recentActivityWrapper.repaint();
                // Revalidate up the hierarchy so scroll pane adjusts
                Container parent = recentActivityWrapper.getParent();
                while (parent != null) {
                    parent.revalidate();
                    parent.repaint();
                    parent = parent.getParent();
                }
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
        add(mainScroll, BorderLayout.CENTER);
    }

    private void loadRecentCards() {
        recentCardsPanel.removeAll();
        ReviewDAO reviewDAO = new ReviewDAO();
        MediaDAO mediaDAO = new MediaDAO();
        List<com.playtrack.model.Review> recentReviews = reviewDAO.getRecentReviews(userId, 50);
        // Batch fetch all media to avoid N+1 DB queries
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

        for (int i = 0; i < recentItems.size(); i++) {
            recentCardsPanel.add(new MediaCard(recentItems.get(i), false, false, this::refreshRecentActivity));
        }

        // Show the down arrow when there are more than 16 cards (8 per row x 2 rows)
        boolean hasOverflow = recentItems.size() > MAX_VISIBLE_CARDS;
        if (downArrow != null) {
            downArrow.setVisible(hasOverflow);
        }

        // Apply 2-row height clip when there's overflow
        if (hasOverflow) {
            cardClipWrapper.setPreferredSize(new Dimension(0, TWO_ROW_HEIGHT));
            cardClipWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, TWO_ROW_HEIGHT));
        } else {
            cardClipWrapper.setPreferredSize(null);
            cardClipWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
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

        statsContainer.add(createHomeStatCard("Films", counts.get("Films")));
        statsContainer.add(createHomeStatCard("Games Played", counts.get("Games")));
        statsContainer.add(createHomeStatCard("Books Read", counts.get("Books")));
        statsContainer.revalidate();
        statsContainer.repaint();
    }

    private JPanel createHomeStatCard(String label, int count) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Deep premium background
                g2.setColor(new Color(30, 35, 45));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));

                // Subtle glassy top highlight
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 12), 0, getHeight() / 2, new Color(255, 255, 255, 0)));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));

                // Clean translucent border
                g2.setColor(new Color(255, 255, 255, 20));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 24, 24));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(240, 80));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        String catLabel = label.contains("Films") ? "Films" : (label.contains("Games") ? "Games" : "Books");
        card.setToolTipText("Go to " + catLabel);
        
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (onNavigate != null) {
                    onNavigate.accept(catLabel);
                }
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 15, 0, 10);
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;

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
        icon.setPreferredSize(new Dimension(60, 60));
        card.add(icon, gbc);

        gbc.gridheight = 1;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(15, 0, 0, 15);
        JLabel title = new JLabel(label);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(StyleConfig.TEXT_COLOR);
        card.add(title, gbc);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 15, 15);
        JLabel val = new JLabel(count + " Logged");
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setForeground(StyleConfig.TEXT_SECONDARY);
        card.add(val, gbc);

        return card;
    }
}