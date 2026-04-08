package com.playtrack.ui.components;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class UIUtils {

    // Cached PNG icons map
    private static final Map<String, BufferedImage> iconCache = new HashMap<>();
    private static BufferedImage authBackgroundImage;

    private static synchronized BufferedImage getIcon(String name) {
        if (!iconCache.containsKey(name)) {
            try {
                InputStream is = UIUtils.class.getClassLoader().getResourceAsStream("resources/icons/" + name + ".png");
                if (is != null) {
                    BufferedImage img = ImageIO.read(is);
                    iconCache.put(name, img);
                    is.close();
                } else {
                    java.io.File f = new java.io.File("src/resources/icons/" + name + ".png");
                    if (f.exists()) {
                        BufferedImage img = ImageIO.read(f);
                        iconCache.put(name, img);
                    } else {
                        System.err.println("Could not find icon resource: " + name);
                        iconCache.put(name, null);
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not load icon: " + name + " - " + e.getMessage());
                iconCache.put(name, null);
            }
        }
        return iconCache.get(name);
    }

    private static synchronized BufferedImage getAuthBackgroundImage() {
        if (authBackgroundImage != null) {
            return authBackgroundImage;
        }
        try {
            InputStream is = UIUtils.class.getClassLoader().getResourceAsStream("resources/auth_bg.png");
            if (is != null) {
                authBackgroundImage = ImageIO.read(is);
                is.close();
                return authBackgroundImage;
            }
            java.io.File local = new java.io.File("src/resources/auth_bg.png");
            if (local.exists()) {
                authBackgroundImage = ImageIO.read(local);
            }
        } catch (Exception ignored) {
            authBackgroundImage = null;
        }
        return authBackgroundImage;
    }

    public static void paintFadedAuthBackground(Graphics2D g2, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        BufferedImage bgImage = getAuthBackgroundImage();

        if (bgImage != null) {
            double panelRatio = (double) width / height;
            double imgRatio = (double) bgImage.getWidth() / bgImage.getHeight();

            int drawW;
            int drawH;
            int drawX;
            int drawY;

            if (panelRatio > imgRatio) {
                drawW = width;
                drawH = (int) (width / imgRatio);
                drawX = 0;
                drawY = (height - drawH) / 2;
            } else {
                drawH = height;
                drawW = (int) (height * imgRatio);
                drawX = (width - drawW) / 2;
                drawY = 0;
            }

            Composite oldComposite = g2.getComposite();
            // Keep image visible but still subtle.
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.34f));
            g2.drawImage(bgImage, drawX, drawY, drawW, drawH, null);
            g2.setComposite(oldComposite);
        }

        g2.setPaint(new GradientPaint(
                0, 0, new Color(StyleConfig.BACKGROUND_COLOR.getRed(), StyleConfig.BACKGROUND_COLOR.getGreen(), StyleConfig.BACKGROUND_COLOR.getBlue(), 138),
                0, height, new Color(StyleConfig.BACKGROUND_LIGHT.getRed(), StyleConfig.BACKGROUND_LIGHT.getGreen(), StyleConfig.BACKGROUND_LIGHT.getBlue(), 172)));
        g2.fillRect(0, 0, width, height);

        g2.setPaint(new GradientPaint(
                0, 0, new Color(StyleConfig.PALETTE_WINE.getRed(), StyleConfig.PALETTE_WINE.getGreen(), StyleConfig.PALETTE_WINE.getBlue(), 28),
                width, 0, new Color(StyleConfig.PALETTE_PEACH.getRed(), StyleConfig.PALETTE_PEACH.getGreen(), StyleConfig.PALETTE_PEACH.getBlue(), 22)));
        g2.fillRect(0, 0, width, height);

        g2.setPaint(new GradientPaint(
                0, 0, new Color(StyleConfig.BACKGROUND_COLOR.getRed(), StyleConfig.BACKGROUND_COLOR.getGreen(), StyleConfig.BACKGROUND_COLOR.getBlue(), 10),
                width, 0, new Color(StyleConfig.BACKGROUND_COLOR.getRed(), StyleConfig.BACKGROUND_COLOR.getGreen(), StyleConfig.BACKGROUND_COLOR.getBlue(), 70)));
        g2.fillRect(0, 0, width, height);
    }

    /**
     * Draws a tinted version of a BufferedImage centered at (cx, cy) with the given size.
     */
    private static void drawTintedIcon(Graphics2D g2, BufferedImage src, int cx, int cy, int size, Color color) {
        if (src == null) return;
        // Scale to target size
        double srcW = src.getWidth();
        double srcH = src.getHeight();
        double aspectScale = Math.min((double)size / srcW, (double)size / srcH);
        int drawW = Math.max((int) Math.round(srcW * aspectScale), 1);
        int drawH = Math.max((int) Math.round(srcH * aspectScale), 1);

        // Create tinted + scaled copy
        java.awt.Image scaledSrc = src.getScaledInstance(drawW, drawH, java.awt.Image.SCALE_SMOOTH);
        BufferedImage tinted = new BufferedImage(drawW, drawH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tg = tinted.createGraphics();
        tg.drawImage(scaledSrc, 0, 0, null);
        tg.dispose();

        // Tint pixels
        int tintRGB = (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        int[] pixels =  tinted.getRGB(0, 0, drawW, drawH, null, 0, drawW);
        for (int i = 0; i < pixels.length; i++) {
            int alpha = (pixels[i] >> 24) & 0xFF;
            if (alpha > 0) {
                pixels[i] = (alpha << 24) | tintRGB;
            }
        } 
        tinted.setRGB(0, 0, drawW, drawH, pixels, 0, drawW);

        // Draw centered
        g2.drawImage(tinted, cx - drawW / 2, cy - drawH / 2, null);
    }

    /**
     * Draws a tinted + rotated version of a BufferedImage centered at (cx, cy) with the given size.
     */
    private static void drawTintedRotatedIcon(Graphics2D g2, BufferedImage src, int cx, int cy, int size, Color color, double angleRadians) {
        if (src == null) return;
        // Scale to target size
        double srcW = src.getWidth();
        double srcH = src.getHeight();
        double aspectScale = Math.min((double)size / srcW, (double)size / srcH);
        int drawW = Math.max((int) Math.round(srcW * aspectScale), 1);
        int drawH = Math.max((int) Math.round(srcH * aspectScale), 1);

        // Create tinted + scaled copy
        java.awt.Image scaledSrc = src.getScaledInstance(drawW, drawH, java.awt.Image.SCALE_SMOOTH);
        BufferedImage tinted = new BufferedImage(drawW, drawH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tg = tinted.createGraphics();
        tg.drawImage(scaledSrc, 0, 0, null);
        tg.dispose();

        // Tint pixels
        int tintRGB = (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        int[] pixels = tinted.getRGB(0, 0, drawW, drawH, null, 0, drawW);
        for (int i = 0; i < pixels.length; i++) {
            int alpha = (pixels[i] >> 24) & 0xFF;
            if (alpha > 0) {
                pixels[i] = (alpha << 24) | tintRGB;
            }
        }
        tinted.setRGB(0, 0, drawW, drawH, pixels, 0, drawW);

        // Draw centered with rotation
        Graphics2D rg = (Graphics2D) g2.create();
        rg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        rg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rg.translate(cx, cy);
        rg.rotate(angleRadians);
        rg.drawImage(tinted, -drawW / 2, -drawH / 2, null);
        rg.dispose();
    }
    
    public static void drawCloseIcon(Graphics2D g2, int x, int y, int size, Color color, float strokeWidth) {
        BufferedImage src = getIcon("close");
        if (src != null) {
            drawTintedIcon(g2, src, x + size / 2, y + size / 2, size, color);
        }
    }

    public static void drawEyeIcon(Graphics2D g2, int x, int y, int width, int height, Color color, boolean visible) {
        BufferedImage src = getIcon(visible ? "eye" : "eye_crossed");
        if (src != null) {
            int cx = x + width / 2;
            int cy = y + height / 2;
            int size = 20; // 20 matches previous hardcoded target dimensions
            drawTintedIcon(g2, src, cx, cy, size, color);
        }
    }

    public static void drawTrashIcon(Graphics2D g2, int x, int y, int size, Color color) {
        BufferedImage src = getIcon("trash");
        if (src != null) {
            drawTintedIcon(g2, src, x + size / 2, y + size / 2, size, color);
        }
    }

    public static void drawArrowIcon(Graphics2D g2, int x, int y, int width, int height, Color color, boolean right) {
        BufferedImage src = getIcon(right ? "arrow_right" : "arrow_left");
        if (src != null) {
            int cx = x + width / 2;
            int cy = y + height / 2;
            int size = Math.max(Math.min(width, height) * 2 / 3, 16);
            drawTintedIcon(g2, src, cx, cy, size, color);
        }
    }

    public static void drawVerticalArrowIcon(Graphics2D g2, int x, int y, int width, int height, Color color, boolean down) {
        BufferedImage src = getIcon("arrow_right");
        if (src != null) {
            int cx = x + width / 2;
            int cy = y + height / 2;
            int size = Math.max(Math.min(width, height) * 2 / 3, 16);
            double angle = down ? Math.PI / 2 : -Math.PI / 2;
            drawTintedRotatedIcon(g2, src, cx, cy, size, color, angle);
        }
    }

    public static void drawArrowIcon(Graphics2D g2, int x, int y, int size, Color color, boolean right) {
        drawArrowIcon(g2, x, y, size, size, color, right);
    }
    
    public static void drawSearchIcon(Graphics2D g2, int x, int y, int size, Color color) {
        BufferedImage src = getIcon("search");
        if (src != null) {
            drawTintedIcon(g2, src, x + size / 2, y + size / 2, size, color);
        }
    }

    public static void drawPlusIcon(Graphics2D g2, int cx, int cy, int size, Color color) {
        BufferedImage src = getIcon("plus");
        if (src != null) {
            drawTintedIcon(g2, src, cx, cy, size, color);
        }
    }

    public static void drawMenuIcon(Graphics2D g2, int cx, int cy, int size, Color color) {
        BufferedImage src = getIcon("menu");
        if (src != null) {
            drawTintedIcon(g2, src, cx, cy, size, color);
            return;
        }

        // Fallback if icon is missing.
        Graphics2D iconG = (Graphics2D) g2.create();
        iconG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        iconG.setColor(color);
        float stroke = Math.max(1.6f, size / 9f);
        iconG.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int half = size / 2;
        int left = cx - half + 2;
        int right = cx + half - 2;
        iconG.drawLine(left, cy - size / 4, right, cy - size / 4);
        iconG.drawLine(left, cy, right, cy);
        iconG.drawLine(left, cy + size / 4, right, cy + size / 4);
        iconG.dispose();
    }

    public static void drawCategoryIcon(Graphics2D g2, String category, int cx, int cy, int size, Color color) {
        String iconName = "";
        if ("Films".equals(category)) iconName = "film";
        else if ("Games".equals(category)) iconName = "game";
        else if ("Books".equals(category)) iconName = "book";
        else if ("Watchlist".equals(category)) iconName = "watchlist";

        if (!iconName.isEmpty()) {
            BufferedImage src = getIcon(iconName);
            if (src != null) {
                drawTintedIcon(g2, src, cx, cy, size, color);
            }
        }
    }

    public static void drawCategoryIcon(Graphics2D g2, String category, int cx, int cy, Color color, float strokeWidth, double scale) {
        drawCategoryIcon(g2, category, cx, cy, (int)(32 * scale), color);
    }

    public static void drawDropdownCategoryIcon(Graphics2D g2, int cx, int cy, int iconType, Color color, int size) {
        String iconName = "";
        if (iconType == 1) iconName = "film";
        else if (iconType == 2) iconName = "game";
        else if (iconType == 3) iconName = "book";
        else if (iconType == 4) iconName = "watchlist";

        if (!iconName.isEmpty()) {
            BufferedImage src = getIcon(iconName);
            if (src != null) {
                drawTintedIcon(g2, src, cx, cy, size, color);
            }
        }
    }

    public static void drawCalendarIcon(Graphics2D g2, int cx, int cy, int size, Color color) {
        BufferedImage src = getIcon("calendar");
        if (src != null) {
            drawTintedIcon(g2, src, cx, cy, size, color);
        }
    }
}
