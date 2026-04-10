package com.playtrack.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StarRating extends JPanel {
    private static final long serialVersionUID = 1L;
    private int rating = 0;
    private boolean editable;
    private int starSize;
    private StarIcon[] stars = new StarIcon[5];

    public StarRating(int initialRating, boolean editable) {
        this(initialRating, editable, 24);
    }

    public StarRating(int initialRating, boolean editable, int starSize) {
        this.rating = initialRating;
        this.editable = editable;
        this.starSize = starSize;
        setLayout(new FlowLayout(FlowLayout.LEFT, starSize / 6, 0));
        setOpaque(false);

        for (int i = 0; i < 5; i++) {
            final int index = i + 1;
            stars[i] = new StarIcon(starSize);
            stars[i].setCursor(new Cursor(editable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));

            if (editable) {
                stars[i].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) { setRating(index); }
                    @Override
                    public void mouseEntered(MouseEvent e) { updateStars(index); }
                    @Override
                    public void mouseExited(MouseEvent e) { updateStars(rating); }
                });
            }
            add(stars[i]);
        }
        updateStars(rating);
    }

    public void setRating(int rating) {
        this.rating = rating;
        updateStars(rating);
    }

    public int getRating() { return rating; }

    private void updateStars(int r) {
        for (int i = 0; i < 5; i++) {
            stars[i].setFilled(i < r);
        }
    }

    private class StarIcon extends JPanel {
        private static final long serialVersionUID = 1L;
        private boolean filled = false;
        private int size;

        public StarIcon(int size) {
            this.size = size;
            setPreferredSize(new Dimension(size, size));
            setOpaque(false);
        }

        public void setFilled(boolean f) {
            this.filled = f;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            
            double cx = w / 2.0;
            double cy = h / 2.0;
            double outerRadius = Math.min(w, h) / 2.0 - 1;
            double innerRadius = outerRadius * 0.45;
            
            java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
            for (int i = 0; i < 10; i++) {
                double r = (i % 2 == 0) ? outerRadius : innerRadius;
                double a = Math.PI / 2.0 - i * Math.PI / 5.0;
                double x = cx + Math.cos(a) * r;
                double y = cy - Math.sin(a) * r;
                if (i == 0) path.moveTo(x, y);
                else path.lineTo(x, y);
            }
            path.closePath();

            if (filled) {
                g2.setColor(StyleConfig.STAR_COLOR);
                g2.fill(path);
            } else {
                g2.setColor(StyleConfig.STAR_COLOR_EMPTY);
                g2.setStroke(new BasicStroke(size > 18 ? 1.5f : 1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(path);
            }
            
            g2.dispose();
        }
    }
}
