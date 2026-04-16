package com.playtrack.ui.library;

import com.playtrack.model.MediaItem;
import com.playtrack.service.MediaService;
import com.playtrack.ui.components.*;
import com.playtrack.util.SessionManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
// Main panel for the library section.
public class LibraryPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int CARD_WIDTH = 160;
    private static final int CARD_HEIGHT = 240;
    private static final int CARD_GAP = 20;
    private static final int MAX_CATEGORY_ROWS = 2;
    private MediaService mediaService = new MediaService();
    private JPanel cardGrid;
    private JScrollPane libraryScroll;
    private int sectionRowIndex = 0;
    private String currentCategory = "All";
    private PlaceholderTextField searchField;
    private JPanel tabsPanel;
    // Constructor.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        UIUtils.paintFadedAuthBackground(g2, getWidth(), getHeight());

        // Decorative glowing orb in the background.
        int orbSize = 350;
        g2.setPaint(new RadialGradientPaint(
            80f, 60f, orbSize / 2f,
            new float[]{0f, 0.4f, 1f},
            new Color[]{StyleConfig.PANEL_GLOW_PRIMARY, new Color(StyleConfig.PALETTE_RED.getRed(), StyleConfig.PALETTE_RED.getGreen(), StyleConfig.PALETTE_RED.getBlue(), 8), new Color(0, 0, 0, 0)}
        ));
        g2.fillOval(80 - orbSize / 2, 60 - orbSize / 2, orbSize, orbSize);

        g2.dispose();
    }
    // Helper method to load the empty library icon from resources.
    public LibraryPanel() {
        setLayout(new BorderLayout());
        setBackground(StyleConfig.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                StyleConfig.PAGE_PAD_TOP, StyleConfig.PAGE_PAD_X, StyleConfig.PAGE_PAD_BOTTOM, StyleConfig.PAGE_PAD_X));

        // Top section with page title and search bar.
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        // Page title label.
        JLabel pageTitle = new JLabel("Your Library");
        pageTitle.setFont(StyleConfig.FONT_TITLE);
        pageTitle.setForeground(StyleConfig.TEXT_COLOR);
        topSection.add(pageTitle, BorderLayout.NORTH);

        // Filters row with category tabs and search field.
        JPanel filtersRow = new JPanel(new BorderLayout());
        filtersRow.setOpaque(false);
        filtersRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Category tabs panel.
        tabsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        tabsPanel.setOpaque(false);
        refreshTabs();
        filtersRow.add(tabsPanel, BorderLayout.CENTER);

        // Search field for filtering media items.
        searchField = new PlaceholderTextField("Search your library...", "SEARCH");
        searchField.setPreferredSize(new Dimension(280, 42));
        searchField.addActionListener(e -> {
            refreshLibrary();
            scrollToTop();
        });
        filtersRow.add(searchField, BorderLayout.EAST);

        // Left spacer to push the search field to the right.
        JPanel leftSpacer = new JPanel();
        leftSpacer.setOpaque(false);
        leftSpacer.setPreferredSize(new Dimension(280, 42));
        filtersRow.add(leftSpacer, BorderLayout.WEST);

        topSection.add(filtersRow, BorderLayout.SOUTH);
        add(topSection, BorderLayout.NORTH);

        // Main content area with scrollable card grid.
        class ScrollableCardGrid extends JPanel implements Scrollable {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Paint a full, stable background so fast scrolling never leaves ghost trails.
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
        // Card grid panel for displaying media items.
        cardGrid = new ScrollableCardGrid();
        cardGrid.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        cardGrid.setOpaque(true);
        cardGrid.setBackground(StyleConfig.BACKGROUND_COLOR);
        // JScrollPane for the card grid.
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
        // Full repaint on scroll prevents background striping artifacts.
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshLibrary();
            }
        });

        add(scroll, BorderLayout.CENTER);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshLibrary();
            }
        });

        refreshLibrary();
    }
    // Helper method to create a category tab.
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
        tab.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); 

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

            JLabel emptyIcon = new JLabel(loadEmptyLibraryIcon(72), SwingConstants.CENTER);
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
        boolean reserveTwoRows = isCategorySection || !"All".equals(currentCategory);
        final int itemCount = items.size();

        JPanel section = new JPanel(new BorderLayout(0, 20));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 24));

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

        
        JPanel centerRow = new JPanel(new BorderLayout(8, 0));
        centerRow.setOpaque(false);
        centerRow.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel lanePanel = new JPanel(new BorderLayout(8, 0));
        lanePanel.setOpaque(false);
        lanePanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        int columns = calculateCardColumns(showSectionArrow);
        JPanel cardsHost = new JPanel();
        cardsHost.setOpaque(false);
        cardsHost.setLayout(new BoxLayout(cardsHost, BoxLayout.Y_AXIS));
        cardsHost.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cardsHost.setAlignmentX(Component.LEFT_ALIGNMENT);

        int visibleCount = itemCount;
        if (isCategorySection && "All".equals(currentCategory)) {
            visibleCount = Math.min(itemCount, columns * MAX_CATEGORY_ROWS);
        }

        for (int start = 0; start < visibleCount; start += columns) {
            JPanel row = new JPanel();
            row.setOpaque(false);
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            int end = Math.min(visibleCount, start + columns);
            for (int i = start; i < end; i++) {
                row.add(new MediaCard(items.get(i), false, true, this::refreshLibrary));
                if (i < end - 1) {
                    row.add(Box.createHorizontalStrut(CARD_GAP));
                }
            }

            cardsHost.add(row);
            if (end < visibleCount) {
                cardsHost.add(Box.createVerticalStrut(CARD_GAP));
            }
        }

        if (reserveTwoRows) {
            int shownRows = Math.max(1, (visibleCount + columns - 1) / columns);
            int missingRows = Math.max(0, MAX_CATEGORY_ROWS - shownRows);
            if (missingRows > 0) {
                int reservedHeight = missingRows * (CARD_HEIGHT + CARD_GAP);
                cardsHost.add(Box.createVerticalStrut(reservedHeight));
            }
        }

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

        int centerRowHorizontalPadding = 24; // 12 left + 12 right
        int laneHorizontalPadding = 28; // 14 left + 14 right
        int arrowReserve = showSectionArrow ? 44 : 0; // arrow host + spacing
        int safety = 16; // keep a visual buffer so cards never clip at edge
        int available = Math.max(
                CARD_WIDTH,
                viewportWidth - centerRowHorizontalPadding - laneHorizontalPadding - arrowReserve - safety
        );
        int maxCols = Math.max(1, (available + CARD_GAP) / (CARD_WIDTH + CARD_GAP));
        for (int cols = maxCols; cols >= 1; cols--) {
            int needed = (cols * CARD_WIDTH) + ((cols - 1) * CARD_GAP);
            if (needed <= available) {
                return cols;
            }
        }
        return 1;
    }

    private void scrollToTop() {
        if (libraryScroll == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> libraryScroll.getVerticalScrollBar().setValue(0));
    }

    private ImageIcon loadEmptyLibraryIcon(int size) {
        String[] candidates = {
                "resources/icons/library_empty.png",
                "resources/icons/empty_library.png"
        };
        for (String path : candidates) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                if (is == null) continue;
                BufferedImage image = ImageIO.read(is);
                if (image == null) continue;
                Image scaled = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            } catch (Exception ignored) {
            }
        }

        // Fallback so empty state still looks good if custom PNG is missing.
        BufferedImage fallback = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = fallback.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        UIUtils.drawCategoryIcon(g2, "Books", size / 2, size / 2, size - 16, StyleConfig.TEXT_LIGHT);
        g2.dispose();
        return new ImageIcon(fallback);
    }
}
