package com.playtrack.ui.review;

import com.playtrack.model.MediaItem;
import com.playtrack.model.Review;
import com.playtrack.service.MediaService;
import com.playtrack.ui.components.*;
import com.playtrack.util.SessionManager;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class ReviewFormDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private MediaService mediaService = new MediaService();
    private String category;
    private JTextField titleField;
    private JTextField authorField;
    private JComboBox<String> genreBox;
    private JTextField customGenreField;
    private JPanel customGenreWrapper;
    private JTextField dateField;
    private JTextArea reviewArea;
    private StarRating starRating;
    private JToggleButton favoriteToggle;
    private Runnable onSave;
    private JPanel posterPanel;
    private BufferedImage selectedImage = null;
    private String selectedImagePath = null;
    private MediaItem editItem = null;
    private boolean isWatchlist = false;

    
    private static final Color BG_DARK = new Color(0x181A2F);
    private static final Color BG_FIELD = new Color(0x242E49);
    private static final Color BORDER_SUBTLE = new Color(0x37415C);
    private static final Color INPUT_TEXT_COLOR = Color.WHITE;
    private static final int REVIEW_CHAR_LIMIT = 500;
    private static final String CUSTOM_GENRE_OPTION = "Others";

    private void setLimit(javax.swing.text.JTextComponent comp, int limit) {
        ((AbstractDocument) comp.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string != null && fb.getDocument().getLength() + string.length() <= limit)
                    super.insertString(fb, offset, string, attr);
            }
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text != null && fb.getDocument().getLength() + text.length() - length <= limit)
                    super.replace(fb, offset, length, text, attrs);
            }
        });
    }

    private JPanel createLabeledField(String labelText, JComponent field, Color catColor) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(getReadableAccentColor(catColor));
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private Color getReadableAccentColor(Color base) {
        
        int r = (int) (base.getRed() * 0.6 + 255 * 0.4);
        int g = (int) (base.getGreen() * 0.6 + 255 * 0.4);
        int b = (int) (base.getBlue() * 0.6 + 255 * 0.4);
        return new Color(Math.min(255, r), Math.min(255, g), Math.min(255, b));
    }

    private Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    
    
    
    public ReviewFormDialog(Frame parent, String defaultCategory, Runnable onSave) {
        super(parent, true);
        this.onSave = onSave;
        this.isWatchlist = (defaultCategory != null && defaultCategory.equals("Watchlist"));
        this.category = (defaultCategory != null && !defaultCategory.equals("Watchlist")) ? defaultCategory : "Films";

        int dialogWidth = isWatchlist ? 440 : 470;
        setSize(dialogWidth, isWatchlist ? 390 : 580);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        final Color catColor = getCategoryColor(category);

        
        JPanel mainCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int arc = 16;
                int inset = 8;

                
                for (int i = 4; i >= 1; i--) {
                    g2.setColor(new Color(catColor.getRed(), catColor.getGreen(), catColor.getBlue(), 6 * i));
                    g2.setStroke(new BasicStroke(i * 2.5f));
                    g2.draw(new RoundRectangle2D.Float(inset - i, inset - i, w - (inset - i) * 2, h - (inset - i) * 2, arc + i * 2, arc + i * 2));
                }

                
                g2.setColor(BG_DARK);
                g2.fill(new RoundRectangle2D.Float(inset, inset, w - inset * 2, h - inset * 2, arc, arc));

                
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(catColor.getRed(), catColor.getGreen(), catColor.getBlue(), 120));
                g2.draw(new RoundRectangle2D.Float(inset + 0.5f, inset + 0.5f, w - inset * 2 - 1, h - inset * 2 - 1, arc, arc));

                
                int cx = w / 2;
                int iconTop = inset - 4;
                
                g2.setColor(BG_DARK);
                g2.fillRect(cx - 30, iconTop - 10, 60, 48);

                String iconCat = isWatchlist ? "Watchlist" : category;
                UIUtils.drawCategoryIcon(g2, iconCat, cx, iconTop + 10, 36, catColor);

                g2.dispose();
            }
        };
        mainCard.setOpaque(false);
        mainCard.setBorder(BorderFactory.createEmptyBorder(28, 28, 24, 28));

        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        
        JPanel topSection = new JPanel(new BorderLayout(16, 0));
        topSection.setOpaque(false);
        topSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        
        posterPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                int w = getWidth(), h = getHeight();

                if (selectedImage != null) {
                    g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
                    double scale = Math.max((double) w / selectedImage.getWidth(), (double) h / selectedImage.getHeight());
                    int dw = (int) (selectedImage.getWidth() * scale);
                    int dh = (int) (selectedImage.getHeight() * scale);
                    g2.drawImage(selectedImage, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
                } else {
                    
                    g2.setColor(BG_FIELD);
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
                    g2.setColor(new Color(catColor.getRed(), catColor.getGreen(), catColor.getBlue(), 50));
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 12, 12));

                    
                    int cx = w / 2, cy = h / 2 - 34;
                    String iconCat = isWatchlist ? "Films" : category;
                    Color iconColor = withAlpha(getReadableAccentColor(catColor), 185);
                    UIUtils.drawCategoryIcon(g2, iconCat, cx, cy, 58, iconColor);

                    
                    g2.setColor(withAlpha(getReadableAccentColor(catColor), 220));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    FontMetrics fm = g2.getFontMetrics();
                    String line1 = "UPLOAD";
                    String line2 = category.equals("Books") ? "COVER" : (category.equals("Games") ? "BOX ART" : "POSTER");
                    int line1Y = cy + 30;
                    int line2Y = line1Y + fm.getHeight() - 2;
                    g2.drawString(line1, (w - fm.stringWidth(line1)) / 2, line1Y);
                    g2.drawString(line2, (w - fm.stringWidth(line2)) / 2, line2Y);

                    
                    g2.setColor(new Color(255, 255, 255, 140));
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int py = line2Y + 18;
                    g2.drawLine(cx - 7, py, cx + 7, py);
                    g2.drawLine(cx, py - 7, cx, py + 7);

                    String enteredTitle = (titleField != null && titleField.getText() != null)
                        ? titleField.getText().trim()
                        : "";
                    if (!enteredTitle.isEmpty()) {
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        FontMetrics tfm = g2.getFontMetrics();
                        int maxTextWidth = w - 20;
                        String displayTitle = enteredTitle;
                        while (displayTitle.length() > 1 && tfm.stringWidth(displayTitle + "...") > maxTextWidth) {
                            displayTitle = displayTitle.substring(0, displayTitle.length() - 1);
                        }
                        if (!displayTitle.equals(enteredTitle)) {
                            displayTitle = displayTitle + "...";
                        }

                        int tx = (w - tfm.stringWidth(displayTitle)) / 2;
                        int ty = h - 14;
                        int bgX = tx - 6;
                        int bgY = ty - tfm.getAscent() - 3;
                        int bgW = tfm.stringWidth(displayTitle) + 12;
                        int bgH = tfm.getHeight() + 4;

                        g2.setColor(new Color(0, 0, 0, 90));
                        g2.fillRoundRect(bgX, bgY, bgW, bgH, 10, 10);
                        g2.setColor(new Color(255, 255, 255, 215));
                        g2.drawString(displayTitle, tx, ty);
                    }
                }
                g2.dispose();
            }
        };
        posterPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        posterPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Button action: choose cover/poster image for this media item.
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif", "webp"));
                if (chooser.showOpenDialog(ReviewFormDialog.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        java.io.File file = chooser.getSelectedFile();
                        selectedImagePath = file.getAbsolutePath();
                        selectedImage = ImageIO.read(file);
                        posterPanel.repaint();
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }
        });
        int posterW = isWatchlist ? 170 : 160;
        int posterH = isWatchlist ? 255 : 240;
        posterPanel.setPreferredSize(new Dimension(posterW, posterH));
        posterPanel.setMinimumSize(new Dimension(posterW, posterH));
        posterPanel.setMaximumSize(new Dimension(posterW, posterH));
        posterPanel.setOpaque(false);
        topSection.add(posterPanel, BorderLayout.WEST);

        
        JPanel meta = new JPanel();
        meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));
        meta.setOpaque(false);

        
        String titleLabelText = category.equals("Books") ? "Book Title" : (category.equals("Games") ? "Game Title" : "Film Title");
        String titlePlaceholder = category.equals("Books") ? "Enter book title..." : (category.equals("Games") ? "Enter game title..." : "Enter film title...");
        titleField = createField(titlePlaceholder, catColor);
        setLimit(titleField, 100);
        titleField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { posterPanel.repaint(); }
            public void removeUpdate(DocumentEvent e) { posterPanel.repaint(); }
            public void changedUpdate(DocumentEvent e) { posterPanel.repaint(); }
        });
        addFieldToPanel(meta, titleLabelText, titleField, catColor);

        
        if (category.equals("Books")) {
            authorField = createField("Enter author name...", catColor);
            setLimit(authorField, 70);
            addFieldToPanel(meta, "Author Name", authorField, catColor);
        }

        
        String[] baseGenres = getGenresForCategory(category);
        String[] genreOptions = java.util.Arrays.copyOf(baseGenres, baseGenres.length + 1);
        genreOptions[baseGenres.length] = CUSTOM_GENRE_OPTION;
        // Genre dropdown with an "Others" option for custom input.
        genreBox = new JComboBox<>(genreOptions);
        genreBox.setMaximumRowCount(15);
        styleComboBox(genreBox, catColor);
        // Button/dropdown action: show custom genre input when needed.
        genreBox.addActionListener(e -> toggleCustomGenreInput());
        
        addFieldToPanel(meta, "Genre", genreBox, catColor);

        customGenreField = createField("Enter custom genre...", catColor);
        setLimit(customGenreField, 50);
        JPanel customGenrePanel = createLabeledField("Custom Genre", customGenreField, catColor);
        customGenrePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        customGenrePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        customGenreWrapper = new JPanel(new BorderLayout());
        customGenreWrapper.setOpaque(false);
        customGenreWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        customGenreWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        customGenreWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        customGenreWrapper.add(customGenrePanel, BorderLayout.CENTER);
        customGenreWrapper.setVisible(false);
        meta.add(customGenreWrapper);

        
        if (!isWatchlist) {
            String dateLabelText = category.equals("Games") ? "Played On" : (category.equals("Books") ? "Finished On" : "Watched On");
            String datePlaceholder = category.equals("Games") ? "Select played date." : 
                                    (category.equals("Books") ? "Select finished date." : 
                                    "Select watch date.");
            // Calendar/date picker field used for watched/played/finished dates.
            dateField = new DatePickerField(datePlaceholder);
            dateField.setForeground(INPUT_TEXT_COLOR);
            dateField.setCaretColor(INPUT_TEXT_COLOR);
            dateField.setFont(new Font("Arial", Font.PLAIN, 12));
            addFieldToPanel(meta, dateLabelText, dateField, catColor);
        }

        
        if (!isWatchlist) {
            // Compact interaction row for rating stars and favorite-heart toggle.
            meta.add(Box.createVerticalStrut(4));
            JPanel ratingRow = new JPanel(new GridBagLayout());
            ratingRow.setOpaque(false);
            ratingRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            ratingRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            GridBagConstraints rc = new GridBagConstraints();
            rc.gridy = 0;
            rc.anchor = GridBagConstraints.CENTER;

            rc.gridx = 0;
            rc.insets = new Insets(0, 0, 0, 4);
            JLabel rLbl = new JLabel("Rating");
            rLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            rLbl.setForeground(getReadableAccentColor(catColor));
            ratingRow.add(rLbl, rc);

            rc.gridx = 1;
            rc.insets = new Insets(0, 0, 0, 12);
            starRating = new StarRating(0, true, 16);
            Dimension starSize = starRating.getPreferredSize();
            starRating.setMinimumSize(starSize);
            starRating.setPreferredSize(starSize);
            starRating.setMaximumSize(starSize);
            ratingRow.add(starRating, rc);

            rc.gridx = 2;
            rc.weightx = 1.0;
            rc.fill = GridBagConstraints.HORIZONTAL;
            rc.insets = new Insets(0, 0, 0, 0);
            ratingRow.add(Box.createHorizontalGlue(), rc);

            rc.gridx = 3;
            rc.weightx = 0;
            rc.fill = GridBagConstraints.NONE;
            rc.insets = new Insets(0, 0, 0, 6);
            JLabel fLbl = new JLabel("Favorite");
            fLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            fLbl.setForeground(getReadableAccentColor(catColor));
            ratingRow.add(fLbl, rc);

            rc.gridx = 4;
            rc.insets = new Insets(0, 0, 0, 0);
            favoriteToggle = createHeartToggle(getReadableAccentColor(catColor));
            ratingRow.add(favoriteToggle, rc);

            meta.add(ratingRow);
        }


        topSection.add(meta, BorderLayout.CENTER);

        int topHeight = isWatchlist ? 290 : 260; 
        topSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, topHeight));
        topSection.setPreferredSize(new Dimension(Integer.MAX_VALUE, topHeight));
        content.add(topSection);
        content.add(Box.createVerticalStrut(16));

        
        if (!isWatchlist) {
            String reviewTitle = category.equals("Books") ? "Your Thoughts" :
                                 (category.equals("Games") ? "Gameplay Experience" : "Your Review");
            JLabel revLabel = new JLabel(reviewTitle);
            revLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            revLabel.setForeground(getReadableAccentColor(catColor));
            JLabel reviewCountLabel = new JLabel("0/" + REVIEW_CHAR_LIMIT);
            reviewCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            reviewCountLabel.setForeground(new Color(255, 255, 255, 170));

            JPanel reviewHeader = new JPanel(new BorderLayout());
            reviewHeader.setOpaque(false);
            reviewHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            reviewHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            reviewHeader.add(revLabel, BorderLayout.WEST);
            reviewHeader.add(reviewCountLabel, BorderLayout.EAST);
            content.add(reviewHeader);
            content.add(Box.createVerticalStrut(6));

            
            reviewArea = new JTextArea(5, 20) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (getText().isEmpty() && !isFocusOwner()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        g2.setColor(new Color(255, 255, 255, 120));
                        FontMetrics fm = g2.getFontMetrics();
                        String ph = "Enter your thoughts here...";
                        g2.drawString(ph, getInsets().left, getInsets().top + fm.getAscent());
                        g2.dispose();
                    }
                }
            };
            reviewArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            reviewArea.setLineWrap(true);
            reviewArea.setWrapStyleWord(true);
            reviewArea.setBackground(BG_FIELD);
            reviewArea.setForeground(INPUT_TEXT_COLOR);
            reviewArea.setCaretColor(INPUT_TEXT_COLOR);
            reviewArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            setLimit(reviewArea, REVIEW_CHAR_LIMIT);

            reviewArea.getDocument().addDocumentListener(new DocumentListener() {
                private void updateCount() {
                    int count = reviewArea.getText() != null ? reviewArea.getText().length() : 0;
                    reviewCountLabel.setText(count + "/" + REVIEW_CHAR_LIMIT);
                    if (count >= REVIEW_CHAR_LIMIT) {
                        reviewCountLabel.setForeground(StyleConfig.ERROR_COLOR);
                    } else {
                        reviewCountLabel.setForeground(new Color(255, 255, 255, 170));
                    }
                }

                public void insertUpdate(DocumentEvent e) { updateCount(); }
                public void removeUpdate(DocumentEvent e) { updateCount(); }
                public void changedUpdate(DocumentEvent e) { updateCount(); }
            });

            reviewArea.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) { repaint(); }
                @Override public void focusLost(java.awt.event.FocusEvent e) { repaint(); }
            });

            JScrollPane reviewScroll = new JScrollPane(reviewArea);
            reviewScroll.setOpaque(false);
            reviewScroll.getViewport().setOpaque(false);
            reviewScroll.setBorder(null);
            reviewScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            
            JPanel reviewWrapper = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    g2.setColor(BG_FIELD);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    
                    g2.setColor(BORDER_SUBTLE);
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
                    g2.dispose();
                }
            };
            reviewWrapper.setOpaque(false);
            reviewWrapper.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            reviewWrapper.add(reviewScroll, BorderLayout.CENTER);
            reviewWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
            reviewWrapper.setPreferredSize(new Dimension(380, 200));
            reviewWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            content.add(reviewWrapper);
        }

        
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        
        // Secondary cancel button that closes the dialog without saving.
        JPanel cancelBtn = new JPanel() {
            private boolean hover = false;
            {
                setOpaque(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(90, 32));
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hover = true; repaint(); }
                    public void mouseExited(java.awt.event.MouseEvent e) { hover = false; repaint(); }
                    // Button action: close the review dialog without saving.
                    public void mouseClicked(java.awt.event.MouseEvent e) { dispose(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                
                g2.setColor(hover ? new Color(45, 52, 70) : new Color(35, 42, 58));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
                
                g2.setColor(new Color(90, 100, 120));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 12, 12));
                
                g2.setColor(StyleConfig.TEXT_COLOR);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth("Cancel")) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString("Cancel", tx, ty);
                g2.dispose();
            }
        };
        actionRow.add(cancelBtn);

        
        // Primary save button that submits media and review changes.
        RoundedButton saveBtn = new RoundedButton("Save", catColor, 12);
        saveBtn.setPreferredSize(new Dimension(90, 32));
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.setForeground(Color.WHITE);
        // Button action: save media/review form data.
        saveBtn.addActionListener(e -> save());
        actionRow.add(saveBtn);

        mainCard.add(content, BorderLayout.CENTER);
        mainCard.add(actionRow, BorderLayout.SOUTH);
        add(mainCard, BorderLayout.CENTER);
    }

    
    
    
    public ReviewFormDialog(Frame parent, MediaItem item, Runnable onSave) {
        this(parent, item.getCategory(), onSave);
        this.editItem = item;
        titleField.setText(item.getTitle());
        if (category.equals("Books") && item.getAuthor() != null && authorField != null) {
            authorField.setText(item.getAuthor());
        }
        applySavedGenre(item.getGenre());
        if (item.getImagePath() != null) {
            try {
                selectedImagePath = item.getImagePath();
                selectedImage = ImageIO.read(new java.io.File(item.getImagePath()));
                posterPanel.repaint();
            } catch (Exception e) {}
        }
        Review r = mediaService.getReviewByMedia(item.getId());
        if (r != null) {
            if (starRating != null) starRating.setRating(r.getRating());
            if (reviewArea != null) reviewArea.setText(r.getReviewText());
            if (favoriteToggle != null && r.isFavorite() && !favoriteToggle.isSelected()) {
                favoriteToggle.doClick();
            }
            if (r.getWatchDate() != null) {
                dateField.setText(r.getWatchDate());
            }
        }


    }

    // Start: favorite heart toggle button function.
    private JToggleButton createHeartToggle(final Color accentColor) {
        JToggleButton btn = new JToggleButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = isSelected();
                g2.setColor(sel ? accentColor : new Color(170, 180, 195));

                int w = getWidth(), h = getHeight();
                double cx = w / 2.0, cy = h / 2.0;
                double s = Math.min(w, h) * 0.38;

                Path2D.Double heart = new Path2D.Double();
                heart.moveTo(cx, cy + s * 0.7);
                heart.curveTo(cx - s * 1.2, cy - s * 0.1, cx - s * 0.9, cy - s * 1.0, cx, cy - s * 0.35);
                heart.curveTo(cx + s * 0.9, cy - s * 1.0, cx + s * 1.2, cy - s * 0.1, cx, cy + s * 0.7);
                heart.closePath();

                if (sel) {
                    g2.fill(heart);
                } else {
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(heart);
                }
                g2.dispose();
            }
        };
        Dimension heartSize = new Dimension(24, 24);
        btn.setMinimumSize(heartSize);
        btn.setPreferredSize(heartSize);
        btn.setMaximumSize(heartSize);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    // End: favorite heart toggle button function.

    
    private void addFieldToPanel(JPanel panel, String label, JComponent field, Color catColor) {
        JPanel p = createLabeledField(label, field, catColor);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel.add(p);
        panel.add(Box.createVerticalStrut(6));
    }

    
    private JTextField createField(String placeholder, Color catColor) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner() && placeholder != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 120));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            }
        };
        field.setPreferredSize(new Dimension(200, 28));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setBackground(BG_FIELD);
        field.setForeground(INPUT_TEXT_COLOR);
        field.setCaretColor(INPUT_TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
            BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { repaint(); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { repaint(); }
        });

        return field;
    }

    
    private void setupStyledComboBox(JComboBox<String> box, Color catColor) {
        styleComboBox(box, catColor);
    }

    private void styleComboBox(JComboBox<String> box, Color catColor) {
        // Shared dropdown styling for review form combo boxes.
        box.setPreferredSize(new Dimension(200, 28));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        box.setFont(new Font("Arial", Font.PLAIN, 12));
        box.setBackground(BG_FIELD);
        box.setForeground(INPUT_TEXT_COLOR);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setOpaque(true);
                list.setBackground(BG_FIELD);
                c.setBackground(isSelected ? new Color(50, 60, 80) : BG_FIELD);
                c.setForeground(INPUT_TEXT_COLOR);
                return c;
            }
        });
        
        Object popup = box.getUI().getAccessibleChild(box, 0);
        if (popup instanceof javax.swing.JPopupMenu) {
            ((javax.swing.JPopupMenu) popup).setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1));
            for (Component c : ((javax.swing.JPopupMenu) popup).getComponents()) {
                if (c instanceof JScrollPane) {
                    ((JScrollPane) c).getViewport().setBackground(BG_FIELD);
                }
            }
        }
    }

    private void toggleCustomGenreInput() {
        boolean showCustomGenre = genreBox != null
            && CUSTOM_GENRE_OPTION.equals(String.valueOf(genreBox.getSelectedItem()));
        if (customGenreWrapper != null) {
            customGenreWrapper.setVisible(showCustomGenre);
            customGenreWrapper.revalidate();
            customGenreWrapper.repaint();
        }
        if (showCustomGenre && customGenreField != null) {
            SwingUtilities.invokeLater(() -> customGenreField.requestFocusInWindow());
        }
    }

    private void applySavedGenre(String savedGenre) {
        if (genreBox == null) {
            return;
        }
        String normalized = savedGenre != null ? savedGenre.trim() : "";
        if (normalized.isEmpty()) {
            toggleCustomGenreInput();
            return;
        }

        ComboBoxModel<String> model = genreBox.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            String option = model.getElementAt(i);
            if (!CUSTOM_GENRE_OPTION.equals(option) && option.equalsIgnoreCase(normalized)) {
                genreBox.setSelectedIndex(i);
                toggleCustomGenreInput();
                return;
            }
        }

        genreBox.setSelectedItem(CUSTOM_GENRE_OPTION);
        if (customGenreField != null) {
            customGenreField.setText(normalized);
        }
        toggleCustomGenreInput();
    }

    private String resolveSelectedGenre() {
        Object selected = genreBox != null ? genreBox.getSelectedItem() : null;
        String selectedGenre = selected != null ? selected.toString().trim() : "";
        if (!CUSTOM_GENRE_OPTION.equals(selectedGenre)) {
            return selectedGenre;
        }
        return customGenreField != null ? customGenreField.getText().trim() : "";
    }

    private String[] getGenresForCategory(String category) {
        if (category.equals("Games")) return new String[]{
            "Action", "Action-Adventure", "Adventure", "Battle Royale", "Beat 'em Up",
            "Card Game", "City Builder", "CRPG", "Dating Sim", "Dungeon Crawler",
            "Educational", "Fighting", "FPS", "Hack and Slash", "Horror",
            "Idle", "JRPG", "Metroidvania", "MMORPG", "MOBA",
            "Music / Rhythm", "Open World", "Party", "Platformer", "Puzzle",
            "Racing", "Real-Time Strategy", "Roguelike", "Roguelite", "RPG",
            "Sandbox", "Shooter", "Simulation", "Social Deduction", "Souls-like",
            "Sports", "Stealth", "Strategy", "Survival", "Survival Horror",
            "Tactical RPG", "Third-Person Shooter", "Tower Defense", "Turn-Based Strategy",
            "Visual Novel", "Walking Simulator"
        };
        if (category.equals("Books")) return new String[]{
            "Action / Adventure", "Autobiography", "Biography", "Business",
            "Children's", "Classic", "Comedy / Humor", "Coming-of-Age", "Cookbook",
            "Crime", "Dystopian", "Epic", "Erotica", "Essay",
            "Fairy Tale", "Fantasy", "Graphic Novel", "Health / Wellness",
            "Historical Fiction", "History", "Horror", "Inspirational / Motivational",
            "Literary Fiction", "Manga", "Memoir", "Mystery",
            "Mythology", "Non-Fiction", "Paranormal", "Philosophy",
            "Poetry", "Political", "Psychology", "Religion / Spirituality",
            "Romance", "Satire", "Science", "Science Fiction",
            "Self-Help", "Short Stories", "Thriller", "Travel",
            "True Crime", "Western", "Young Adult"
        };
        
        return new String[]{
            "Action", "Adventure", "Animation", "Anime", "Biography",
            "Comedy", "Crime", "Dark Comedy", "Documentary", "Drama",
            "Epic", "Experimental", "Family", "Fantasy", "Film Noir",
            "Historical", "Horror", "Indie", "Martial Arts", "Musical",
            "Mystery", "Neo-Noir", "Political", "Romance", "Romantic Comedy",
            "Satire", "Sci-Fi", "Slasher", "Sports", "Superhero",
            "Supernatural", "Suspense", "Thriller", "War", "Western"
        };
    }

    private Color getCategoryColor(String cat) {
        if (isWatchlist) return StyleConfig.WATCHLIST_COLOR;
        if (cat.equals("Games")) return StyleConfig.GAME_COLOR;
        if (cat.equals("Books")) return StyleConfig.BOOK_COLOR;
        return StyleConfig.FILM_COLOR;
    }

    private void save() {
        String title = titleField.getText();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a title");
            return;
        }
        String selectedGenre = resolveSelectedGenre();
        if (selectedGenre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select or enter a genre");
            return;
        }

        MediaItem item = (editItem != null) ? editItem : new MediaItem();
        item.setUserId(SessionManager.getCurrentUser().getId());
        item.setTitle(title);
        item.setCategory(category);
        item.setGenre(selectedGenre);

        if (selectedImagePath != null) {
            item.setImagePath(selectedImagePath);
        }
        if (category.equals("Books") && authorField != null) {
            item.setAuthor(authorField.getText());
        }

        if (editItem == null) {
            int mediaId = mediaService.addMedia(item);
            if (mediaId != -1) { saveReviewAndClose(mediaId); }
        } else {
            mediaService.updateMedia(item);
            saveReviewAndClose(item.getId());
        }
    }

    private void saveReviewAndClose(int mediaId) {
        Review review = new Review();
        review.setMediaId(mediaId);
        review.setUserId(SessionManager.getCurrentUser().getId());
        review.setRating(starRating != null ? starRating.getRating() : 0.0);
        review.setReviewText(reviewArea != null ? reviewArea.getText() : "");
        review.setFavorite(favoriteToggle != null ? favoriteToggle.isSelected() : false);
        review.setWatchDate(dateField != null ? dateField.getText() : null);
        review.setWatchlist(isWatchlist);
        mediaService.addOrUpdateReview(review);

        onSave.run();
        dispose();
    }
}
