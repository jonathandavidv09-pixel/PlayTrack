package com.playtrack.ui.components;

import java.awt.Color;
import java.awt.Font;
// Style configuration class.
public class StyleConfig {
    
    public static final Color PALETTE_NAVY = new Color(0x08, 0x12, 0x21);
    public static final Color PALETTE_INDIGO = new Color(0x11, 0x1D, 0x31);
    public static final Color PALETTE_SLATE = new Color(0x21, 0x30, 0x49);
    public static final Color PALETTE_PEACH = new Color(0xF6, 0xBE, 0x7A);
    public static final Color PALETTE_RED = new Color(0xFF, 0x72, 0x68);
    public static final Color PALETTE_WINE = new Color(0x9A, 0x53, 0x66);

    
    public static final Color PRIMARY_COLOR = PALETTE_RED;
    public static final Color PRIMARY_LIGHT = new Color(0xFF, 0x92, 0x83);
    public static final Color PRIMARY_DARK = new Color(0xD8, 0x59, 0x58);
    public static final Color SECONDARY_COLOR = PALETTE_PEACH;
    public static final Color ACCENT_COLOR = PALETTE_PEACH;

    
    public static final Color BACKGROUND_COLOR = PALETTE_NAVY;
    public static final Color BACKGROUND_LIGHT = new Color(0x0F, 0x1B, 0x2D);
    public static final Color CARD_BACKGROUND = new Color(0x13, 0x22, 0x37);
    public static final Color CARD_HOVER = new Color(0x1D, 0x31, 0x4D);
    public static final Color SURFACE_COLOR = new Color(0x18, 0x27, 0x3C);
    public static final Color SURFACE_ELEVATED = new Color(0x1D, 0x2D, 0x46);
    public static final Color SURFACE_SOFT = new Color(0x24, 0x36, 0x53);

    
    public static final Color INPUT_BG = new Color(0x11, 0x1B, 0x2E);
    public static final Color INPUT_BG_FOCUS = new Color(0x17, 0x24, 0x3A);
    public static final Color INPUT_FOCUS = new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 176);

    
    public static final Color TEXT_COLOR = new Color(245, 248, 255);
    public static final Color TEXT_SECONDARY = new Color(198, 209, 228);
    public static final Color TEXT_LIGHT = new Color(140, 155, 180);

    
    public static final Color FILM_COLOR = PALETTE_RED;
    public static final Color GAME_COLOR = new Color(0x73, 0xCC, 0xFF);
    public static final Color BOOK_COLOR = new Color(0xDA, 0xB0, 0x66);
    public static final Color WATCHLIST_COLOR = PALETTE_PEACH;

    
    public static final Color SUCCESS_COLOR = new Color(87, 210, 144);
    public static final Color ERROR_COLOR = new Color(255, 128, 118);
    public static final Color WARNING_COLOR = new Color(246, 196, 99);
    public static final Color STAR_COLOR = new Color(255, 202, 105);
    public static final Color STAR_COLOR_EMPTY = new Color(122, 138, 165);

    
    public static final Color BORDER_COLOR = new Color(86, 102, 128);
    public static final Color DIVIDER_COLOR = new Color(120, 136, 162, 118);
    public static final Color SURFACE_STROKE = new Color(255, 255, 255, 34);
    public static final Color PANEL_GLOW_PRIMARY = new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 56);
    public static final Color PANEL_GLOW_SECONDARY = new Color(SECONDARY_COLOR.getRed(), SECONDARY_COLOR.getGreen(), SECONDARY_COLOR.getBlue(), 38);

    
    public static final Color GRADIENT_START = PRIMARY_COLOR;
    public static final Color GRADIENT_END = SECONDARY_COLOR;

    public static final int PANEL_RADIUS = 26;
    public static final int INPUT_RADIUS = 16;
    public static final int BUTTON_RADIUS = 18;

    
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 38);
    public static final Font FONT_LARGE_NUMBER = new Font("Segoe UI", Font.BOLD, 48);

    // Global page spacing (reduced to keep layouts tighter).
    public static final int PAGE_PAD_X = 24;
    public static final int PAGE_PAD_TOP = 16;
    public static final int PAGE_PAD_BOTTOM = 16;

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}
