package com.playtrack.ui.summary;

import com.playtrack.service.SummaryService;
import com.playtrack.ui.components.*;
import com.playtrack.util.SessionManager;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Summary dashboard for user activity and ratings.
public class SummaryPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private SummaryService summaryService = new SummaryService();
    private static final int GENRE_DONUT_RING = 36;
    private static final int GENRE_LEGEND_WIDTH = 150;
    private static final int GENRE_LEGEND_ROWS = 5;
    private static final int ACTIVITY_WEEKS = 5;
    private static final String[] DAY_LABELS = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
    private static final int TOP_RATED_POSTER_W = 42;
    private static final int TOP_RATED_POSTER_H = 62;
    private static final int TOP_RATED_PREVIEW_LIMIT = 6;
    private static final int TOP_RATED_PREVIEW_COLUMNS = 3;
    private static final int TOP_RATED_PREVIEW_ROWS = TOP_RATED_PREVIEW_LIMIT / TOP_RATED_PREVIEW_COLUMNS;
    private static final Color[] GENRE_PIE_COLORS = {
            StyleConfig.PRIMARY_COLOR,
            StyleConfig.SECONDARY_COLOR,
            new Color(86, 170, 150),
            new Color(120, 104, 164),
            new Color(130, 148, 184),
            new Color(169, 138, 102)
    };
    private static final Color[] HEAT_LEVEL_COLORS = {
            new Color(90, 32, 60),
            new Color(118, 43, 70),
            new Color(145, 54, 80),
            new Color(172, 68, 87),
            new Color(198, 92, 95),
            new Color(221, 122, 103),
            new Color(242, 161, 112)
    };
    private static final Map<String, Image> POSTER_THUMB_CACHE =
            Collections.synchronizedMap(new LinkedHashMap<String, Image>(96, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
                    return size() > 180;
                }
            });
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

            int orb = 360;
            bg.setPaint(new RadialGradientPaint(
                    w - 120f, 90f, orb / 2f,
                    new float[] { 0f, 0.45f, 1f },
                    new Color[] {
                            StyleConfig.PANEL_GLOW_SECONDARY,
                            new Color(StyleConfig.PALETTE_PEACH.getRed(), StyleConfig.PALETTE_PEACH.getGreen(),
                                    StyleConfig.PALETTE_PEACH.getBlue(), 8),
                            new Color(0, 0, 0, 0)
                    }));
            bg.fillOval(w - 120 - orb / 2, 90 - orb / 2, orb, orb);
            bg.dispose();
        }
        g2.drawImage(cachedBackground, 0, 0, null);
    }

    public SummaryPanel() {
        setLayout(new BorderLayout());
        setBackground(StyleConfig.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                0, StyleConfig.PAGE_PAD_X, StyleConfig.PAGE_PAD_BOTTOM, StyleConfig.PAGE_PAD_X));

        refreshSummary();
    }

    public void refreshSummary() {
        removeAll();

        if (SessionManager.getCurrentUser() == null)
            return;

        int userId = SessionManager.getCurrentUser().getId();
        Map<String, Integer> counts = summaryService.getCategoryCounts(userId);

        class ScrollableSummaryContent extends JPanel implements Scrollable {
            private static final long serialVersionUID = 1L;

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
                return 110;
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

        JPanel mainContent = new ScrollableSummaryContent();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(24, 0, 20, 0));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel title = new JLabel("Your Activity");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(StyleConfig.TEXT_COLOR);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Summary of your media activity");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(StyleConfig.TEXT_SECONDARY);
        subtitle.setBorder(BorderFactory.createEmptyBorder(8, 0, 20, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComponent divider = createPageDivider();

        headerPanel.add(title);
        headerPanel.add(subtitle);
        headerPanel.add(divider);
        mainContent.add(headerPanel);

        
        JPanel row1 = new JPanel(new GridLayout(1, 3, 30, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        row1.add(createCountCard("Films Watched", counts.get("Films"), 1));
        row1.add(createCountCard("Games Played", counts.get("Games"), 2));
        row1.add(createCountCard("Books Read", counts.get("Books"), 3));
        mainContent.add(row1);
        mainContent.add(Box.createVerticalStrut(30));

        
        JPanel row2 = new JPanel(new GridLayout(1, 3, 30, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        row2.add(createGenreDonutCard("Top Film Genres", summaryService.getGenreDistribution(userId, "Films", 4, true)));
        row2.add(createGenreDonutCard("Top Game Genres", summaryService.getGenreDistribution(userId, "Games", 4, true)));
        row2.add(createGenreDonutCard("Top Book Genres", summaryService.getGenreDistribution(userId, "Books", 4, true)));
        mainContent.add(row2);
        mainContent.add(Box.createVerticalStrut(30));

        
        JPanel row3 = new JPanel(new GridLayout(1, 2, 30, 0));
        row3.setOpaque(false);
        row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3.setPreferredSize(new Dimension(1, 320));
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        row3.add(createTopRatedCard(userId));
        row3.add(createMediaActivityByDayCard(userId));

        JPanel row3Wrapper = new JPanel(new BorderLayout());
        row3Wrapper.setOpaque(false);
        row3Wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3Wrapper.setPreferredSize(new Dimension(1, 320));
        row3Wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        row3Wrapper.add(row3, BorderLayout.NORTH);

        mainContent.add(row3Wrapper);

        JScrollPane scroll = new JScrollPane(mainContent);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setWheelScrollingEnabled(true);
        scroll.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createBaseCard() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = StyleConfig.PANEL_RADIUS;
                g2.setColor(StyleConfig.withAlpha(Color.BLACK, 30));
                g2.fill(new RoundRectangle2D.Float(0, 5, getWidth(), Math.max(0, getHeight() - 7), arc, arc));

                g2.setPaint(new GradientPaint(0, 0, StyleConfig.withAlpha(StyleConfig.SURFACE_ELEVATED, 226),
                        0, getHeight(), StyleConfig.withAlpha(StyleConfig.SURFACE_COLOR, 236)));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));

                g2.setPaint(new GradientPaint(0, 0, StyleConfig.withAlpha(Color.WHITE, 18), 0, getHeight() / 2,
                        StyleConfig.withAlpha(Color.WHITE, 0)));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));

                g2.setColor(StyleConfig.SURFACE_STROKE);
                g2.setStroke(new BasicStroke(1.1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, arc, arc));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private JComponent createPageDivider() {
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.SECONDARY_COLOR,
                        getWidth(), 0, StyleConfig.withAlpha(StyleConfig.SECONDARY_COLOR, 0)));
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(1, 1));
        divider.setMinimumSize(new Dimension(0, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        return divider;
    }

    private JComponent createCardDivider() {
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int y = getHeight() / 2;
                g2.setPaint(new GradientPaint(0, y, StyleConfig.withAlpha(StyleConfig.DIVIDER_COLOR, 150),
                        getWidth(), y, StyleConfig.withAlpha(StyleConfig.DIVIDER_COLOR, 18)));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(0, y, getWidth(), y);
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(1, 18));
        divider.setMinimumSize(new Dimension(0, 18));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        return divider;
    }

    private JPanel createCountCard(String title, int count, int iconType) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = StyleConfig.PANEL_RADIUS;
                g2.setColor(StyleConfig.withAlpha(Color.BLACK, 30));
                g2.fill(new RoundRectangle2D.Float(0, 8, getWidth(), getHeight() - 6, arc, arc));
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.withAlpha(StyleConfig.SURFACE_ELEVATED, 230),
                        0, getHeight(), StyleConfig.withAlpha(StyleConfig.SURFACE_COLOR, 238)));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.withAlpha(Color.WHITE, 22),
                        0, getHeight() / 2, StyleConfig.withAlpha(Color.WHITE, 0)));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
                g2.setColor(StyleConfig.SURFACE_STROKE);
                g2.setStroke(new BasicStroke(1.1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, arc, arc));
                int y = getHeight() - 14;
                g2.setPaint(new GradientPaint(18, y, StyleConfig.withAlpha(StyleConfig.SECONDARY_COLOR, 190),
                        getWidth() - 18, y, StyleConfig.withAlpha(StyleConfig.PRIMARY_COLOR, 28)));
                g2.fillRoundRect(18, y, Math.max(0, getWidth() - 36), 1, 1, 1);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        card.setPreferredSize(new Dimension(350, 110));

        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setColor(StyleConfig.withAlpha(StyleConfig.PRIMARY_COLOR, 18));
                g2.fill(new RoundRectangle2D.Float(3, 3, getWidth() - 6, getHeight() - 6, 16, 16));
                g2.setColor(StyleConfig.withAlpha(Color.WHITE, 28));
                g2.draw(new RoundRectangle2D.Float(3.5f, 3.5f, getWidth() - 7, getHeight() - 7, 16, 16));
                String cat = iconType == 1 ? "Films" : (iconType == 2 ? "Games" : "Books");
                UIUtils.drawCategoryIcon(g2, cat, cx, cy, StyleConfig.PRIMARY_COLOR, 3f, 1.45);
                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(66, 66));
        card.add(iconPanel, BorderLayout.WEST);

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(StyleConfig.TEXT_COLOR);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        textStack.add(Box.createVerticalGlue());
        textStack.add(lblTitle);
        textStack.add(Box.createVerticalStrut(6));

        JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        countRow.setOpaque(false);
        countRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCount = new JLabel(String.valueOf(count));
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 34));
        lblCount.setForeground(StyleConfig.TEXT_COLOR);
        countRow.add(lblCount);

        JLabel loggedLabel = new JLabel("Logged");
        loggedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loggedLabel.setForeground(StyleConfig.TEXT_SECONDARY);
        loggedLabel.setBorder(BorderFactory.createEmptyBorder(9, 0, 0, 0));
        countRow.add(loggedLabel);

        textStack.add(countRow);
        textStack.add(Box.createVerticalGlue());
        card.add(textStack, BorderLayout.CENTER);

        return card;
    }

    private JPanel createGenreDonutCard(String title, Map<String, Integer> genres) {
        JPanel card = createBaseCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        card.setPreferredSize(new Dimension(350, 250));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(StyleConfig.TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        card.add(titleLabel, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(12, 0));
        body.setOpaque(false);

        JPanel donut = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (genres.isEmpty()) {
                    g2.setColor(StyleConfig.TEXT_LIGHT);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                    g2.drawString("No activity yet", 14, 24);
                    g2.dispose();
                    return;
                }

                int total = genres.values().stream().mapToInt(Integer::intValue).sum();
                if (total <= 0) {
                    g2.dispose();
                    return;
                }

                int size = Math.min(getWidth(), getHeight()) - 24;
                size = Math.max(120, size);
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                g2.setColor(StyleConfig.withAlpha(Color.WHITE, 12));
                g2.fillOval(x, y, size, size);

                int startAngle = 90;
                int consumed = 0;
                int idx = 0;
                for (Map.Entry<String, Integer> entry : genres.entrySet()) {
                    int angle = (idx == genres.size() - 1)
                            ? (360 - consumed)
                            : Math.round(360f * entry.getValue() / (float) total);
                    g2.setColor(GENRE_PIE_COLORS[idx % GENRE_PIE_COLORS.length]);
                    g2.fillArc(x, y, size, size, startAngle - consumed, -angle);
                    consumed += angle;
                    idx++;
                }

                int hole = Math.max(56, size - GENRE_DONUT_RING * 2);
                int hx = x + (size - hole) / 2;
                int hy = y + (size - hole) / 2;

                g2.setColor(StyleConfig.withAlpha(StyleConfig.BACKGROUND_LIGHT, 232));
                g2.fillOval(hx, hy, hole, hole);
                g2.setColor(StyleConfig.withAlpha(Color.WHITE, 34));
                g2.drawOval(hx, hy, hole, hole);

                String totalText = String.valueOf(total);
                g2.setColor(StyleConfig.TEXT_COLOR);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 34));
                FontMetrics fmCount = g2.getFontMetrics();
                int tx = hx + (hole - fmCount.stringWidth(totalText)) / 2;
                int ty = hy + hole / 2 + fmCount.getAscent() / 3;
                g2.drawString(totalText, tx, ty);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(StyleConfig.TEXT_LIGHT);
                String subtitle = "Logged";
                FontMetrics fmSub = g2.getFontMetrics();
                int sx = hx + (hole - fmSub.stringWidth(subtitle)) / 2;
                g2.drawString(subtitle, sx, ty + 16);

                g2.dispose();
            }
        };
        donut.setOpaque(false);
        donut.setPreferredSize(new Dimension(180, 180));
        body.add(donut, BorderLayout.CENTER);

        JPanel legend = new JPanel();
        legend.setOpaque(false);
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        legend.setPreferredSize(new Dimension(GENRE_LEGEND_WIDTH, 0));
        legend.setMinimumSize(new Dimension(GENRE_LEGEND_WIDTH, 0));
        legend.setMaximumSize(new Dimension(GENRE_LEGEND_WIDTH, Integer.MAX_VALUE));
        int colorIndex = 0;
        for (Map.Entry<String, Integer> entry : genres.entrySet()) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            JLabel dot = new JLabel("\u25A0");
            dot.setForeground(GENRE_PIE_COLORS[colorIndex % GENRE_PIE_COLORS.length]);
            dot.setFont(new Font("Dialog", Font.PLAIN, 13));
            JLabel text = new JLabel(toEllipsis(entry.getKey(), 14) + " (" + entry.getValue() + ")");
            text.setForeground(StyleConfig.TEXT_SECONDARY);
            text.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            row.add(dot);
            row.add(text);
            legend.add(row);
            colorIndex++;
        }
        for (int i = colorIndex; i < GENRE_LEGEND_ROWS; i++) {
            JPanel spacerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            spacerRow.setOpaque(false);
            spacerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            JLabel spacer = new JLabel(" ");
            spacer.setFont(new Font("Dialog", Font.PLAIN, 13));
            spacer.setForeground(new Color(0, 0, 0, 0));
            spacerRow.add(spacer);
            legend.add(spacerRow);
        }
        legend.add(Box.createVerticalGlue());
        body.add(legend, BorderLayout.EAST);

        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTopRatedCard(int userId) {
        JPanel card = createBaseCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        card.setPreferredSize(new Dimension(550, 320));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout(12, 0));
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Top 6 Highest Rated");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(StyleConfig.TEXT_COLOR);
        titleRow.add(title, BorderLayout.WEST);
        head.add(titleRow, BorderLayout.NORTH);

        head.add(createCardDivider(), BorderLayout.SOUTH);
        card.add(head, BorderLayout.NORTH);

        List<SummaryService.RatedMedia> topMedia = summaryService.getTopRatedMedia(userId, TOP_RATED_PREVIEW_LIMIT);
        if (topMedia.isEmpty()) {
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setOpaque(false);
            JLabel empty = new JLabel("No rated media found");
            empty.setForeground(StyleConfig.TEXT_LIGHT);
            list.add(empty);
            list.add(Box.createVerticalGlue());
            card.add(list, BorderLayout.CENTER);
        } else {
            JPanel mediaRow = new JPanel(new GridLayout(TOP_RATED_PREVIEW_ROWS, TOP_RATED_PREVIEW_COLUMNS, 10, 8));
            mediaRow.setOpaque(false);

            for (SummaryService.RatedMedia item : topMedia) {
                mediaRow.add(createTopRatedMiniCard(item));
            }
            for (int i = topMedia.size(); i < TOP_RATED_PREVIEW_LIMIT; i++) {
                mediaRow.add(createTopRatedEmptyCard());
            }

            JPanel body = new JPanel(new BorderLayout());
            body.setOpaque(false);
            body.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            body.add(mediaRow, BorderLayout.NORTH);
            card.add(body, BorderLayout.CENTER);
        }

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        footer.add(createViewAllButton(userId));
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createTopRatedMiniCard(SummaryService.RatedMedia item) {
        JPanel tile = createTopRatedTileShell();
        tile.setLayout(new BorderLayout(8, 0));
        tile.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));

        tile.add(createTopRatedPoster(item.media), BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel mediaTitle = new JLabel(toEllipsis(item.media.getTitle(), 18));
        mediaTitle.setToolTipText(item.media.getTitle());
        mediaTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        mediaTitle.setForeground(StyleConfig.TEXT_COLOR);
        mediaTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(mediaTitle);
        info.add(Box.createVerticalStrut(4));

        JPanel starRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        starRow.setOpaque(false);
        starRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        StarRating stars = new StarRating(roundToHalf(item.rating), false, 11);
        starRow.add(stars);
        JLabel ratingText = new JLabel(formatRating(item.rating));
        ratingText.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ratingText.setForeground(StyleConfig.TEXT_SECONDARY);
        starRow.add(ratingText);
        info.add(starRow);
        info.add(Box.createVerticalGlue());

        tile.add(info, BorderLayout.CENTER);
        return tile;
    }

    private JPanel createTopRatedEmptyCard() {
        JPanel tile = createTopRatedTileShell();
        tile.setLayout(new GridBagLayout());
        JLabel text = new JLabel("No Entry");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        text.setForeground(StyleConfig.TEXT_LIGHT);
        tile.add(text);
        return tile;
    }

    private JPanel createTopRatedTileShell() {
        JPanel tile = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 18;
                g2.setColor(StyleConfig.withAlpha(Color.BLACK, 30));
                g2.fill(new RoundRectangle2D.Float(0, 2, getWidth(), Math.max(0, getHeight() - 4), arc, arc));
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.withAlpha(StyleConfig.SURFACE_ELEVATED, 212), 0,
                        getHeight(), StyleConfig.withAlpha(StyleConfig.SURFACE_COLOR, 220)));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
                g2.setColor(StyleConfig.withAlpha(Color.WHITE, 34));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, arc, arc));
                g2.dispose();
            }
        };
        tile.setOpaque(false);
        tile.setPreferredSize(new Dimension(160, 76));
        return tile;
    }

    private JComponent createTopRatedPoster(com.playtrack.model.MediaItem media) {
        JPanel poster = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                int w = getWidth();
                int h = getHeight();
                int arc = 12;

                Shape clip = new RoundRectangle2D.Float(0, 0, w, h, arc, arc);
                g2.setClip(clip);

                Image img = loadPosterThumbnail(media.getImagePath(), w, h);
                if (img != null) {
                    g2.drawImage(img, 0, 0, w, h, null);
                } else {
                    g2.setPaint(new GradientPaint(0, 0, StyleConfig.withAlpha(StyleConfig.SURFACE_ELEVATED, 210), 0, h,
                            StyleConfig.withAlpha(StyleConfig.SURFACE_COLOR, 228)));
                    g2.fillRect(0, 0, w, h);
                    UIUtils.drawCategoryIcon(g2, media.getCategory(), w / 2, h / 2, 34, StyleConfig.TEXT_LIGHT);
                }
                g2.setClip(null);
                g2.setColor(StyleConfig.withAlpha(Color.WHITE, 56));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));
                g2.dispose();
            }
        };
        poster.setOpaque(false);
        poster.setPreferredSize(new Dimension(TOP_RATED_POSTER_W, TOP_RATED_POSTER_H));
        poster.setMinimumSize(new Dimension(TOP_RATED_POSTER_W, TOP_RATED_POSTER_H));
        poster.setMaximumSize(new Dimension(TOP_RATED_POSTER_W, TOP_RATED_POSTER_H));
        return poster;
    }

    private Image loadPosterThumbnail(String imagePath, int width, int height) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        String key = imagePath + "|" + width + "x" + height;
        Image cached = POSTER_THUMB_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                return null;
            }
            BufferedImage raw = javax.imageio.ImageIO.read(file);
            if (raw == null) {
                return null;
            }
            Image scaled = raw.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            POSTER_THUMB_CACHE.put(key, scaled);
            return scaled;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String formatRating(double value) {
        return String.format("%.1f", roundToHalf(value)).replace(".0", "");
    }

    private double roundToHalf(double value) {
        double clamped = Math.max(0.0, Math.min(5.0, value));
        return Math.round(clamped * 2.0) / 2.0;
    }

    private String toEllipsis(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, Math.max(1, maxLen - 1)) + "\u2026";
    }

    // Start: view-all ratings button function.
    private JPanel createViewAllButton(int userId) {
        JPanel expandBtn = new JPanel() {
            private boolean hovered = false;
            {
                setOpaque(false);
                setPreferredSize(new Dimension(132, 34));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hovered = false;
                        repaint();
                    }

                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        // Button action: open the full rated-media dialog.
                        showAllRatingsDialog(userId);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (hovered) {
                    g2.setPaint(new GradientPaint(0, 0, StyleConfig.PRIMARY_COLOR, getWidth(), 0, StyleConfig.SECONDARY_COLOR));
                } else {
                    g2.setColor(new Color(255, 255, 255, 13));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 36, 36);
                g2.setColor(hovered ? new Color(255, 255, 255, 70) : StyleConfig.SURFACE_STROKE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 36, 36);
                // View All arrow
                g2.setColor(hovered ? Color.WHITE : StyleConfig.TEXT_COLOR);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String txt = "View All";
                int txtW = fm.stringWidth(txt);
                int startX = (getWidth() - txtW - 14) / 2;
                g2.drawString(txt, startX, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);

                int cx = startX + txtW + 10;
                int cy = getHeight() / 2;
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawPolyline(new int[] { cx - 5, cx, cx + 5 }, new int[] { cy - 3, cy + 3, cy - 3 }, 3);
                g2.dispose();
            }
        };
        return expandBtn;
    }
    // End: view-all ratings button function.

    // Start: all-ratings dialog button/list flow function.
    private void showAllRatingsDialog(int userId) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "All Rated Media",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 700);
        dialog.setMinimumSize(new Dimension(560, 620));
        dialog.setLocationRelativeTo(this);

        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.withAlpha(StyleConfig.BACKGROUND_LIGHT, 255),
                        0, getHeight(), StyleConfig.withAlpha(StyleConfig.BACKGROUND_COLOR, 255)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setPaint(new RadialGradientPaint(
                        getWidth() - 80f, 50f, 220f,
                        new float[] { 0f, 0.48f, 1f },
                        new Color[] {
                                StyleConfig.withAlpha(StyleConfig.PRIMARY_COLOR, 34),
                                StyleConfig.withAlpha(StyleConfig.PRIMARY_COLOR, 8),
                                StyleConfig.withAlpha(StyleConfig.PRIMARY_COLOR, 0)
                        }));
                g2.fillOval(getWidth() - 300, -170, 440, 440);
                g2.dispose();
            }
        };
        container.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(34, 40, 18, 40));

        List<SummaryService.RatedMedia> allMedia = summaryService.getTopRatedMedia(userId, Integer.MAX_VALUE);

        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Your Rated Library");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(StyleConfig.TEXT_COLOR);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel(allMedia.size() + " rated media");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(StyleConfig.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleStack.add(title);
        titleStack.add(Box.createVerticalStrut(7));
        titleStack.add(subtitle);
        header.add(titleStack, BorderLayout.WEST);

        JPanel headerLine = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.PRIMARY_COLOR,
                        getWidth(), 0, StyleConfig.withAlpha(StyleConfig.PRIMARY_COLOR, 0)));
                g2.fillRect(0, getHeight() / 2, getWidth(), 1);
                g2.dispose();
            }
        };
        headerLine.setOpaque(false);
        headerLine.setPreferredSize(new Dimension(100, 10));
        header.add(headerLine, BorderLayout.SOUTH);

        container.add(header, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.setBorder(BorderFactory.createEmptyBorder(8, 36, 38, 36));

        int rowIndex = 0;
        for (SummaryService.RatedMedia item : allMedia) {
            list.add(createRatedLibraryRow(item, rowIndex++));
            list.add(Box.createVerticalStrut(8));
        }
        list.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(list);
        scroll.setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        SmoothScrollSupport.installVertical(scroll);
        scroll.getViewport().setOpaque(false);
        container.add(scroll, BorderLayout.CENTER);

        dialog.add(container);
        dialog.setVisible(true);
    }
    // End: all-ratings dialog button/list flow function.

    private JPanel createRatedLibraryRow(SummaryService.RatedMedia item, int rowIndex) {
        JPanel row = new JPanel(new BorderLayout(18, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 14;
                Color top = rowIndex % 2 == 0
                        ? StyleConfig.withAlpha(StyleConfig.SURFACE_ELEVATED, 166)
                        : StyleConfig.withAlpha(StyleConfig.SURFACE_COLOR, 134);
                Color bottom = rowIndex % 2 == 0
                        ? StyleConfig.withAlpha(StyleConfig.SURFACE_COLOR, 174)
                        : StyleConfig.withAlpha(StyleConfig.SURFACE_ELEVATED, 124);
                g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() - 1, arc, arc));
                g2.setColor(StyleConfig.withAlpha(Color.WHITE, 22));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 2, arc, arc));
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(13, 18, 13, 18));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        JLabel mediaTitle = new JLabel(toEllipsis(item.media.getTitle(), 38));
        mediaTitle.setToolTipText(item.media.getTitle());
        mediaTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        mediaTitle.setForeground(StyleConfig.TEXT_COLOR);
        mediaTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel category = new JLabel(item.media.getCategory() == null ? "Media" : item.media.getCategory());
        category.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        category.setForeground(StyleConfig.TEXT_LIGHT);
        category.setAlignmentX(Component.LEFT_ALIGNMENT);
        textStack.add(mediaTitle);
        textStack.add(Box.createVerticalStrut(4));
        textStack.add(category);
        row.add(textStack, BorderLayout.CENTER);

        JPanel ratingHost = new JPanel(new GridBagLayout());
        ratingHost.setOpaque(false);
        ratingHost.setPreferredSize(new Dimension(150, 34));
        StarRating stars = new StarRating(roundToHalf(item.rating), false, 19);
        ratingHost.add(stars);
        row.add(ratingHost, BorderLayout.EAST);

        return row;
    }

    private JPanel createMediaActivityByDayCard(int userId) {
        JPanel card = createBaseCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        card.setPreferredSize(new Dimension(550, 320));

        int[][] activity = summaryService.getActivityHeatmapByDay(userId, ACTIVITY_WEEKS);
        int tempMaxValue = 0;
        for (int r = 0; r < activity.length; r++) {
            for (int c = 0; c < activity[r].length; c++) {
                tempMaxValue = Math.max(tempMaxValue, activity[r][c]);
            }
        }
        final int maxValue = tempMaxValue;

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel title = new JLabel("Media Activity by Day");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(StyleConfig.TEXT_COLOR);
        head.add(title, BorderLayout.WEST);
        head.add(createActivityHeatLegend(maxValue), BorderLayout.EAST);

        head.add(createCardDivider(), BorderLayout.SOUTH);
        card.add(head, BorderLayout.NORTH);

        JPanel heatBody = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (maxValue == 0) {
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                    g2.setColor(StyleConfig.TEXT_LIGHT);
                    g2.drawString("No media activity recorded yet.", 8, 24);
                    g2.dispose();
                    return;
                }

                int rows = activity.length;
                int cols = DAY_LABELS.length;
                int leftPad = 82;
                int rightPad = 8;
                int topPad = 30;
                int bottomPad = 8;
                int hGap = 7;
                int vGap = 7;

                int gridW = getWidth() - leftPad - rightPad;
                int gridH = getHeight() - topPad - bottomPad;
                int cellW = Math.max(14, (gridW - (cols - 1) * hGap) / cols);
                int cellH = Math.max(14, (gridH - (rows - 1) * vGap) / rows);
                int usedW = cols * cellW + (cols - 1) * hGap;
                int usedH = rows * cellH + (rows - 1) * vGap;

                int startX = leftPad + Math.max(0, (gridW - usedW) / 2);
                int startY = topPad + Math.max(0, (gridH - usedH) / 2);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.setColor(StyleConfig.TEXT_SECONDARY);
                for (int col = 0; col < cols; col++) {
                    int x = startX + col * (cellW + hGap);
                    String label = DAY_LABELS[col];
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = x + (cellW - fm.stringWidth(label)) / 2;
                    g2.drawString(label, tx, startY - 8);
                }

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                for (int row = 0; row < rows; row++) {
                    int y = startY + row * (cellH + vGap);
                    String rowLabel = formatWeekRowLabel(rows - 1 - row);
                    FontMetrics fm = g2.getFontMetrics();
                    int ty = y + (cellH + fm.getAscent() - fm.getDescent()) / 2;
                    g2.setColor(StyleConfig.TEXT_LIGHT);
                    g2.drawString(rowLabel, Math.max(6, leftPad - fm.stringWidth(rowLabel) - 8), ty);

                    for (int col = 0; col < cols; col++) {
                        int x = startX + col * (cellW + hGap);
                        int value = activity[row][col];
                        g2.setColor(heatColorForValue(value, maxValue));
                        g2.fillRoundRect(x, y, cellW, cellH, 6, 6);
                        g2.setColor(StyleConfig.withAlpha(Color.WHITE, value > 0 ? 38 : 16));
                        g2.drawRoundRect(x, y, cellW, cellH, 6, 6);
                    }
                }

                g2.dispose();
            }
        };
        heatBody.setOpaque(false);
        card.add(heatBody, BorderLayout.CENTER);
        return card;
    }

    private String formatWeekRowLabel(int weeksAgo) {
        if (weeksAgo <= 0) {
            return "This Week";
        }
        if (weeksAgo == 1) {
            return "Last Week";
        }
        return weeksAgo + "w Ago";
    }

    private JComponent createActivityHeatLegend(int maxValue) {
        JPanel legend = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (maxValue <= 0) {
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    g2.setColor(StyleConfig.TEXT_LIGHT);
                    g2.drawString("No data", 4, 15);
                    g2.dispose();
                    return;
                }

                int steps = HEAT_LEVEL_COLORS.length;
                int barW = 18;
                int gap = 4;
                int x = 0;
                for (int i = 0; i < steps; i++) {
                    g2.setColor(StyleConfig.withAlpha(HEAT_LEVEL_COLORS[i], 220));
                    g2.fillRoundRect(x, 0, barW, 10, 4, 4);
                    g2.setColor(StyleConfig.withAlpha(Color.WHITE, 42));
                    g2.drawRoundRect(x, 0, barW, 10, 4, 4);
                    x += barW + gap;
                }

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(StyleConfig.TEXT_SECONDARY);
                int cell = barW + gap;
                for (int i = 0; i < steps; i++) {
                    String txt = String.valueOf(i + 1);
                    FontMetrics fm = g2.getFontMetrics();
                    int cx = i * cell + (barW - fm.stringWidth(txt)) / 2;
                    g2.drawString(txt, cx, 24);
                }
                g2.dispose();
            }
        };
        legend.setOpaque(false);
        legend.setPreferredSize(new Dimension(162, 26));
        legend.setMinimumSize(new Dimension(162, 26));
        legend.setMaximumSize(new Dimension(162, 26));
        return legend;
    }

    private Color heatColorForValue(int value, int maxValue) {
        if (value <= 0 || maxValue <= 0) {
            return StyleConfig.withAlpha(Color.WHITE, 12);
        }
        double ratio = Math.max(0.0, Math.min(1.0, (double) value / maxValue));
        int idx = (int) Math.ceil(ratio * HEAT_LEVEL_COLORS.length) - 1;
        idx = Math.max(0, Math.min(HEAT_LEVEL_COLORS.length - 1, idx));
        int alpha = Math.min(236, 120 + idx * 18);
        return StyleConfig.withAlpha(HEAT_LEVEL_COLORS[idx], alpha);
    }

    private JPanel createAverageRatingCard(double avg) {
        JPanel card = createBaseCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        card.setPreferredSize(new Dimension(550, 360));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Average Rating");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(StyleConfig.TEXT_COLOR);
        head.add(title, BorderLayout.NORTH);

        head.add(createCardDivider(), BorderLayout.CENTER);
        card.add(head, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;

        JLabel avgLabel = new JLabel(String.format("%.1f", avg).replace(".0", ""));
        avgLabel.setFont(new Font("Segoe UI", Font.BOLD, 72));
        avgLabel.setForeground(StyleConfig.PRIMARY_COLOR);
        body.add(avgLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(15, 0, 0, 0);
        StarRating sr = new StarRating(roundToHalf(avg), false);
        body.add(sr, gbc);

        card.add(body, BorderLayout.CENTER);
        return card;
    }
}
