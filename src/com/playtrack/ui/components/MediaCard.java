package com.playtrack.ui.components;

import com.playtrack.dao.WatchlistDAO;
import com.playtrack.model.MediaItem;
import com.playtrack.model.Review;
import com.playtrack.service.MediaService;
import com.playtrack.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

public class MediaCard extends JPanel {
    private final MediaItem item;
    private final MediaService mediaService = new MediaService();
    private final Runnable onRefresh;
    private final boolean showDeleteIcon;
    private boolean hovered = false;
    private java.awt.Image posterImage = null;

    private Review review;
    private StarRating starRating;
    private JToggleButton favoriteToggle;
    private JButton deleteButton;

    public MediaCard(MediaItem item) {
        this(item, false, false, null);
    }

    public MediaCard(MediaItem item, boolean compact, boolean showDeleteIcon, Runnable onRefresh) {
        this.item = item;
        this.showDeleteIcon = showDeleteIcon;
        this.onRefresh = onRefresh;

        setLayout(null);
        // Clean poster aspect ratio (2:3)
        setPreferredSize(new Dimension(160, 240));
        setMinimumSize(new Dimension(160, 240));
        setMaximumSize(new Dimension(160, 240));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText(item.getTitle() + (item.getGenre() != null ? " • " + item.getGenre() : ""));

        loadPoster();
        refreshReviewState();
        buildComponents();
        registerInteractions();
    }

