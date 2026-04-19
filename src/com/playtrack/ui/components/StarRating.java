package com.playtrack.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
// Custom star rating component.
public class StarRating extends JPanel {
    private static final long serialVersionUID = 1L;
    private double rating = 0.0;
    private double hoverRating = -1.0;
    private final boolean editable;
    private final int starSize;
    private final StarIcon[] stars = new StarIcon[5];

    // Constructor.
    public StarRating(int initialRating, boolean editable) {
        this((double) initialRating, editable, 24);
    }

    public StarRating(int initialRating, boolean editable, int starSize) {
        this((double) initialRating, editable, starSize);
    }

    public StarRating(double initialRating, boolean editable) {
        this(initialRating, editable, 24);
    }

    // Start: star rating button setup function.
    public StarRating(double initialRating, boolean editable, int starSize) {
        this.rating = normalizeToHalf(initialRating);
        this.editable = editable;
        this.starSize = starSize;
        setLayout(new FlowLayout(FlowLayout.LEFT, starSize / 6, 0));
        setOpaque(false);

        // Create and add the star icons to the panel.
        for (int i = 0; i < 5; i++) {
            final int index = i;
            stars[i] = new StarIcon(starSize);
            stars[i].setCursor(new Cursor(editable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));

            if (editable) {
                stars[i].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Button action: set the selected star rating value.
                        setRating(valueFromPosition(index, e.getX(), stars[index].getWidth()));
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        setHoverRating(valueFromPosition(index, e.getX(), stars[index].getWidth()));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }
                });
                stars[i].addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        setHoverRating(valueFromPosition(index, e.getX(), stars[index].getWidth()));
                    }
                });
            }
            add(stars[i]);
        }

        if (editable) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoverRating = -1.0;
                    updateStars();
                }
            });
        }

        updateStars();
    }
    // End: star rating button setup function.

    public void setRating(double rating) {
        this.rating = normalizeToHalf(rating);
        this.hoverRating = -1.0;
        updateStars();
    }

    public void setRating(int rating) {
        setRating((double) rating);
    }

    public double getRating() { return rating; }

    public int getRoundedRating() {
        return (int) Math.round(rating);
    }

    private void setHoverRating(double hoverValue) {
        this.hoverRating = normalizeToHalf(hoverValue);
        updateStars();
    }

    private void updateStars() {
        double effectiveRating = hoverRating >= 0 ? hoverRating : rating;
        for (int i = 0; i < 5; i++) {
            double fill = clamp(effectiveRating - i, 0.0, 1.0);
            stars[i].setFillFraction(fill);
            stars[i].setToolTipText(String.format("%.1f", effectiveRating).replace(".0", ""));
        }
    }

    private double valueFromPosition(int zeroBasedStarIndex, int mouseX, int width) {
        int safeWidth = Math.max(1, width);
        boolean half = mouseX < (safeWidth / 2.0);
        double raw = zeroBasedStarIndex + (half ? 0.5 : 1.0);
        return normalizeToHalf(raw);
    }

    private double normalizeToHalf(double value) {
        return clamp(Math.round(value * 2.0) / 2.0, 0.0, 5.0);
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    // Inner class for the star icon.
    private class StarIcon extends JPanel {
        private static final long serialVersionUID = 1L;
        private double fillFraction = 0.0;
        private final int size;

        public StarIcon(int size) {
            this.size = size;
            setPreferredSize(new Dimension(size, size));
            setOpaque(false);
        }

        public void setFillFraction(double fillFraction) {
            this.fillFraction = clamp(fillFraction, 0.0, 1.0);
            repaint();
        }

        // Custom painting of the star shape.
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

            Path2D.Double path = new Path2D.Double();
            for (int i = 0; i < 10; i++) {
                double r = (i % 2 == 0) ? outerRadius : innerRadius;
                double a = Math.PI / 2.0 - i * Math.PI / 5.0;
                double x = cx + Math.cos(a) * r;
                double y = cy - Math.sin(a) * r;
                if (i == 0)
                    path.moveTo(x, y);
                else
                    path.lineTo(x, y);
            }
            path.closePath();

            g2.setColor(StyleConfig.withAlpha(StyleConfig.STAR_COLOR_EMPTY, 90));
            g2.fill(path);

            if (fillFraction > 0.0) {
                Shape oldClip = g2.getClip();
                g2.clip(new Rectangle2D.Double(0, 0, w * fillFraction, h));
                g2.setColor(StyleConfig.STAR_COLOR);
                g2.fill(path);
                g2.setClip(oldClip);
            }

            g2.setColor(fillFraction > 0.0 ? StyleConfig.withAlpha(Color.WHITE, 84) : StyleConfig.STAR_COLOR_EMPTY);
            g2.setStroke(new BasicStroke(size > 18 ? 1.45f : 1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(path);

            g2.dispose();
        }
    }
}
