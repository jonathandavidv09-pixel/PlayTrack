package com.playtrack.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class RoundedButton extends JButton {
    private static final long serialVersionUID = 1L;
    private Color backgroundColor;
    private Color hoverColor;
    private Color pressedColor;
    private int radius;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private boolean isGradient = false;
    private Color gradientEnd;

    // Start: reusable rounded button setup function.
    public RoundedButton(String text, Color backgroundColor, int radius) {
        super(text);
        // Base reusable button style used across auth/forms/dialog actions.
        this.backgroundColor = backgroundColor;
        this.hoverColor = brighter(backgroundColor, 0.15f);
        this.pressedColor = darker(backgroundColor, 0.1f);
        this.radius = radius;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBackground(backgroundColor);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }
    // End: reusable rounded button setup function.

    public void setGradient(Color end) {
        this.isGradient = true;
        this.gradientEnd = end;
    }

    @Override
    // Start: rounded button hit-area function.
    public boolean contains(int x, int y) {
        Shape shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius);
        return shape.contains(x, y);
    }
    // End: rounded button hit-area function.

    @Override
    // Start: rounded button paint function.
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color bg;
        if (!isEnabled()) {
            bg = new Color(84, 92, 111);
        } else if (isPressed) {
            bg = pressedColor;
        } else if (isHovered) {
            bg = hoverColor;
        } else {
            bg = backgroundColor;
        }

        Shape buttonShape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius);

        if (isEnabled()) {
            int shadowAlpha = isPressed ? 18 : (isHovered ? 36 : 26);
            g2.setColor(new Color(0, 0, 0, shadowAlpha));
            g2.fill(new RoundRectangle2D.Float(0, 2f, getWidth(), getHeight() - 1f, radius, radius));
        }

        if (isGradient && gradientEnd != null) {
            Color end = !isEnabled()
                    ? new Color(80, 86, 104)
                    : (isPressed ? darker(gradientEnd, 0.12f) : (isHovered ? brighter(gradientEnd, 0.08f) : gradientEnd));
            GradientPaint gp = new GradientPaint(0, 0, brighter(bg, 0.03f), getWidth(), getHeight(), end);
            g2.setPaint(gp);
        } else {
            g2.setPaint(new GradientPaint(0, 0, brighter(bg, isHovered ? 0.06f : 0.02f), 0, getHeight(),
                    darker(bg, isPressed ? 0.16f : 0.05f)));
        }

        g2.fill(buttonShape);

        if (isEnabled()) {
            g2.setColor(new Color(255, 255, 255, isHovered ? 34 : 22));
            Shape oldClip = g2.getClip();
            g2.clip(buttonShape);
            g2.fill(new Rectangle(0, 0, getWidth(), Math.max(1, getHeight() / 2)));
            g2.setColor(new Color(255, 255, 255, isPressed ? 0 : 10));
            g2.fill(new Rectangle(0, getHeight() / 2, getWidth(), Math.max(1, getHeight() / 2)));
            g2.setClip(oldClip);
        }

        g2.setColor(new Color(255, 255, 255, isHovered ? 98 : 66));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1,
                Math.max(2, radius - 1), Math.max(2, radius - 1)));

        if ("+".equals(getText())) {
            g2.setColor(getForeground());
            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int size = Math.min(getWidth(), getHeight()) / 3;
            g2.drawLine(getWidth()/2 - size/2, getHeight()/2, getWidth()/2 + size/2, getHeight()/2);
            g2.drawLine(getWidth()/2, getHeight()/2 - size/2, getWidth()/2, getHeight()/2 + size/2);
        } else if (getText() != null && getText().startsWith("+ ")) {
            String ctaText = getText().substring(2).trim();
            if (ctaText.isEmpty()) {
                ctaText = "NEW";
            }
            g2.setFont(getFont());
            g2.setColor(getForeground());
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(ctaText);
            int iconSize = 14;
            int gap = 8;
            int totalWidth = iconSize + gap + textWidth;
            
            int startX = (getWidth() - totalWidth) / 2;
            int cy = getHeight() / 2;
            
            
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(startX, cy, startX + iconSize, cy);
            g2.drawLine(startX + iconSize/2, cy - iconSize/2, startX + iconSize/2, cy + iconSize/2);
            
            
            int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(ctaText, startX + iconSize + gap, textY);
        } else if (getText() != null && !getText().isEmpty()) {
            g2.setFont(getFont());
            g2.setColor(getForeground());
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(getText());
            int textX = (getWidth() - textWidth) / 2;
            int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), textX, textY);
        }
        g2.dispose();
    }
    // End: rounded button paint function.

    private Color brighter(Color c, float factor) {
        int r = Math.min(255, (int)(c.getRed() + 255 * factor));
        int g = Math.min(255, (int)(c.getGreen() + 255 * factor));
        int b = Math.min(255, (int)(c.getBlue() + 255 * factor));
        return new Color(r, g, b);
    }

    private Color darker(Color c, float factor) {
        int r = Math.max(0, (int)(c.getRed() * (1 - factor)));
        int g = Math.max(0, (int)(c.getGreen() * (1 - factor)));
        int b = Math.max(0, (int)(c.getBlue() * (1 - factor)));
        return new Color(r, g, b);
    }
}
