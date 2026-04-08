package com.playtrack.ui.components;

import java.awt.Color;
import java.awt.Font;

public class StyleConfig {
    // Modern Dark Theme Colors (Deep Blue/Grey with Coral Red Accents)
    public static final Color PRIMARY_COLOR = new Color(211, 64, 69);       // Coral Red from design
    public static final Color PRIMARY_LIGHT = new Color(230, 80, 85);
    public static final Color PRIMARY_DARK = new Color(180, 50, 55);
    public static final Color SECONDARY_COLOR = new Color(211, 64, 69);     // Also red for emphasis
    public static final Color ACCENT_COLOR = new Color(220, 50, 60);

    // Dark backgrounds
    public static final Color BACKGROUND_COLOR = new Color(25, 30, 42);     // Deep outer background
    public static final Color BACKGROUND_LIGHT = new Color(36, 45, 60);     // Inputs / dark areas
    public static final Color CARD_BACKGROUND = new Color(51, 62, 79);      // Window body
    public static final Color CARD_HOVER = new Color(66, 78, 97);           // Hover state for cards
    public static final Color SURFACE_COLOR = new Color(51, 62, 79);        // Panels

    // Text colors
    public static final Color TEXT_COLOR = new Color(245, 245, 250);        // Near white
    public static final Color TEXT_SECONDARY = new Color(170, 180, 195);    // Light bluish gray
    public static final Color TEXT_LIGHT = new Color(130, 140, 155);        // Muted gray

    // Category colors
    public static final Color FILM_COLOR = new Color(0xB4182D);             // Red from palette
    public static final Color GAME_COLOR = new Color(56, 163, 165);         // Teal (Original)
    public static final Color BOOK_COLOR = new Color(0xFDA481);             // Peach from palette
    public static final Color WATCHLIST_COLOR = new Color(0xB4182D);        // Red (Same as Film)

    // Status colors
    public static final Color SUCCESS_COLOR = new Color(46, 204, 113);      
    public static final Color ERROR_COLOR = new Color(211, 64, 69);         
    public static final Color WARNING_COLOR = new Color(241, 196, 15);      
    public static final Color STAR_COLOR = new Color(211, 64, 69);          // Red for filled stars
    public static final Color STAR_COLOR_EMPTY = new Color(170, 180, 195);  // Gray for empty stars

    // Border / Divider
    public static final Color BORDER_COLOR = new Color(66, 78, 97);         
    public static final Color DIVIDER_COLOR = new Color(66, 78, 97, 120);

    // Gradient endpoints for panels/accents
    public static final Color GRADIENT_START = new Color(211, 64, 69);      
    public static final Color GRADIENT_END = new Color(180, 50, 55);        

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 36);
    public static final Font FONT_LARGE_NUMBER = new Font("Segoe UI", Font.BOLD, 46);
}
