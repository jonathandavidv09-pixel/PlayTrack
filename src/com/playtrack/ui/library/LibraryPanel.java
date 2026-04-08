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
    private MediaService mediaService = new MediaService();
    private JPanel cardGrid;
    private String currentCategory = "All";
    private PlaceholderTextField searchField;
    private JPanel tabsPanel;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Subtle glowing orb top-left
        int orbSize = 350;
        g2.setPaint(new RadialGradientPaint(
            80f, 60f, orbSize / 2f,
            new float[]{0f, 0.4f, 1f},
            new Color[]{new Color(211, 64, 69, 20), new Color(211, 64, 69, 6), new Color(0, 0, 0, 0)}
        ));
        g2.fillOval(80 - orbSize / 2, 60 - orbSize / 2, orbSize, orbSize);

        // No top accent line


        g2.dispose();
    }

    public LibraryPanel() {
        setLayout(new BorderLayout());
        setBackground(StyleConfig.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 0));

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
        searchField.addActionListener(e -> refreshLibrary());
        filtersRow.add(searchField, BorderLayout.EAST);

        // Left spacer to perfectly balance BorderLayout.CENTER
        JPanel leftSpacer = new JPanel();
        leftSpacer.setOpaque(false);
        leftSpacer.setPreferredSize(new Dimension(280, 42));
        filtersRow.add(leftSpacer, BorderLayout.WEST);

        topSection.add(filtersRow, BorderLayout.SOUTH);
        add(topSection, BorderLayout.NORTH);

        // Card grid
        cardGrid = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        cardGrid.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        cardGrid.setOpaque(false);

        JScrollPane scroll = new JScrollPane(cardGrid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE); // Fix smearing
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
                if (currentlyActive) {
                    g2.setColor(StyleConfig.PRIMARY_COLOR);
                } else if (hovered) {
                    g2.setColor(StyleConfig.SURFACE_COLOR);
                } else {
                    g2.setColor(StyleConfig.CARD_BACKGROUND);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.dispose();
            }
        };
        tab.setOpaque(false);
        tab.setPreferredSize(new Dimension(100, 36));
        tab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tab.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); // Provide some spacing between tabs

        JLabel label = new JLabel(name, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(active ? Color.WHITE : StyleConfig.TEXT_SECONDARY);
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

    public void reset() {
        currentCategory = "All";
        if (searchField != null) searchField.setText("");
        refreshTabs();
        refreshLibrary();
    }

    public void refreshLibrary() {
        cardGrid.removeAll();
        cardGrid.setLayout(new BoxLayout(cardGrid, BoxLayout.Y_AXIS));

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

            JLabel emptyIcon = new JLabel("📭", SwingConstants.CENTER);
            emptyIcon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyContent.add(emptyIcon);
            emptyContent.add(Box.createRigidArea(new Dimension(0, 10)));
            JLabel emptyText = new JLabel("Your library is empty", SwingConstants.CENTER);
            emptyText.setFont(StyleConfig.FONT_SUBTITLE);
            emptyText.setForeground(StyleConfig.TEXT_SECONDARY);
            emptyText.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyContent.add(emptyText);
            emptyState.add(emptyContent);
            cardGrid.add(emptyState);
        }

        cardGrid.revalidate();
        cardGrid.repaint();
    }

    private void addCategorySection(String title, List<MediaItem> items) {
        if (items == null || items.isEmpty())
            return;

        JPanel section = new JPanel(new BorderLayout(0, 20));
        section.setOpaque(false);
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
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.PRIMARY_COLOR, getWidth(), 0, new Color(0, 0, 0, 0)));
                g2.fillRect(0, 0, getWidth(), 2);
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(divider);
        section.add(headerPanel, BorderLayout.NORTH);

        boolean isAllMode = "All".equals(currentCategory);
        boolean isHorizontalRow = !"Search Results".equals(title);
        
        JPanel cardsPanel;
        int requiredHeight = 280;
        
        if (isHorizontalRow) {
            cardsPanel = new JPanel();
            cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
            // Removed 15px side padding to shift cards further left
            cardsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            cardsPanel.setOpaque(false);
            
            JPanel topRowPanel = new JPanel();
            topRowPanel.setLayout(new BoxLayout(topRowPanel, BoxLayout.X_AXIS));
            topRowPanel.setOpaque(false);
            topRowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            if (isAllMode) {
                // "All" mode: 8 cards per row, max 2 rows (16 cards total)
                JPanel bottomRowPanel = new JPanel();
                bottomRowPanel.setLayout(new BoxLayout(bottomRowPanel, BoxLayout.X_AXIS));
                bottomRowPanel.setOpaque(false);
                bottomRowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                int maxCards = Math.min(items.size(), 16);
                for (int i = 0; i < maxCards; i++) {
                    boolean isTopRow = i < 8;
                    JPanel targetRow = isTopRow ? topRowPanel : bottomRowPanel;
                    targetRow.add(new MediaCard(items.get(i), false, true, this::refreshLibrary));
                    // Reduced strut from 25 to 20 to tighten grid matching HomePanel
                    targetRow.add(Box.createHorizontalStrut(20));
                }
                
                cardsPanel.add(topRowPanel);
                if (maxCards > 8) {
                    cardsPanel.add(Box.createVerticalStrut(20));
                    cardsPanel.add(bottomRowPanel);
                    requiredHeight = 520;
                }
            } else {
                // Category mode: 2 rows, 8 cards each
                JPanel bottomRowPanel = new JPanel();
                bottomRowPanel.setLayout(new BoxLayout(bottomRowPanel, BoxLayout.X_AXIS));
                bottomRowPanel.setOpaque(false);
                bottomRowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                for (int i = 0; i < items.size(); i++) {
                    int posInPage = i % 16;
                    boolean isTopRow = posInPage < 8;
                    
                    JPanel targetRow = isTopRow ? topRowPanel : bottomRowPanel;
                    targetRow.add(new MediaCard(items.get(i), false, true, this::refreshLibrary));
                    targetRow.add(Box.createHorizontalStrut(25));
                }
                
                cardsPanel.add(topRowPanel);
                if (items.size() > 8) {
                    cardsPanel.add(Box.createVerticalStrut(20));
                    cardsPanel.add(bottomRowPanel);
                    requiredHeight = 520;
                }
            }
        } else {
            cardsPanel = new JPanel();
            cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
            cardsPanel.setOpaque(false);
            
            JPanel currentRow = null;
            for (int i = 0; i < items.size(); i++) {
                if (i % 8 == 0) {
                    currentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
                    currentRow.setOpaque(false);
                    currentRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                    if (i == 0) {
                        currentRow.setBorder(BorderFactory.createEmptyBorder(0, -15, 0, 0));
                    } else {
                        currentRow.setBorder(BorderFactory.createEmptyBorder(-20, -15, 0, 0));
                    }
                    cardsPanel.add(currentRow);
                }
                if (currentRow != null) {
                    currentRow.add(new MediaCard(items.get(i), false, true, this::refreshLibrary));
                }
            }
        }

        // Wrap in a horizontal scrollpane
        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        // Restore horizontal scrollbar thickness to 8 so it is visible when activated on genre sections
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        // Reduce scroll pane fixed width requirement tightly to 1420 (saving ~65px)
        scrollPane.setPreferredSize(new Dimension(1420, requiredHeight));
        scrollPane.setMaximumSize(new Dimension(1420, requiredHeight));

        // Forward vertical mouse wheel events to parent (main page) to prevent blocking
        scrollPane.addMouseWheelListener(e -> {
            Container parent = scrollPane.getParent();
            while (parent != null && !(parent instanceof JScrollPane)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                // Ensure native scroll repaints correctly to avoid glitchy rendering
                parent.dispatchEvent(SwingUtilities.convertMouseEvent(scrollPane, e, parent));
                parent.repaint();
            }
        });

        // Removed hgap, we will use symmetric margins around the arrow instead
        JPanel contentWrapper = new JPanel(new BorderLayout(0, 0));
        contentWrapper.setOpaque(false);

        // Arrow Button for navigation or horizontal scrolling
        if (isHorizontalRow) {
            class ScrollArrow extends JLabel {
                private final boolean isRight;

                public ScrollArrow(boolean isRight) {
                    super(" ");
                    this.isRight = isRight;
                    setForeground(Color.WHITE);
                    setPreferredSize(new Dimension(45, 60));
                    setMaximumSize(new Dimension(45, 60));
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                    setToolTipText("All".equals(currentCategory) ? "Go to " + title : "See more");
                    
                    addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            if ("All".equals(currentCategory)) {
                                currentCategory = title;
                                refreshTabs();
                                refreshLibrary();
                            } else {
                                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                                updateVisibility(false);
                                scrollPane.revalidate();
                                scrollPane.repaint();
                                JScrollBar hb = scrollPane.getHorizontalScrollBar();
                                hb.setValue(hb.getValue() + 555);
                            }
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
            arrowContainer.setOpaque(false);
            // Add thick right padding (80px) to shift arrow out of the corner, keeping a smaller left gap
            arrowContainer.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 80));
            arrowContainer.add(Box.createVerticalGlue());
            arrowContainer.add(rightArrow);
            arrowContainer.add(Box.createVerticalGlue());

            // Reduce content wrapper size tightly so arrow sits leftward securely inside standard windows
            scrollPane.setPreferredSize(new Dimension(1420, requiredHeight));
            scrollPane.setMaximumSize(new Dimension(1420, requiredHeight));

            contentWrapper.add(scrollPane, BorderLayout.CENTER);
            contentWrapper.add(arrowContainer, BorderLayout.EAST);
            
            // Show arrow always in 'All' mode as a link, otherwise hide it if cards < 16
            if ("All".equals(currentCategory)) {
                rightArrow.updateVisibility(true);
            } else {
                rightArrow.updateVisibility(items.size() > 16);
            }

            // Setup scroll listener to toggle arrow visibility dynamically (if needed)
            scrollPane.getHorizontalScrollBar().addAdjustmentListener(e -> {
                if (!"All".equals(currentCategory)) {
                    int val = e.getValue();
                    int max = scrollPane.getHorizontalScrollBar().getMaximum() - scrollPane.getHorizontalScrollBar().getVisibleAmount();
                    rightArrow.updateVisibility(val < max && items.size() > 16);
                }
            });

            // Tightly constrained container to prevent clipping the arrow off-screen, while accounting for the extra shifted right margin
            contentWrapper.setPreferredSize(new Dimension(1550, requiredHeight)); 
            contentWrapper.setMaximumSize(new Dimension(1550, requiredHeight));
        } else {
            contentWrapper.add(scrollPane, BorderLayout.CENTER);
            contentWrapper.setMaximumSize(new Dimension(1385, 99999));
        }

        section.add(contentWrapper, BorderLayout.WEST);
        cardGrid.add(section);
    }
}
