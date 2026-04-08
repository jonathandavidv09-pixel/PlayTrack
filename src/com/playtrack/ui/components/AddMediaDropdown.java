package com.playtrack.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

public class AddMediaDropdown extends JPopupMenu {

    private static final Color ITEM_BG = new Color(36, 45, 60);        // Normal item background
    private static final Color ITEM_HOVER_BG = new Color(55, 65, 82);  // Subtle hover highlight
    private static final Color DROPDOWN_BG = new Color(25, 30, 42);    // Outer dropdown bg
    private static final int ARC = 10;                                  // Corner radius

    public AddMediaDropdown(Consumer<String> onAddMedia) {
        setLightWeightPopupEnabled(true);
        setOpaque(true);
        setBackground(DROPDOWN_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 82, 180), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // Main container panel
        JPanel container = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DROPDOWN_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(true);
        container.setBackground(DROPDOWN_BG);
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        container.add(createItem("Film", 1, () -> { setVisible(false); onAddMedia.accept("Films"); }));
        container.add(Box.createVerticalStrut(3));
        container.add(createItem("Game", 2, () -> { setVisible(false); onAddMedia.accept("Games"); }));
        container.add(Box.createVerticalStrut(3));
        container.add(createItem("Book", 3, () -> { setVisible(false); onAddMedia.accept("Books"); }));
        container.add(Box.createVerticalStrut(3));
        container.add(createItem("Watchlist", 4, () -> { setVisible(false); onAddMedia.accept("Watchlist"); }));

        add(container);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(DROPDOWN_BG);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private JPanel createItem(String text, int iconType, Runnable action) {
        JPanel item = new JPanel(new BorderLayout()) {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
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

                    @Override
                    public void mousePressed(MouseEvent e) {
                        action.run();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill parent background first to avoid any black artifacts
                g2.setColor(DROPDOWN_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Draw rounded item background
                RoundRectangle2D.Float shape = new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), ARC, ARC);

                if (hovered) {
                    g2.setColor(ITEM_HOVER_BG);
                } else {
                    g2.setColor(ITEM_BG);
                }
                g2.fill(shape);

                g2.dispose();
            }
        };

        // IMPORTANT: set opaque to false so our custom paintComponent handles all painting
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(220, 40));
        item.setPreferredSize(new Dimension(220, 40));
        item.setMinimumSize(new Dimension(220, 40));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(text, createIcon(iconType, Color.WHITE), SwingConstants.LEFT);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(StyleConfig.TEXT_COLOR);
        lbl.setIconTextGap(12);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        // Label must also be non-opaque to let parent paint through
        lbl.setOpaque(false);
        item.add(lbl, BorderLayout.CENTER);

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
