package com.playtrack.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ShadowPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private int shadowSize = 5;
    private int radius = 15;

    public ShadowPanel(int radius, int shadowSize) {
        this.radius = radius;
        this.shadowSize = shadowSize;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw subtle shadow
        for (int i = 0; i < shadowSize; i++) {
            int alpha = (shadowSize - i) * 3;
            g2.setColor(new Color(0, 0, 0, Math.min(alpha, 255)));
            g2.fill(new RoundRectangle2D.Float(i, i + 2, getWidth() - i * 2, getHeight() - i * 2, radius, radius));
        }

        // Draw card background
        g2.setColor(StyleConfig.CARD_BACKGROUND);
        g2.fill(new RoundRectangle2D.Float(shadowSize, shadowSize, getWidth() - shadowSize * 2, getHeight() - shadowSize * 2, radius, radius));

        // Subtle border
        g2.setColor(new Color(255, 255, 255, 8));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(shadowSize, shadowSize, getWidth() - shadowSize * 2 - 1, getHeight() - shadowSize * 2 - 1, radius, radius));

        g2.dispose();
    }
}
