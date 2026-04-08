package com.playtrack.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class StatsCard extends ShadowPanel {
    private Color accentColor;
    private String title;
    private int count;

    public StatsCard(String title, int count, Color accentColor) {
        super(20, 6);
        this.title = title;
        this.count = count;
        this.accentColor = accentColor;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 160));

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;

        // Icon
        JLabel iconLabel = new JLabel(getIcon(title));
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 8, 0);
        content.add(iconLabel, gbc);

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(StyleConfig.FONT_SMALL);
        titleLabel.setForeground(StyleConfig.TEXT_SECONDARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        content.add(titleLabel, gbc);

        // Count
        JLabel countLabel = new JLabel(String.valueOf(count));
        countLabel.setFont(StyleConfig.FONT_LARGE_NUMBER);
        countLabel.setForeground(accentColor);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(countLabel, gbc);

        add(content, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw accent bar at top
        g2.setColor(accentColor);
        g2.fill(new RoundRectangle2D.Float(6, 6, getWidth() - 12, 4, 4, 4));
        g2.dispose();
    }

    private String getIcon(String title) {
        if (title.contains("Film")) return "🎬";
        if (title.contains("Game")) return "🎮";
        if (title.contains("Book")) return "📚";
        return "📊";
    }
}
