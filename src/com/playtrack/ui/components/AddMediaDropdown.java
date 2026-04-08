package com.playtrack.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class AddMediaDropdown extends JPopupMenu {

    public AddMediaDropdown(Consumer<String> onAddMedia) {
        setBackground(StyleConfig.BACKGROUND_COLOR);
        setBorder(BorderFactory.createLineBorder(StyleConfig.BORDER_COLOR, 1));
        setOpaque(true);

        // Main container
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        PillButton filmBtn = new PillButton("Film", createIcon(1, Color.WHITE),
                StyleConfig.TEXT_COLOR, () -> {
                    setVisible(false);
                    onAddMedia.accept("Films");
                });
        filmBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(filmBtn);
        container.add(Box.createVerticalStrut(3));

        PillButton gameBtn = new PillButton("Game", createIcon(2, Color.WHITE),
                StyleConfig.TEXT_COLOR, () -> {
                    setVisible(false);
                    onAddMedia.accept("Games");
                });
        gameBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(gameBtn);
        container.add(Box.createVerticalStrut(3));

        PillButton bookBtn = new PillButton("Book", createIcon(3, Color.WHITE),
                StyleConfig.TEXT_COLOR, () -> {
                    setVisible(false);
                    onAddMedia.accept("Books");
                });
        bookBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(bookBtn);
        container.add(Box.createVerticalStrut(3));

        PillButton watchBtn = new PillButton("Watchlist", createIcon(4, Color.WHITE),
                StyleConfig.TEXT_COLOR, () -> {
                    setVisible(false);
                    onAddMedia.accept("Watchlist");
                });
        watchBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(watchBtn);

        add(container);
    }

    // Custom Pill Button
    private class PillButton extends JPanel {
        private boolean hovered = false;

        public PillButton(String text, Icon icon, Color textColor, Runnable action) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setMaximumSize(new Dimension(250, 45));
            setPreferredSize(new Dimension(250, 45));
            setMinimumSize(new Dimension(250, 45));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel lbl = new JLabel(text, icon, SwingConstants.LEFT);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setForeground(textColor);
            lbl.setIconTextGap(15);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
            add(lbl, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                public void mouseClicked(MouseEvent e) {
                    action.run();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (hovered) {
                g2.setColor(StyleConfig.CARD_BACKGROUND);
            } else {
                g2.setColor(StyleConfig.BACKGROUND_LIGHT);
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
        }
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
            UIUtils.drawDropdownCategoryIcon(g2, cx, cy, type, color, 28);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 32; }

        @Override
        public int getIconHeight() { return 32; }
    }
}
