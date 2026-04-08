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
    private JScrollPane recentScroll;
    private JLabel rightArrow;
    private String username;
    private int userId;

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

        // No top accent line

        g2.dispose();
    }

    public HomePanel(Consumer<String> onAddMedia) {
        setLayout(new BorderLayout());
        setBackground(StyleConfig.BACKGROUND_COLOR);

        User user = SessionManager.getCurrentUser();
        username = (user != null) ? user.getUsername() : "User";
        userId = (user != null) ? user.getId() : 0;

        // Custom panel to ensure the content stretches to the full width of the window,
        // which guarantees that "center" alignments are real screen centers.
        class ScrollablePanel extends JPanel implements Scrollable {
            public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
            public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 20; }
            public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 60; }
            public boolean getScrollableTracksViewportWidth() { return true; }
            public boolean getScrollableTracksViewportHeight() { return false; }
        }

        JPanel mainContent = new ScrollablePanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 50, 40, 50));

        JPanel welcomeRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        welcomeRow.setOpaque(false);
        welcomeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        welcomeRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JLabel welcomeLabel = new JLabel("Welcome Back, " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(StyleConfig.FONT_TITLE);
        welcomeLabel.setForeground(StyleConfig.TEXT_COLOR);
        welcomeRow.add(welcomeLabel);
        
        mainContent.add(welcomeRow);

        mainContent.add(Box.createVerticalStrut(20));

        // Use OverlayLayout so the stats are truly centered across the full width,
        // while the "+" button floats on top at the right edge.
        JPanel statsRowWrapper = new JPanel() {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false; // Required for overlapping components
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

        // Button layer: right-aligned, floating on top of the centered stats
        JPanel addBtnLayer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        addBtnLayer.setOpaque(false);
        addBtnLayer.setAlignmentX(0.5f);
        addBtnLayer.setAlignmentY(0.5f);
        addBtnLayer.add(addBtn);

        // Add layers: first added = on top
        statsRowWrapper.add(addBtnLayer);
        statsRowWrapper.add(statsContainer);
        mainContent.add(statsRowWrapper);
        mainContent.add(Box.createVerticalStrut(30));

        // Since mainContent is now FULL width, we must set recentCardsPanel and header to LEFT_ALIGNMENT,
        // and limit their size or put them in a dedicated left-aligned flow inside the box.
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

        recentCardsPanel = new JPanel();
        recentCardsPanel.setLayout(new BoxLayout(recentCardsPanel, BoxLayout.Y_AXIS));
        recentCardsPanel.setOpaque(false);
        recentCardsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        recentScroll = new JScrollPane(recentCardsPanel);
        recentScroll.setOpaque(false);
        recentScroll.getViewport().setOpaque(false);
        recentScroll.setBorder(null);
        // Disable horizontal scrollbar completely (as requested)
        recentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        recentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        // Vertical scrollbar invisible initially
        recentScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        recentScroll.getVerticalScrollBar().setUnitIncrement(16);
        
        // Allow JScrollPane to stretch exactly for 8 cards.
        recentScroll.setPreferredSize(new Dimension(1485, 540));
        recentScroll.setMaximumSize(new Dimension(1485, 540));

        // Set simple scroll mode to fix repaint smearing with transparent viewport
        recentScroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        // By default, disable native vertical mouse scrolling over the cards until they explicitly click the arrow
        recentScroll.setWheelScrollingEnabled(false);

        // Forward vertical mouse wheel events to parent ONLY IF we are not vertically scrolling ourselves
        recentScroll.addMouseWheelListener(e -> {
            if (recentScroll.getVerticalScrollBarPolicy() == JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED) {
                recentScroll.repaint();
                return; // Let JScrollPane handle the event natively
            }
            e.consume(); // Prevent native scrolling when disabled
            Container parent = recentScroll.getParent();
            while (parent != null && !(parent instanceof JScrollPane)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                parent.dispatchEvent(SwingUtilities.convertMouseEvent(recentScroll, e, parent));
            }
        });

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.add(recentScroll, BorderLayout.CENTER);

        rightArrow = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = 36;
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                
                UIUtils.drawVerticalArrowIcon(g2, cx - size/2, cy - size/2, size, size, getForeground(), true);
                g2.dispose();
            }
        };
        rightArrow.setForeground(StyleConfig.TEXT_SECONDARY);
        // Smaller physical hit-box for the arrow so it stays tightly centered
        rightArrow.setPreferredSize(new Dimension(100, 50));
        rightArrow.setHorizontalAlignment(SwingConstants.CENTER);
        rightArrow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightArrow.setToolTipText("See more");
        rightArrow.setVisible(false);
        rightArrow.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Height is already 540, simply enable scrolling to peek at further rows
                recentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                recentScroll.setWheelScrollingEnabled(true);
                
                rightArrow.setVisible(false); // Hide arrow once scrollbar appears
                
                recentActivityWrapper.revalidate();
                recentActivityWrapper.repaint();
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                rightArrow.setForeground(StyleConfig.PRIMARY_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                rightArrow.setForeground(StyleConfig.TEXT_SECONDARY);
            }
        });
        
        // Ensure contentWrapper is tightly wrapped for BorderLayout
        contentWrapper.setMaximumSize(new Dimension(1485, 600));

        JPanel arrowWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        arrowWrapper.setOpaque(false);
        arrowWrapper.add(rightArrow);

        JPanel alignmentWrapper = new JPanel(new BorderLayout());
        alignmentWrapper.setOpaque(false);
        alignmentWrapper.add(contentWrapper, BorderLayout.CENTER);
        // Add arrow directly beneath the scroll pane in the view wrapper rather than inside the scrolling canvas
        alignmentWrapper.add(arrowWrapper, BorderLayout.SOUTH);

        loadRecentCards();

        recentActivityWrapper.add(alignmentWrapper, BorderLayout.CENTER);
        recentActivityWrapper.setMaximumSize(new Dimension(1485, 600));
        mainContent.add(recentActivityWrapper);

        JScrollPane mainScroll = new JScrollPane(mainContent);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        mainScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(mainScroll, BorderLayout.CENTER);
    }

    private void loadRecentCards() {
        recentCardsPanel.removeAll();
        ReviewDAO reviewDAO = new ReviewDAO();
        MediaDAO mediaDAO = new MediaDAO();
        List<com.playtrack.model.Review> recentReviews = reviewDAO.getRecentReviews(userId, 50);
        List<MediaItem> recentItems = new ArrayList<>();
        for (com.playtrack.model.Review review : recentReviews) {
            MediaItem mediaItem = mediaDAO.getMediaById(review.getMediaId());
            if (mediaItem != null) {
                recentItems.add(mediaItem);
            }
        }

        if (rightArrow != null) {
            rightArrow.setVisible(recentItems.size() > 16);
        }

        JPanel currentRow = null;
        for (int i = 0; i < recentItems.size(); i++) {
            if (i % 8 == 0) {
                currentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
                currentRow.setOpaque(false);
                currentRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                // Extra padding mimicking the WrapLayout wrapping
                if (i == 0) {
                    currentRow.setBorder(BorderFactory.createEmptyBorder(0, -15, 0, 0));
                } else {
                    currentRow.setBorder(BorderFactory.createEmptyBorder(-20, -15, 0, 0));
                }
                recentCardsPanel.add(currentRow);
            }
            if (currentRow != null) {
                currentRow.add(new MediaCard(recentItems.get(i), false, false, this::refreshRecentActivity));
            }
        }

        if (recentScroll != null) {
            recentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            recentScroll.setWheelScrollingEnabled(false);
            // Disable horizontal scrolling as requested
            recentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            // Reset scroll position to top
            javax.swing.SwingUtilities.invokeLater(() -> {
                recentScroll.getViewport().setViewPosition(new Point(0, 0));
                
                if (rightArrow != null) {
                    rightArrow.setVisible(recentItems.size() > 16);
                }
            });
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