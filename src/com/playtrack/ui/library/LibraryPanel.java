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

    public void setCategory(String category) {
        this.currentCategory = category;
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

            JLabel emptyIcon = new JLabel("\uD83D\uDCED", SwingConstants.CENTER);
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

        final int MAX_VISIBLE_CARDS = 16; // 8 cards per row x 2 rows
        boolean hasOverflow = items.size() > MAX_VISIBLE_CARDS;
        // Card height = 240, vgap = 20, so 2 rows = 240 + 20 + 240 = 500
        final int TWO_ROW_HEIGHT = 500;

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

        // Build cards panel with ALL cards
        JPanel cardsPanel = new JPanel(new WrapLayout(WrapLayout.LEFT, 20, 20));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        for (int i = 0; i < items.size(); i++) {
            cardsPanel.add(new MediaCard(items.get(i), false, true, this::refreshLibrary));
        }

        // Wrap cards in a clipping container that limits to 2 rows initially
        JPanel cardClipWrapper = new JPanel(new BorderLayout());
        cardClipWrapper.setOpaque(false);
        cardClipWrapper.add(cardsPanel, BorderLayout.CENTER);

        if (hasOverflow) {
            cardClipWrapper.setPreferredSize(new Dimension(0, TWO_ROW_HEIGHT));
            cardClipWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, TWO_ROW_HEIGHT));
        }

        section.add(cardClipWrapper, BorderLayout.CENTER);

        // Down-arrow icon to expand/collapse when there are more than 2 rows
        if (hasOverflow) {
            final boolean[] expanded = {false};

            JLabel arrowIcon = new JLabel() {
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
            arrowIcon.setForeground(StyleConfig.TEXT_SECONDARY);
            arrowIcon.setPreferredSize(new Dimension(100, 40));
            arrowIcon.setHorizontalAlignment(SwingConstants.CENTER);
            arrowIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
            arrowIcon.setToolTipText("Show more");

            arrowIcon.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    expanded[0] = !expanded[0];
                    if (expanded[0]) {
                        // Remove height constraint - show all cards
                        cardClipWrapper.setPreferredSize(null);
                        cardClipWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
                        arrowIcon.setToolTipText("Show less");
                    } else {
                        // Collapse back to 2 rows
                        cardClipWrapper.setPreferredSize(new Dimension(0, TWO_ROW_HEIGHT));
                        cardClipWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, TWO_ROW_HEIGHT));
                        arrowIcon.setToolTipText("Show more");
                    }
                    arrowIcon.repaint();
                    section.revalidate();
                    section.repaint();
                    // Revalidate up the hierarchy so scroll pane adjusts
                    Container parent = section.getParent();
                    while (parent != null) {
                        parent.revalidate();
                        parent.repaint();
                        parent = parent.getParent();
                    }
                }
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    arrowIcon.setForeground(StyleConfig.PRIMARY_COLOR);
                }
                public void mouseExited(java.awt.event.MouseEvent e) {
                    arrowIcon.setForeground(StyleConfig.TEXT_SECONDARY);
                }
            });

            JPanel arrowWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            arrowWrapper.setOpaque(false);
            arrowWrapper.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            arrowWrapper.add(arrowIcon);
            section.add(arrowWrapper, BorderLayout.SOUTH);
        }

        cardGrid.add(section);
    }
}
