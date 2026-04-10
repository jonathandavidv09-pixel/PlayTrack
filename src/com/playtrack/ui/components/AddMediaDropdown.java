package com.playtrack.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

public class AddMediaDropdown extends JPopupMenu {
    private static final long serialVersionUID = 1L;

    private static final Color ITEM_BG = StyleConfig.SURFACE_ELEVATED;
    private static final Color ITEM_HOVER_BG = StyleConfig.SURFACE_SOFT;
    private static final Color ITEM_PRESSED_BG = new Color(72, 84, 116);
    private static final Color DROPDOWN_BG = StyleConfig.BACKGROUND_LIGHT;
    private static final int ARC = 14;
    private static final int ITEM_WIDTH = 220;
    private static final int ITEM_HEIGHT = 40;
    private static final int ITEM_GAP = 4;
    private static final int CONTENT_PAD = 12;

    public AddMediaDropdown(Consumer<String> onAddMedia) {
        // Heavyweight popup avoids layered-pane transparency artifacts on some systems.
        setLightWeightPopupEnabled(false);
        setDoubleBuffered(true);
        setOpaque(true);
        setBackground(DROPDOWN_BG);
        setBorder(BorderFactory.createEmptyBorder());

        // Main container panel
        JPanel container = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Always fill whole bounds to prevent black artifacts.
                g2.setColor(DROPDOWN_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Surface body
                g2.setColor(DROPDOWN_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), ARC, ARC));

                // Accent strip
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), 16, ARC, ARC));
                g2.setPaint(new GradientPaint(0, 0, StyleConfig.PRIMARY_COLOR, getWidth(), 0, StyleConfig.SECONDARY_COLOR));
                g2.fillRect(0, 0, getWidth(), 3);
                g2.setClip(null);

                // Border
                g2.setColor(new Color(255, 255, 255, 30));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, ARC - 1, ARC - 1));
                g2.dispose();
            }
        };
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(true);
        container.setBackground(DROPDOWN_BG);
        container.setBorder(BorderFactory.createEmptyBorder(CONTENT_PAD, CONTENT_PAD, CONTENT_PAD, CONTENT_PAD));

        container.add(createItem("Film", 1, () -> { setVisible(false); onAddMedia.accept("Films"); }));
        container.add(Box.createVerticalStrut(ITEM_GAP));
        container.add(createItem("Game", 2, () -> { setVisible(false); onAddMedia.accept("Games"); }));
        container.add(Box.createVerticalStrut(ITEM_GAP));
        container.add(createItem("Book", 3, () -> { setVisible(false); onAddMedia.accept("Books"); }));
        container.add(Box.createVerticalStrut(ITEM_GAP));
        container.add(createItem("Watchlist", 4, () -> { setVisible(false); onAddMedia.accept("Watchlist"); }));

        int popupWidth = (CONTENT_PAD * 2) + ITEM_WIDTH;
        int popupHeight = (CONTENT_PAD * 2) + (ITEM_HEIGHT * 4) + (ITEM_GAP * 3);
        Dimension pref = new Dimension(popupWidth, popupHeight);
        container.setPreferredSize(pref);
        setPreferredSize(pref);
        setPopupSize(pref);

        add(container);
    }

    @Override
    public void show(Component invoker, int x, int y) {
        setPopupSize(getPreferredSize());
        super.show(invoker, x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(DROPDOWN_BG);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private JPanel createItem(String text, int iconType, Runnable action) {
        class ItemPanel extends JPanel {
            private static final long serialVersionUID = 1L;
            private boolean hovered;
            private boolean pressed;

            ItemPanel() {
                super(new BorderLayout());
            }

            void setHovered(boolean hovered) {
                if (this.hovered != hovered) {
                    this.hovered = hovered;
                    repaint();
                }
            }

            void setPressed(boolean pressed) {
                if (this.pressed != pressed) {
                    this.pressed = pressed;
                    repaint();
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill parent background first to avoid transparent artifacts
                g2.setColor(DROPDOWN_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                RoundRectangle2D.Float shape = new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), ARC, ARC);

                if (pressed) {
                    g2.setPaint(new GradientPaint(0, 0, ITEM_PRESSED_BG, getWidth(), 0, ITEM_HOVER_BG));
                } else if (hovered) {
                    g2.setPaint(new GradientPaint(0, 0, new Color(58, 67, 96), getWidth(), 0, ITEM_HOVER_BG));
                } else {
                    g2.setColor(ITEM_BG);
                }
                g2.fill(shape);

                if (hovered || pressed) {
                    g2.setColor(pressed ? StyleConfig.SECONDARY_COLOR : StyleConfig.PRIMARY_COLOR);
                    g2.fillRoundRect(0, 7, 4, getHeight() - 14, 4, 4);
                    g2.setColor(new Color(255, 255, 255, pressed ? 62 : 42));
                } else {
                    g2.setColor(new Color(255, 255, 255, 22));
                }
                g2.draw(shape);

                g2.dispose();
            }
        }

        ItemPanel item = new ItemPanel();

        item.setOpaque(false);
        item.setMaximumSize(new Dimension(ITEM_WIDTH, ITEM_HEIGHT));
        item.setPreferredSize(new Dimension(ITEM_WIDTH, ITEM_HEIGHT));
        item.setMinimumSize(new Dimension(ITEM_WIDTH, ITEM_HEIGHT));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(text, createIcon(iconType, Color.WHITE), SwingConstants.LEFT);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(StyleConfig.TEXT_COLOR);
        lbl.setIconTextGap(12);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        // Label must also be non-opaque to let parent paint through
        lbl.setOpaque(false);
        item.add(lbl, BorderLayout.CENTER);

        MouseAdapter sharedHoverHandler = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setHovered(true);
                lbl.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setPressed(false);
                // Defer check to avoid flicker when moving between label and parent row.
                SwingUtilities.invokeLater(() -> {
                    if (!item.isShowing()) {
                        item.setHovered(false);
                        return;
                    }
                    PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                    if (pointerInfo == null) {
                        item.setHovered(false);
                        return;
                    }
                    Point p = pointerInfo.getLocation();
                    SwingUtilities.convertPointFromScreen(p, item);
                    item.setHovered(item.contains(p));
                    if (!item.contains(p)) {
                        lbl.setForeground(StyleConfig.TEXT_COLOR);
                    }
                });
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    item.setPressed(true);
                    lbl.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                item.setPressed(false);
                Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), item);
                item.setHovered(item.contains(p));
                lbl.setForeground(item.contains(p) ? Color.WHITE : StyleConfig.TEXT_COLOR);
                if (item.contains(p)) {
                    action.run();
                }
            }
        };

        item.addMouseListener(sharedHoverHandler);
        lbl.addMouseListener(sharedHoverHandler);

        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        return item;
    }

    private Icon createIcon(int type, Color color) {
        return new CategoryIcon(type, color);
    }

    private static class CategoryIcon implements Icon {
        private final int type;
        private final Color color;

        public CategoryIcon(int type, Color color) {
            this.type = type;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            int cx = x + getIconWidth() / 2;
            int cy = y + getIconHeight() / 2;
            UIUtils.drawDropdownCategoryIcon(g2, cx, cy, type, color, 24);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 28; }

        @Override
        public int getIconHeight() { return 28; }
    }
}
