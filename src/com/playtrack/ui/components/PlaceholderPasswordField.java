package com.playtrack.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class PlaceholderPasswordField extends JPasswordField {
    private static final long serialVersionUID = 1L;
    private String placeholder;
    private boolean showingPlaceholder;
    private char defaultEchoChar;
    private boolean focused = false;
    private boolean passwordVisible = false;
    private JLabel eyeLabel;
    private Rectangle eyeBounds = new Rectangle();
    private String iconText = null;

    public PlaceholderPasswordField(String placeholder) {
        this(placeholder, null);
    }

    public PlaceholderPasswordField(String placeholder, String iconText) {
        this.placeholder = placeholder;
        this.showingPlaceholder = true;
        this.iconText = iconText;
        this.defaultEchoChar = getEchoChar();
        setEchoChar((char) 0);
        setText(placeholder);
        setForeground(StyleConfig.TEXT_LIGHT);
        setFont(StyleConfig.FONT_NORMAL);
        setCaretColor(StyleConfig.TEXT_COLOR);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, iconText != null ? 40 : 16, 12, 44));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                if (showingPlaceholder) {
                    setText("");
                    setEchoChar(passwordVisible ? (char) 0 : defaultEchoChar);
                    setForeground(StyleConfig.TEXT_COLOR);
                    showingPlaceholder = false;
                }
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                if (new String(getPassword()).isEmpty()) {
                    setText(placeholder);
                    setEchoChar((char) 0);
                    setForeground(StyleConfig.TEXT_LIGHT);
                    showingPlaceholder = true;
                    passwordVisible = false;
                }
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (eyeBounds.contains(e.getPoint()) && !showingPlaceholder) {
                    togglePasswordVisibility();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (eyeBounds.contains(e.getPoint())) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(new Cursor(Cursor.TEXT_CURSOR));
                }
            }
        });
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            setEchoChar((char) 0);
        } else {
            setEchoChar(defaultEchoChar);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        
        g2.setPaint(new GradientPaint(
                0, 0, focused ? StyleConfig.INPUT_BG_FOCUS : StyleConfig.INPUT_BG,
                0, getHeight(), StyleConfig.BACKGROUND_LIGHT));
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));

        
        if (focused) {
            g2.setColor(StyleConfig.INPUT_FOCUS);
            g2.setStroke(new BasicStroke(2f));
        } else {
            g2.setColor(StyleConfig.SURFACE_STROKE);
            g2.setStroke(new BasicStroke(1f));
        }
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 13, 13));

        
        if (iconText != null) {
            if ("LOCK".equals(iconText)) {
                Color iconColor = focused ? StyleConfig.PRIMARY_COLOR : StyleConfig.TEXT_LIGHT;
                UIUtils.drawLockIcon(g2, 14, getHeight() / 2 - 8, 16, 16, iconColor);
            } else {
                g2.setColor(focused ? StyleConfig.PRIMARY_COLOR : StyleConfig.TEXT_LIGHT);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(iconText, 14, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        }

        g2.dispose();
        super.paintComponent(g);

        
        Graphics2D g3 = (Graphics2D) g.create();
        g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int eyeX = getWidth() - 25;
        int eyeY = getHeight() / 2;
        eyeBounds = new Rectangle(eyeX - 15, 0, 30, getHeight());

        Color eyeColor = showingPlaceholder ? StyleConfig.TEXT_LIGHT : (passwordVisible ? StyleConfig.SECONDARY_COLOR : StyleConfig.TEXT_SECONDARY);
        g3.setColor(eyeColor);
        g3.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        UIUtils.drawEyeIcon(g3, eyeX - 10, eyeY - 6, 20, 12, eyeColor, passwordVisible);

        g3.dispose();
    }

    @Override
    public char[] getPassword() {
        return showingPlaceholder ? new char[0] : super.getPassword();
    }

    public boolean isPasswordVisible() {
        return passwordVisible;
    }
}
