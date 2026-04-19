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

// Media card component.
public class MediaCard extends JPanel {
    private static final long serialVersionUID = 1L;
    private final MediaItem item;
    private static final MediaService mediaService = new MediaService();

    private static final java.util.Map<String, java.awt.Image> posterCache = java.util.Collections
            .synchronizedMap(new java.util.LinkedHashMap<String, java.awt.Image>(100, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<String, java.awt.Image> eldest) {
                    return size() > 200;
                }
            });
    private final Runnable onRefresh;
    private final boolean showDeleteIcon;
    private final boolean forceReviewControls;
    private boolean hovered = false;
    private boolean controlsAlwaysVisible = false;
    private java.awt.Image posterImage = null;

    private Review review;
    private StarRating starRating;
    private JToggleButton favoriteToggle;
    private JButton deleteButton;

    public MediaCard(MediaItem item) {
        this(item, false, false, null);
    }

    public MediaCard(MediaItem item, boolean compact, boolean showDeleteIcon, Runnable onRefresh) {
        this(item, compact, showDeleteIcon, onRefresh, false);
    }

    public MediaCard(MediaItem item, boolean compact, boolean showDeleteIcon, Runnable onRefresh,
            boolean forceReviewControls) {
        this.item = item;
        this.showDeleteIcon = showDeleteIcon;
        this.onRefresh = onRefresh;
        this.forceReviewControls = forceReviewControls;

        setLayout(null);

        setPreferredSize(new Dimension(160, 240));
        setMinimumSize(new Dimension(160, 240));
        setMaximumSize(new Dimension(160, 240));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText(item.getTitle() + (item.getGenre() != null ? " | " + item.getGenre() : ""));

        loadPoster();
        refreshReviewState();
        buildComponents();
        registerInteractions();
    }

    private void loadPoster() {
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            String cacheKey = item.getImagePath();
            java.awt.Image cached = posterCache.get(cacheKey);
            if (cached != null) {
                posterImage = cached;
                return;
            }
            try {
                java.awt.image.BufferedImage rawImg = javax.imageio.ImageIO.read(new File(item.getImagePath()));
                if (rawImg != null) {
                    posterImage = rawImg.getScaledInstance(160, 240, java.awt.Image.SCALE_SMOOTH);
                    posterCache.put(cacheKey, posterImage);
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
        double ratingVal = review != null ? review.getRating() : 0.0;
        boolean isFav = review != null && review.isFavorite();
        boolean isWatch = review != null && review.isWatchlist();

        int footerY = 205;

        if (item.getId() >= 0 && (!isWatch || forceReviewControls)) {
            starRating = new StarRating(ratingVal, false, 14);
            starRating.setBounds(6, footerY, 85, 14);
            add(starRating);

            int actionX = forceReviewControls ? 88 : 98;

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
        boolean shouldShow = controlsAlwaysVisible || visible;
        if (starRating != null)
            starRating.setVisible(shouldShow);
        if (favoriteToggle != null)
            favoriteToggle.setVisible(shouldShow);
        if (deleteButton != null)
            deleteButton.setVisible(shouldShow);
    }

    public void setControlsAlwaysVisible(boolean controlsAlwaysVisible) {
        this.controlsAlwaysVisible = controlsAlwaysVisible;
        setControlsVisible(hovered);
        repaint();
    }

    // Start: favorite toggle button function.
    private JToggleButton createFavoriteToggle(boolean selected) {
        // Hover-revealed toggle button for marking/unmarking favorites.
        JToggleButton toggle = new JToggleButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ButtonModel model = getModel();
                boolean pressed = model.isPressed();
                boolean rollover = model.isRollover();
                Color bgTop = pressed ? new Color(59, 70, 96, 215)
                        : (rollover ? new Color(67, 79, 110, 205) : new Color(21, 28, 43, 172));
                Color bgBottom = pressed ? new Color(44, 53, 74, 225)
                        : (rollover ? new Color(50, 61, 86, 210) : new Color(13, 18, 31, 188));
                g2.setPaint(new GradientPaint(0, 0, bgTop, 0, getHeight(), bgBottom));
                g2.fillOval(1, 1, getWidth() - 2, getHeight() - 2);
                g2.setColor(new Color(255, 255, 255, 65));
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
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
            // Button action: save the favorite state for this media item.
            Review currentReview = ensureReview();
            currentReview.setFavorite(toggle.isSelected());
            mediaService.addOrUpdateReview(currentReview);
            triggerRefresh();
        });
        return toggle;
    }
    // End: favorite toggle button function.

    // Start: delete button function.
    private JButton createDeleteButton() {
        // Hover-revealed delete button for removing this media card item.
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ButtonModel model = getModel();
                boolean pressed = model.isPressed();
                boolean rollover = model.isRollover();
                Color bgTop = pressed ? new Color(59, 70, 96, 215)
                        : (rollover ? new Color(67, 79, 110, 205) : new Color(21, 28, 43, 172));
                Color bgBottom = pressed ? new Color(44, 53, 74, 225)
                        : (rollover ? new Color(50, 61, 86, 210) : new Color(13, 18, 31, 188));
                g2.setPaint(new GradientPaint(0, 0, bgTop, 0, getHeight(), bgBottom));
                g2.fillOval(1, 1, getWidth() - 2, getHeight() - 2);
                g2.setColor(new Color(255, 255, 255, 65));
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
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
        // Button action: open the delete confirmation flow.
        button.addActionListener(e -> deleteCurrentItem());
        return button;
    }
    // End: delete button function.

    private void styleActionToggle(AbstractButton button, boolean selected, String tooltip) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setSelected(selected);
        button.setToolTipText(tooltip);
    }

    // Start: media card click interaction function.
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

                try {
                    Point screenPt = e.getLocationOnScreen();
                    Point cardLoc = getLocationOnScreen();
                    if (screenPt.x >= cardLoc.x && screenPt.x < cardLoc.x + getWidth()
                            && screenPt.y >= cardLoc.y && screenPt.y < cardLoc.y + getHeight()) {
                        return;
                    }
                } catch (Exception ex) {

                }
                hovered = false;
                setControlsVisible(false);
                repaint();
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    // Button/card action: open review details for this media item.
                    openReviewDialog();
                }
            }
        });
    }

    // End: media card click interaction function.
    // Opens the review dialog for this media item.
    private void openReviewDialog() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof Frame) {
            com.playtrack.ui.review.ReviewFormDialog dialog = new com.playtrack.ui.review.ReviewFormDialog(
                    (Frame) window, item, this::triggerRefresh);
            dialog.setVisible(true);
        }
    }

    // Ensures that a Review object exists for this media item and the current user.
    private Review ensureReview() {
        Review currentReview = mediaService.getReviewByMedia(item.getId());
        if (currentReview == null) {
            currentReview = new Review();
            currentReview.setMediaId(item.getId());
            currentReview.setUserId(SessionManager.getCurrentUser().getId());
            currentReview.setRating(0.0);
            currentReview.setFavorite(false);
            currentReview.setWatchlist(false);
            currentReview.setReviewText("");
        }
        return currentReview;
    }

    // Handles the deletion of the current media item.
    // Start: delete confirmation button flow function.
    private void deleteCurrentItem() {
        String msg = (item.getId() < 0) ? "Remove this from your watchlist?" : "Delete this item from your library?";

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow instanceof Frame ? (Frame) parentWindow : null, "Confirm Delete",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        final boolean[] confirmed = { false };

        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                for (int i = 6; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 10 * i));
                    g2.fill(new RoundRectangle2D.Float(6 - i, 6 - i, getWidth() - 12 + i * 2, getHeight() - 12 + i * 2,
                            28 + i, 28 + i));
                }

                g2.setColor(new Color(30, 36, 50));
                g2.fill(new RoundRectangle2D.Float(6, 6, getWidth() - 12, getHeight() - 12, 28, 28));

                g2.setColor(new Color(255, 255, 255, 18));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(6.5f, 6.5f, getWidth() - 13, getHeight() - 13, 27, 27));
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel centerPanel = new JPanel(new BorderLayout(20, 0));
        centerPanel.setOpaque(false);

        JLabel iconLbl = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(StyleConfig.withAlpha(StyleConfig.ERROR_COLOR, 30));
                g2.fillOval(0, 0, 52, 52);
                UIUtils.drawTrashIcon(g2, 14, 14, 24, StyleConfig.PRIMARY_COLOR);
                g2.dispose();
            }
        };
        iconLbl.setPreferredSize(new Dimension(52, 52));
        centerPanel.add(iconLbl, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Confirm Deletion");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(StyleConfig.TEXT_COLOR);

        JLabel msgLbl = new JLabel("<html><p style='width:250px; line-height: 1.4;'>" + msg
                + " This action is permanent and cannot be undone.</p></html>");
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        msgLbl.setForeground(StyleConfig.TEXT_LIGHT);

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(msgLbl);
        centerPanel.add(textPanel, BorderLayout.CENTER);

        container.add(centerPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        class CustomButton extends JLabel {
            private static final long serialVersionUID = 1L;
            private final boolean isPrimary;
            private boolean hovered = false;

            public CustomButton(String text, boolean isPrimary) {
                super(text, SwingConstants.CENTER);
                this.isPrimary = isPrimary;
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(isPrimary ? Color.WHITE : StyleConfig.TEXT_LIGHT);
                setPreferredSize(new Dimension(90, 36));
                setCursor(new Cursor(Cursor.HAND_CURSOR));

                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isPrimary) {
                    g2.setColor(hovered ? new Color(190, 50, 55) : StyleConfig.PRIMARY_COLOR);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                } else {
                    g2.setColor(hovered ? new Color(255, 255, 255, 15) : new Color(255, 255, 255, 5));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 15, 15));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        }

        // Secondary button in delete confirmation dialog.
        CustomButton cancelBtn = new CustomButton("Cancel", false);
        cancelBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            // Button action: cancel delete confirmation.
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dialog.dispose();
            }
        });

        // Primary destructive button in delete confirmation dialog.
        CustomButton confirmBtn = new CustomButton("Delete", true);
        confirmBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            // Button action: confirm delete and close the dialog.
            public void mouseClicked(java.awt.event.MouseEvent e) {
                confirmed[0] = true;
                dialog.dispose();
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(confirmBtn);

        container.add(btnPanel, BorderLayout.SOUTH);
        dialog.add(container);

        dialog.setVisible(true);

        if (confirmed[0]) {
            if (item.getId() < 0) {
                new WatchlistDAO().removeFromWatchlist(Math.abs(item.getId()));
            } else {
                mediaService.deleteMedia(item.getId());
            }
            triggerRefresh();
        }
    }
    // End: delete confirmation button flow function.

    private void triggerRefresh() {
        Window window = SwingUtilities.getWindowAncestor(this);
        refreshComponentState();

        if (window instanceof com.playtrack.ui.main.MainFrame) {
            ((com.playtrack.ui.main.MainFrame) window).refreshAll();
        } else if (onRefresh != null) {
            onRefresh.run();
        }
    }

    private void refreshComponentState() {
        refreshReviewState();

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

    // Custom icon painting based on the check state (neutral.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = 18;
        Shape originalClip = g2.getClip();
        g2.clipRect(0, 0, w, h);

        g2.setColor(StyleConfig.withAlpha(Color.BLACK, hovered ? 42 : 26));
        g2.fill(new RoundRectangle2D.Float(0, 6, w, h - 4, arc, arc));

        if (hovered) {
            Color glowColor = getCategoryColor(item.getCategory());
            g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 58));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc + 2, arc + 2));
        }

        Shape clipShape = new RoundRectangle2D.Float(0, 0, w, h, arc, arc);
        g2.setClip(clipShape);

        if (posterImage != null) {
            g2.drawImage(posterImage, 0, 0, w, h, null);
        } else {

            Color catColor = getCategoryColor(item.getCategory());
            g2.setPaint(new GradientPaint(0, 0,
                    new Color(Math.max(catColor.getRed() - 45, 0), Math.max(catColor.getGreen() - 45, 0),
                            Math.max(catColor.getBlue() - 45, 0)),
                    0, h, new Color(Math.max(catColor.getRed() - 70, 0), Math.max(catColor.getGreen() - 70, 0),
                            Math.max(catColor.getBlue() - 70, 0))));
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(255, 255, 255, 30));
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawCategoryIcon(g2, item.getCategory(), w / 2, h / 2 - 20, false);
        }

        g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 18), 0, 40, new Color(255, 255, 255, 0)));
        g2.fillRect(0, 0, w, 40);

        // Keep the title visible on placeholder cards (no poster image):
        // below the icon at rest, and above the icon on hover.
        if (posterImage == null) {
            int iconCenterY = (h / 2) - 20;
            int placeholderTitleY = hovered ? iconCenterY - 38 : iconCenterY + 66;
            placeholderTitleY = Math.max(22, Math.min(h - 30, placeholderTitleY));
            drawCardTitle(g2, w, placeholderTitleY, w - 24, item.getTitle());
        }

        if (hovered) {
            Color catColor = getCategoryColor(item.getCategory());
            g2.setPaint(new GradientPaint(0, 0,
                    new Color(12, 16, 27, 182),
                    w, h, new Color(catColor.getRed(), catColor.getGreen(), catColor.getBlue(), 92)));
            g2.fillRect(0, 0, w, h);

            if (posterImage != null) {
                drawCardTitle(g2, w, 44, w - 24, item.getTitle());
            }
        }

        g2.setClip(originalClip);

        g2.setColor(new Color(255, 255, 255, hovered ? 116 : 42));
        g2.setStroke(new BasicStroke(hovered ? 1.6f : 1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));

        g2.dispose();
    }

    private void drawCardTitle(Graphics2D g2, int cardWidth, int startY, int maxWidth, String rawTitle) {
        String title = rawTitle == null ? "" : rawTitle.trim();
        if (title.isEmpty()) {
            return;
        }
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();

        java.util.List<String> lines = new java.util.ArrayList<>();
        if (fm.stringWidth(title) <= maxWidth) {
            lines.add(title);
        } else {
            String[] words = title.split("\\s+");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (fm.stringWidth(candidate) <= maxWidth) {
                    line.setLength(0);
                    line.append(candidate);
                } else {
                    if (line.length() == 0) {
                        lines.add(trimToWidth(word, fm, maxWidth));
                    } else {
                        lines.add(line.toString());
                        line.setLength(0);
                        line.append(word);
                    }
                }
                if (lines.size() == 2) {
                    break;
                }
            }
            if (lines.size() < 2 && line.length() > 0) {
                lines.add(line.toString());
            }
        }

        if (lines.isEmpty()) {
            return;
        }
        if (lines.size() > 2) {
            lines = lines.subList(0, 2);
        }
        if (lines.size() == 2) {
            String second = lines.get(1);
            if (fm.stringWidth(second) > maxWidth) {
                lines.set(1, trimToWidth(second, fm, maxWidth));
            }
        }

        int lineHeight = fm.getHeight();
        int y = (lines.size() == 1) ? startY : startY - (lineHeight / 2);
        for (String line : lines) {
            int x = (cardWidth - fm.stringWidth(line)) / 2;
            g2.drawString(line, x, y);
            y += lineHeight;
        }
    }

    // Utility method to trim a string.
    private String trimToWidth(String value, FontMetrics fm, int maxWidth) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (fm.stringWidth(value) <= maxWidth) {
            return value;
        }
        String ellipsis = "...";
        int limit = Math.max(1, value.length() - 1);
        while (limit > 1 && fm.stringWidth(value.substring(0, limit) + ellipsis) > maxWidth) {
            limit--;
        }
        return value.substring(0, limit) + ellipsis;
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
