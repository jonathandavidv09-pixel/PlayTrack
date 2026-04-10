package com.playtrack.ui.library;

import com.playtrack.model.MediaItem;
import com.playtrack.service.MediaService;
import com.playtrack.ui.components.*;
import com.playtrack.util.SessionManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class LibraryPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int CARD_WIDTH = 160;
    private static final int CARD_HEIGHT = 240;
    private static final int CARD_GAP = 20;
    private MediaService mediaService = new MediaService();
    private JPanel cardGrid;
    private JScrollPane libraryScroll;
    private int sectionRowIndex = 0;
    private String currentCategory = "All";
    private PlaceholderTextField searchField;
    private JPanel tabsPanel;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        UIUtils.paintFadedAuthBackground(g2, getWidth(), getHeight());

        // Subtle glowing orb top-left
        int orbSize = 350;
        g2.setPaint(new RadialGradientPaint(
            80f, 60f, orbSize / 2f,
            new float[]{0f, 0.4f, 1f},
            new Color[]{StyleConfig.PANEL_GLOW_PRIMARY, new Color(StyleConfig.PALETTE_RED.getRed(), StyleConfig.PALETTE_RED.getGreen(), StyleConfig.PALETTE_RED.getBlue(), 8), new Color(0, 0, 0, 0)}
        ));
        g2.fillOval(80 - orbSize / 2, 60 - orbSize / 2, orbSize, orbSize);

        g2.dispose();
    }

    public LibraryPanel() {
        setLayout(new BorderLayout());
        setBackground(StyleConfig.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Top section
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 50));

        // Title
        JLabel pageTitle = new JLabel("Your Library");
        pageTitle.setFont(StyleConfig.FONT_TITLE);
        pageTitle.setForeground(StyleConfig.TEXT_COLOR);
        topSection.add(pageTitle, BorderLayout.NORTH);

        // Filters row
        JPanel filtersRow = new JPanel(new BorderLayout());
        filtersRow.setOpaque(false);
        filtersRow.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Category tabs
        tabsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        tabsPanel.setOpaque(false);
        refreshTabs();
        filtersRow.add(tabsPanel, BorderLayout.CENTER);

        // Search
        searchField = new PlaceholderTextField("Search your library...", "SEARCH");
        searchField.setPreferredSize(new Dimension(280, 42));
        searchField.addActionListener(e -> {
            refreshLibrary();
            scrollToTop();
        });
        filtersRow.add(searchField, BorderLayout.EAST);

        // Left spacer to perfectly balance BorderLayout.CENTER
        JPanel leftSpacer = new JPanel();
        leftSpacer.setOpaque(false);
        leftSpacer.setPreferredSize(new Dimension(280, 42));
        filtersRow.add(leftSpacer, BorderLayout.WEST);

        topSection.add(filtersRow, BorderLayout.SOUTH);
        add(topSection, BorderLayout.NORTH);

        // Card grid
        class ScrollableCardGrid extends JPanel implements Scrollable {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Repaint a stable full background every frame to prevent scroll ghosting/clipped duplicates.
                g2.setColor(StyleConfig.BACKGROUND_COLOR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                Point p = SwingUtilities.convertPoint(this, 0, 0, LibraryPanel.this);
                g2.translate(-p.x, -p.y);
                UIUtils.paintFadedAuthBackground(g2, LibraryPanel.this.getWidth(), LibraryPanel.this.getHeight());
                g2.dispose();
            }

            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }

            @Override
            public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
                return 20;
            }

            @Override
            public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
                return 80;
            }

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }

            @Override
            public boolean getScrollableTracksViewportHeight() {
                return false;
            }
        }

        cardGrid = new ScrollableCardGrid();
        cardGrid.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        cardGrid.setOpaque(true);
        cardGrid.setBackground(StyleConfig.BACKGROUND_COLOR);

        JScrollPane scroll = new JScrollPane();
        this.libraryScroll = scroll;
        scroll.setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setViewportView(cardGrid);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(StyleConfig.BACKGROUND_COLOR);
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        add(scroll, BorderLayout.CENTER);

        refreshLibrary();
    }

    private JPanel createTab(String name, boolean active) {
        JPanel tab = new JPanel(new BorderLayout()) {
            boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        currentCategory = name;
                        refreshTabs();
                        refreshLibrary();
                        scrollToTop();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean currentlyActive = currentCategory.equals(name);
                RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                if (currentlyActive) {
                    g2.setPaint(new GradientPaint(0, 0, StyleConfig.PRIMARY_COLOR, getWidth(), 0, StyleConfig.SECONDARY_COLOR));
                } else if (hovered) {
                    g2.setColor(StyleConfig.SURFACE_SOFT);
                } else {
                    g2.setColor(StyleConfig.SURFACE_ELEVATED);
                }
                g2.fill(shape);

                g2.setColor(currentlyActive ? new Color(255, 255, 255, 70) : StyleConfig.SURFACE_STROKE);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(shape);
                g2.dispose();
            }
        };
        tab.setOpaque(false);
        tab.setPreferredSize(new Dimension(100, 36));
        tab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tab.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); // Provide some spacing between tabs

        JLabel label = new JLabel(name, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(active ? StyleConfig.TEXT_COLOR : StyleConfig.TEXT_SECONDARY);
        JPanel tabInner = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        tabInner.setOpaque(false);
        tabInner.add(label);
        
        tab.add(tabInner, BorderLayout.CENTER);

        return tab;
    }

    private void refreshTabs() {
        tabsPanel.removeAll();
        tabsPanel.add(createTab("All", currentCategory.equals("All")));
        tabsPanel.add(createTab("Films", currentCategory.equals("Films")));
        tabsPanel.add(createTab("Games", currentCategory.equals("Games")));
        tabsPanel.add(createTab("Books", currentCategory.equals("Books")));
        tabsPanel.revalidate();
        tabsPanel.repaint();
    }

    private JLabel createSectionJumpArrow(String targetCategory) {
        JLabel arrow = new JLabel() {
            private boolean hovered = false;

            {
                setPreferredSize(new Dimension(32, 32));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        currentCategory = targetCategory;
                        if (searchField != null) {
                            searchField.setText("");
                        }
                        refreshTabs();
                        refreshLibrary();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color iconColor = hovered ? StyleConfig.PRIMARY_COLOR : StyleConfig.TEXT_SECONDARY;
                UIUtils.drawArrowIcon(g2, 0, 0, getWidth(), getHeight(), iconColor, true);
                g2.dispose();
            }
        };
        arrow.setToolTipText("Go to " + targetCategory);
        return arrow;
    }

    public void reset() {
        currentCategory = "All";
        if (searchField != null) searchField.setText("");
        refreshTabs();
        refreshLibrary();
        scrollToTop();
    }

    public void setCategory(String category) {
        this.currentCategory = category;
        refreshTabs();
        refreshLibrary();
        scrollToTop();
    }

    public void refreshLibrary() {
        cardGrid.removeAll();
        cardGrid.setLayout(new GridBagLayout());
        sectionRowIndex = 0;

        if (SessionManager.getCurrentUser() == null)
            return;

        List<MediaItem> allItems = null;
        String query = searchField.getText();
        if (query != null && !query.isEmpty()) {
            allItems = mediaService.searchMedia(SessionManager.getCurrentUser().getId(), query);
            addCategorySection("Search Results", allItems);
        } else {
            if ("All".equals(currentCategory)) {
                addCategorySection("Films",
                        mediaService.getMediaByUser(SessionManager.getCurrentUser().getId(), "Films"));
                addCategorySection("Games",
                        mediaService.getMediaByUser(SessionManager.getCurrentUser().getId(), "Games"));
                addCategorySection("Books",
                        mediaService.getMediaByUser(SessionManager.getCurrentUser().getId(), "Books"));
            } else {
                List<MediaItem> catItems = mediaService.getMediaByUser(SessionManager.getCurrentUser().getId(), currentCategory);
                if (catItems != null && !catItems.isEmpty()) {
                    java.util.Map<String, List<MediaItem>> itemsByGenre = new java.util.LinkedHashMap<>();
                    for (MediaItem item : catItems) {
                        String genre = item.getGenre();
                        if (genre == null || genre.isEmpty()) {
                            genre = "Various";
                        }
                        itemsByGenre.computeIfAbsent(genre, k -> new java.util.ArrayList<>()).add(item);
                    }
                    for (java.util.Map.Entry<String, List<MediaItem>> entry : itemsByGenre.entrySet()) {
                        addCategorySection(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        if (cardGrid.getComponentCount() == 0 || (allItems != null && allItems.isEmpty())) {
            JPanel emptyState = new JPanel(new GridBagLayout());
            emptyState.setOpaque(false);
            emptyState.setPreferredSize(new Dimension(800, 300));
            JPanel emptyContent = new JPanel();
            emptyContent.setLayout(new BoxLayout(emptyContent, BoxLayout.Y_AXIS));
            emptyContent.setOpaque(false);

            JLabel emptyIcon = new JLabel("\uD83D\uDCED", SwingConstants.CENTER);
            emptyIcon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
            emptyIcon.setForeground(StyleConfig.TEXT_LIGHT);
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyContent.add(emptyIcon);
            emptyContent.add(Box.createRigidArea(new Dimension(0, 10)));
            JLabel emptyText = new JLabel("Your library is empty", SwingConstants.CENTER);
            emptyText.setFont(StyleConfig.FONT_SUBTITLE);
            emptyText.setForeground(StyleConfig.TEXT_SECONDARY);
            emptyText.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyContent.add(emptyText);
            emptyState.add(emptyContent);
            addSectionToGrid(emptyState);
        }

        GridBagConstraints spacer = new GridBagConstraints();
        spacer.gridx = 0;
        spacer.gridy = sectionRowIndex;
        spacer.weightx = 1.0;
        spacer.weighty = 1.0;
        spacer.fill = GridBagConstraints.BOTH;
        cardGrid.add(Box.createVerticalGlue(), spacer);

        cardGrid.revalidate();
        cardGrid.repaint();
    }

    private void addCategorySection(String title, List<MediaItem> items) {
        if (items == null || items.isEmpty())
            return;

        boolean isCategorySection = "Films".equals(title) || "Games".equals(title) || "Books".equals(title);
        boolean showSectionArrow = "All".equals(currentCategory) && isCategorySection;
        final int itemCount = items.size();

        JPanel section = new JPanel(new BorderLayout(0, 20));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 55));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(StyleConfig.TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(titleLabel);

        headerPanel.add(Box.createVerticalStrut(8));

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
        headerPanel.add(divider);
        section.add(headerPanel, BorderLayout.NORTH);

        // Build cards with deterministic rows (same look, no wrap clipping artifacts).
        int columns = calculateCardColumns(showSectionArrow);
        JPanel cardsHost = new JPanel();
        cardsHost.setOpaque(false);
        cardsHost.setLayout(new BoxLayout(cardsHost, BoxLayout.Y_AXIS));
        cardsHost.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cardsHost.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int start = 0; start < itemCount; start += columns) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, CARD_GAP, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            int end = Math.min(itemCount, start + columns);
            for (int i = start; i < end; i++) {
                row.add(new MediaCard(items.get(i), false, true, this::refreshLibrary));
            }

            cardsHost.add(row);
            if (end < itemCount) {
                cardsHost.add(Box.createVerticalStrut(CARD_GAP));
            }
        }

        JPanel centerRow = new JPanel(new BorderLayout(8, 0));
        centerRow.setOpaque(false);
        centerRow.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel lanePanel = new JPanel(new BorderLayout(8, 0));
        lanePanel.setOpaque(false);
        lanePanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        lanePanel.add(cardsHost, BorderLayout.CENTER);

        if (showSectionArrow) {
            String targetCategory = title;
            JPanel sectionArrowHost = new JPanel(new GridBagLayout());
            sectionArrowHost.setOpaque(false);
            sectionArrowHost.setPreferredSize(new Dimension(36, 0));
            sectionArrowHost.add(createSectionJumpArrow(targetCategory));
            lanePanel.add(sectionArrowHost, BorderLayout.EAST);
        }

        centerRow.add(lanePanel, BorderLayout.CENTER);

        section.add(centerRow, BorderLayout.CENTER);

        addSectionToGrid(section);
    }

    private void addSectionToGrid(Component component) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = sectionRowIndex++;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        cardGrid.add(component, gbc);
    }

    private int calculateCardColumns(boolean showSectionArrow) {
        int viewportWidth = 0;
        if (libraryScroll != null && libraryScroll.getViewport() != null) {
            viewportWidth = libraryScroll.getViewport().getWidth();
        }
        if (viewportWidth <= 0) {
            viewportWidth = cardGrid != null ? cardGrid.getWidth() : 0;
        }
        if (viewportWidth <= 0) {
            viewportWidth = getWidth();
        }
        if (viewportWidth <= 0) {
            viewportWidth = 1200;
        }

        int reserved = showSectionArrow ? 44 : 0; // section arrow host + gap
        int available = Math.max(CARD_WIDTH, viewportWidth - reserved - CARD_GAP);
        return Math.max(1, (available + CARD_GAP) / (CARD_WIDTH + CARD_GAP));
    }

    private void scrollToTop() {
        if (libraryScroll == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> libraryScroll.getVerticalScrollBar().setValue(0));
    }
}
