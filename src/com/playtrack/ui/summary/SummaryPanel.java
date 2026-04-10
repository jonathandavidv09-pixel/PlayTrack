package com.playtrack.ui.summary;

import com.playtrack.service.SummaryService;
import com.playtrack.ui.components.*;
import com.playtrack.util.SessionManager;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;

public class SummaryPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private SummaryService summaryService = new SummaryService();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        UIUtils.paintFadedAuthBackground(g2, getWidth(), getHeight());

        int orb = 360;
        g2.setPaint(new RadialGradientPaint(
                getWidth() - 120f, 90f, orb / 2f,
                new float[] { 0f, 0.45f, 1f },
                new Color[] { StyleConfig.PANEL_GLOW_SECONDARY, new Color(StyleConfig.PALETTE_PEACH.getRed(), StyleConfig.PALETTE_PEACH.getGreen(), StyleConfig.PALETTE_PEACH.getBlue(), 8), new Color(0, 0, 0, 0) }));
        g2.fillOval(getWidth() - 120 - orb / 2, 90 - orb / 2, orb, orb);
        g2.dispose();
    }

    public SummaryPanel() {
        setLayout(new BorderLayout());
        setBackground(StyleConfig.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(0, 50, 40, 50));

        refreshSummary();
    }

    public void refreshSummary() {
        removeAll();

        if (SessionManager.getCurrentUser() == null)
            return;

        int userId = SessionManager.getCurrentUser().getId();
        Map<String, Integer> counts = summaryService.getCategoryCounts(userId);
        double avg = summaryService.getAverageRating(userId);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 30, 0));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Your Activity");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(StyleConfig.TEXT_COLOR);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Summary of your media activity");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(StyleConfig.TEXT_SECONDARY);
        subtitle.setBorder(BorderFactory.createEmptyBorder(8, 0, 20, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel divider = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.SECONDARY_COLOR, getWidth(), 0, new Color(0, 0, 0, 0)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setMaximumSize(new Dimension(800, 2));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(title);
        headerPanel.add(subtitle);
        headerPanel.add(divider);
        mainContent.add(headerPanel);

        // Row 1: Total Counts
        JPanel row1 = new JPanel(new GridLayout(1, 3, 30, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(1400, 110));
        row1.add(createCountCard("Films Watched", counts.get("Films"), 1));
        row1.add(createCountCard("Games Played", counts.get("Games"), 2));
        row1.add(createCountCard("Books Read", counts.get("Books"), 3));
        mainContent.add(row1);
        mainContent.add(Box.createVerticalStrut(30));

        // Row 2: Genres
        JPanel row2 = new JPanel(new GridLayout(1, 3, 30, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(1400, 230));
        row2.add(createGenreBarsCard("Top Film Genres", summaryService.getTopGenres(userId, "Films")));
        row2.add(createGenreBarsCard("Top Game Genres", summaryService.getTopGenres(userId, "Games")));
        row2.add(createGenreBarsCard("Top Book Genres", summaryService.getTopGenres(userId, "Books")));
        mainContent.add(row2);
        mainContent.add(Box.createVerticalStrut(30));

        // Row 3: Bottom Area
        JPanel row3 = new JPanel(new GridLayout(1, 2, 30, 0));
        row3.setOpaque(false);
        row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3.setMaximumSize(new Dimension(1400, 360));
        row3.add(createTopRatedCard(userId));
        row3.add(createAverageRatingCard(avg));

        JPanel row3Wrapper = new JPanel(new BorderLayout());
        row3Wrapper.setOpaque(false);
        row3Wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3Wrapper.add(row3, BorderLayout.NORTH);

        mainContent.add(row3Wrapper);
        mainContent.add(Box.createVerticalStrut(50));
        mainContent.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(mainContent);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
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

                for (int i = 3; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 10 * i));
                    g2.fill(new RoundRectangle2D.Float(1 - i, 1 - i, getWidth() - 2 + i * 2, getHeight() - 2 + i * 2, 24 + i, 24 + i));
                }

                // Card body
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.SURFACE_ELEVATED, 0, getHeight(), StyleConfig.SURFACE_COLOR));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));

                // Soft top gloss
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 14), 0, getHeight() / 2,
                        new Color(255, 255, 255, 0)));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));

                // Border
                g2.setColor(StyleConfig.SURFACE_STROKE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 24, 24));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private JPanel createCountCard(String title, int count, int iconType) {
        JPanel card = createBaseCard();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(350, 110));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 20, 0, 25);
        gbc.gridheight = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JPanel iconPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                String cat = iconType == 1 ? "Films" : (iconType == 2 ? "Games" : "Books");
                UIUtils.drawCategoryIcon(g2, cat, cx, cy, StyleConfig.PRIMARY_COLOR, 3f, 1.6);
                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(60, 60));
        card.add(iconPanel, gbc);

        gbc.gridheight = 1;
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 2, 0);
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(StyleConfig.TEXT_SECONDARY);
        card.add(lblTitle, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblCount = new JLabel(String.valueOf(count));
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblCount.setForeground(StyleConfig.TEXT_COLOR);
        card.add(lblCount, gbc);

        return card;
    }

    private JPanel createGenreBarsCard(String title, Map<String, Integer> genres) {
        JPanel card = createBaseCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        card.setPreferredSize(new Dimension(350, 230));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(StyleConfig.TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        card.add(titleLabel, BorderLayout.NORTH);

        JPanel bars = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (genres.isEmpty()) {
                    g2.setColor(StyleConfig.TEXT_LIGHT);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                    g2.drawString("No activity yet", 0, 20);
                    g2.dispose();
                    return;
                }

                int y = 0;
                int max = genres.values().stream().max(Integer::compare).orElse(1);
                int barMaxWidth = getWidth() - 100;

                for (Map.Entry<String, Integer> entry : genres.entrySet()) {
                    g2.setColor(StyleConfig.TEXT_SECONDARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    String name = entry.getKey();
                    if (name.length() > 10)
                        name = name.substring(0, 8) + "..";
                    g2.drawString(name, 0, y + 14);

                    g2.setColor(new Color(255, 255, 255, 12)); // Track background
                    g2.fillRoundRect(85, y + 3, barMaxWidth, 12, 12, 12);

                    g2.setPaint(new GradientPaint(85, y + 3, StyleConfig.PRIMARY_COLOR, 85 + barMaxWidth, y + 3, StyleConfig.SECONDARY_COLOR));
                    int width = (int) ((double) barMaxWidth * entry.getValue() / max);
                    g2.fillRoundRect(85, y + 3, Math.max(width, 12), 12, 12, 12);

                    y += 35;
                }
                g2.dispose();
            }
        };
        bars.setOpaque(false);
        card.add(bars, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTopRatedCard(int userId) {
        JPanel card = createBaseCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        card.setPreferredSize(new Dimension(550, 360));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Top Rated");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(StyleConfig.TEXT_COLOR);
        head.add(title, BorderLayout.NORTH);

        JPanel div = new JPanel() {
            protected void paintComponent(Graphics g) {
                g.setColor(StyleConfig.DIVIDER_COLOR);
                g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            }
        };
        div.setOpaque(false);
        div.setPreferredSize(new Dimension(100, 25));
        head.add(div, BorderLayout.CENTER);
        card.add(head, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);

        List<SummaryService.RatedMedia> topMedia = summaryService.getTopRatedMedia(userId, 5);
        if (topMedia.isEmpty()) {
            JLabel empty = new JLabel("No rated media found");
            empty.setForeground(StyleConfig.TEXT_LIGHT);
            list.add(empty);
        } else {
            for (SummaryService.RatedMedia item : topMedia) {
                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);
                row.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

                JLabel mediaTitle = new JLabel(item.media.getTitle());
                mediaTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
                mediaTitle.setForeground(StyleConfig.TEXT_COLOR);
                row.add(mediaTitle, BorderLayout.CENTER);

                StarRating stars = new StarRating(item.rating, false);
                row.add(stars, BorderLayout.EAST);

                list.add(row);
            }
            list.add(Box.createVerticalGlue());
        }
        card.add(list, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        footer.setOpaque(false);

        JPanel expandBtn = new JPanel() {
            private boolean hovered = false;
            {
                setOpaque(false);
                setPreferredSize(new Dimension(140, 36));
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
        footer.add(expandBtn);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private void showAllRatingsDialog(int userId) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "All Rated Media",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(this);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(StyleConfig.BACKGROUND_COLOR);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));
        JLabel title = new JLabel("Your Rated Library");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(StyleConfig.TEXT_COLOR);
        header.add(title, BorderLayout.WEST);

        JPanel headerLine = new JPanel() {
            protected void paintComponent(Graphics g) {
                g.setColor(StyleConfig.PRIMARY_COLOR);
                g.fillRect(0, 0, 60, 3); // Cool accent line
            }
        };
        headerLine.setOpaque(false);
        headerLine.setPreferredSize(new Dimension(100, 10));
        header.add(headerLine, BorderLayout.SOUTH);

        container.add(header, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(StyleConfig.BACKGROUND_COLOR);
        list.setBorder(BorderFactory.createEmptyBorder(10, 40, 40, 40));

        List<SummaryService.RatedMedia> allMedia = summaryService.getTopRatedMedia(userId, 1000);
        for (SummaryService.RatedMedia item : allMedia) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 10)),
                    BorderFactory.createEmptyBorder(18, 0, 18, 0)));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

            JLabel mediaTitle = new JLabel(item.media.getTitle());
            mediaTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
            mediaTitle.setForeground(StyleConfig.TEXT_COLOR);
            row.add(mediaTitle, BorderLayout.CENTER);

            StarRating stars = new StarRating(item.rating, false);
            row.add(stars, BorderLayout.EAST);

            list.add(row);
        }
        list.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scroll.getViewport().setBackground(StyleConfig.BACKGROUND_COLOR);
        container.add(scroll, BorderLayout.CENTER);

        dialog.add(container);
        dialog.setVisible(true);
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

        JPanel div = new JPanel() {
            protected void paintComponent(Graphics g) {
                g.setColor(StyleConfig.DIVIDER_COLOR);
                g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            }
        };
        div.setOpaque(false);
        div.setPreferredSize(new Dimension(100, 25));
        head.add(div, BorderLayout.CENTER);
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
        StarRating sr = new StarRating((int) Math.round(avg), false);
        body.add(sr, gbc);

        card.add(body, BorderLayout.CENTER);
        return card;
    }
}
