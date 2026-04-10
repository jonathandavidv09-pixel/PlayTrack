package com.playtrack.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

public class PlaceholderTextField extends JTextField {
    private static final long serialVersionUID = 1L;
    private String placeholder;
    private boolean showingPlaceholder;
    private boolean focused = false;
    private String iconText = null;

    public PlaceholderTextField(String placeholder) {
        this(placeholder, null);
    }

    public PlaceholderTextField(String placeholder, String iconText) {
        this.placeholder = placeholder;
        this.showingPlaceholder = true;
        this.iconText = iconText;
        setText(placeholder);
        setForeground(StyleConfig.TEXT_LIGHT);
        setFont(StyleConfig.FONT_NORMAL);
        setCaretColor(StyleConfig.TEXT_COLOR);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, iconText != null ? 40 : 16, 12, 16));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                if (showingPlaceholder) {
                    setText("");
                    setForeground(StyleConfig.TEXT_COLOR);
                    showingPlaceholder = false;
                }
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                if (getText().isEmpty()) {
                    setText(placeholder);
                    setForeground(StyleConfig.TEXT_LIGHT);
                    showingPlaceholder = true;
                }
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setPaint(new GradientPaint(
                0, 0, focused ? StyleConfig.INPUT_BG_FOCUS : StyleConfig.INPUT_BG,
                0, getHeight(), StyleConfig.BACKGROUND_LIGHT));
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));

        // Border
        if (focused) {
            g2.setColor(StyleConfig.INPUT_FOCUS);
            g2.setStroke(new BasicStroke(2f));
        } else {
            g2.setColor(StyleConfig.SURFACE_STROKE);
            g2.setStroke(new BasicStroke(1f));
        }
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 13, 13));

        // Icon
        if (iconText != null) {
            if ("SEARCH".equals(iconText)) {
                Color iconColor = focused ? StyleConfig.PRIMARY_COLOR : StyleConfig.TEXT_LIGHT;
                UIUtils.drawSearchIcon(g2, 20 - 9, getHeight() / 2 - 9, 18, iconColor);
            } else {
                g2.setColor(focused ? StyleConfig.PRIMARY_COLOR : StyleConfig.TEXT_LIGHT);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(iconText, 14, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        }

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public String getText() {
        return showingPlaceholder ? "" : super.getText();
    }
}