    private void loadPoster() {
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                java.awt.image.BufferedImage rawImg = javax.imageio.ImageIO.read(new File(item.getImagePath()));
                if (rawImg != null) {
                    posterImage = rawImg.getScaledInstance(160, 240, java.awt.Image.SCALE_SMOOTH);
                }
            } catch (Exception ex) {
                posterImage = null;
            }
        }
    }

    private void refreshReviewState() {
        review = mediaService.getReviewByMedia(item.getId());
    }

    private void buildComponents() {
        int ratingVal = review != null ? review.getRating() : 0;
        boolean isFav = review != null && review.isFavorite();
        boolean isWatch = review != null && review.isWatchlist();

        // Footer Y near the bottom of the card
        int footerY = 205;

        if (item.getId() >= 0 && !isWatch) {
            starRating = new StarRating(ratingVal, false, 14);
            starRating.setBounds(6, footerY, 85, 14);
            add(starRating);

            int actionX = 98;

            favoriteToggle = createFavoriteToggle(isFav);
            favoriteToggle.setBounds(actionX, footerY - 6, 26, 26);
            add(favoriteToggle);
            actionX += 28;

            if (showDeleteIcon) {
                deleteButton = createDeleteButton();
                deleteButton.setBounds(actionX, footerY - 6, 26, 26);
                add(deleteButton);
            }
        } else {
            if (showDeleteIcon) {
                deleteButton = createDeleteButton();
                deleteButton.setBounds(126, footerY - 6, 26, 26);
                add(deleteButton);
            }
        }

        setControlsVisible(false);
    }

    private void setControlsVisible(boolean visible) {
        if (starRating != null)
            starRating.setVisible(visible);
        if (favoriteToggle != null)
            favoriteToggle.setVisible(visible);
        if (deleteButton != null)
            deleteButton.setVisible(visible);
    }

    private JToggleButton createFavoriteToggle(boolean selected) {
        JToggleButton toggle = new JToggleButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? StyleConfig.PRIMARY_COLOR : Color.WHITE);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                if (isSelected()) {
                    g2.fillArc(cx - 7, cy - 7, 7, 7, 0, 180);
                    g2.fillArc(cx, cy - 7, 7, 7, 0, 180);
                    g2.fillPolygon(new int[] { cx - 7, cx + 7, cx }, new int[] { cy - 4, cy - 4, cy + 7 }, 3);
                } else {
                    g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawArc(cx - 7, cy - 7, 7, 7, 0, 180);
                    g2.drawArc(cx, cy - 7, 7, 7, 0, 180);
                    g2.drawLine(cx - 7, cy - 4, cx, cy + 7);
                    g2.drawLine(cx + 7, cy - 4, cx, cy + 7);
                }
                g2.dispose();
            }
        };
        styleActionToggle(toggle, selected, "Favorite");
        toggle.addActionListener(e -> {
            Review currentReview = ensureReview();
            currentReview.setFavorite(toggle.isSelected());
            mediaService.addOrUpdateReview(currentReview);
            triggerRefresh();
        });
        return toggle;
    }

    private JButton createDeleteButton() {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int iconSize = 18;
                int ox = (getWidth() - iconSize) / 2;
                int oy = (getHeight() - iconSize) / 2;
                UIUtils.drawTrashIcon(g2, ox, oy, iconSize, Color.WHITE);
                g2.dispose();
            }
        };
        button.setToolTipText("Delete");
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> deleteCurrentItem());
        return button;
    }

    private void styleActionToggle(AbstractButton button, boolean selected, String tooltip) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setSelected(selected);
        button.setToolTipText(tooltip);
    }

    private void registerInteractions() {
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                hovered = true;
                setControlsVisible(true);
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                // If moving into a child component (like a button), do not hide yet
                if (contains(e.getPoint()))
                    return;
                hovered = false;
                setControlsVisible(false);
                repaint();
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    openReviewDialog();
                }
            }
        });
    }

    private void openReviewDialog() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof Frame) {
            com.playtrack.ui.review.ReviewFormDialog dialog = new com.playtrack.ui.review.ReviewFormDialog(
                    (Frame) window, item, this::triggerRefresh);
            dialog.setVisible(true);
        }
    }

    private Review ensureReview() {
        Review currentReview = mediaService.getReviewByMedia(item.getId());
        if (currentReview == null) {
            currentReview = new Review();
            currentReview.setMediaId(item.getId());
            currentReview.setUserId(SessionManager.getCurrentUser().getId());
            currentReview.setRating(0);
            currentReview.setFavorite(false);
            currentReview.setWatchlist(false);
            currentReview.setReviewText("");
        }
        return currentReview;
    }

    private void deleteCurrentItem() {
        String msg = (item.getId() < 0) ? "Remove this from your watchlist?" : "Delete this item from your library?";
        int confirm = JOptionPane.showConfirmDialog(
                this,
                msg,
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (item.getId() < 0) {
                new WatchlistDAO().removeFromWatchlist(Math.abs(item.getId()));
            } else {
                mediaService.deleteMedia(item.getId());
            }
            triggerRefresh();
        }
    }

    private void triggerRefresh() {
        Window window = SwingUtilities.getWindowAncestor(this);

        refreshComponentState();

        if (onRefresh != null) {
            onRefresh.run();
        }

        if (window instanceof com.playtrack.ui.main.MainFrame) {
            ((com.playtrack.ui.main.MainFrame) window).refreshAll();
        }
    }

    private void refreshComponentState() {
        refreshReviewState();

        // Recreate stars just in case rating changed
        if (starRating != null) {
            remove(starRating);
            starRating = new StarRating(review != null ? review.getRating() : 0, false, 14);
            starRating.setBounds(6, 205, 85, 14);
            add(starRating);
            setComponentZOrder(starRating, 0);
        }

        if (favoriteToggle != null) {
            favoriteToggle.setSelected(review != null && review.isFavorite());
            favoriteToggle.repaint();
        }

        setControlsVisible(hovered);
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = 8; // Small arc for Letterboxd style

        // Shadow/glow if hovered
        if (hovered) {
            Color glowColor = getCategoryColor(item.getCategory());
            g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 60));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
        }

        Shape clipShape = new RoundRectangle2D.Float(0, 0, w, h, arc, arc);
        g2.setClip(clipShape);

        if (posterImage != null) {
            g2.drawImage(posterImage, 0, 0, w, h, null);
        } else {
            // Placeholder
            Color catColor = getCategoryColor(item.getCategory());
            g2.setColor(catColor.darker().darker());
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(255, 255, 255, 30));
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawCategoryIcon(g2, item.getCategory(), w / 2, h / 2 - 20, false);

            if (!hovered) { // Fix: Only draw title if not hovered to avoid double-rendering
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                String title = item.getTitle();
                int stringW = fm.stringWidth(title);
                if (stringW > w - 20) {
                    title = title.substring(0, Math.min(title.length(), 15)) + "...";
                    stringW = fm.stringWidth(title);
                }
                g2.drawString(title, (w - stringW) / 2, h / 2 + 30);
            }
        }

        // Overlay on hover
        if (hovered) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, w, h);

            // Draw title text cleanly centered
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();

            String title = item.getTitle();
            // simple wrap calculation for max 2 lines
            int maxWidth = w - 20;
            if (fm.stringWidth(title) > maxWidth) {
                int splitIndex = title.length() / 2;
                int spaceIndex = title.lastIndexOf(" ", splitIndex);
                if (spaceIndex == -1)
                    spaceIndex = splitIndex;
                String line1 = title.substring(0, spaceIndex);
                String line2 = title.substring(spaceIndex).trim();
                if (fm.stringWidth(line2) > maxWidth)
                    line2 = line2.substring(0, Math.min(line2.length(), 10)) + "...";

                g2.drawString(line1, (w - fm.stringWidth(line1)) / 2, 40);
                g2.drawString(line2, (w - fm.stringWidth(line2)) / 2, 40 + fm.getHeight());
            } else {
                g2.drawString(title, (w - fm.stringWidth(title)) / 2, 50);
            }
        }

        g2.setClip(null);

        // Thin border
        g2.setColor(new Color(255, 255, 255, hovered ? 80 : 30));
        g2.setStroke(new BasicStroke(hovered ? 2f : 1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));

        g2.dispose();
    }

    private void drawCategoryIcon(Graphics2D g2, String category, int cx, int cy, boolean mini) {
        float strokeWidth = 3f;
        double scale = mini ? 2.8 : 2.5;
        UIUtils.drawCategoryIcon(g2, category, cx, cy, g2.getColor(), strokeWidth, scale);
    }

    private Color getCategoryColor(String category) {
        switch (category) {
            case "Films":
                return StyleConfig.FILM_COLOR;
            case "Games":
                return StyleConfig.GAME_COLOR;
            case "Books":
                return StyleConfig.BOOK_COLOR;
            default:
                return StyleConfig.TEXT_LIGHT;
        }
    }
}
