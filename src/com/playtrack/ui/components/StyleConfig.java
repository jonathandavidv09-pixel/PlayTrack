package com.playtrack.ui.components;

import java.awt.Color;
import java.awt.Font;
// Style configuration class.
public class StyleConfig {
    
    public static final Color PALETTE_NAVY = new Color(0x18, 0x1A, 0x2F);   
    public static final Color PALETTE_INDIGO = new Color(0x24, 0x2E, 0x49); 
    public static final Color PALETTE_SLATE = new Color(0x37, 0x41, 0x5C);  
    public static final Color PALETTE_PEACH = new Color(0xFD, 0xA4, 0x81);  
    public static final Color PALETTE_RED = new Color(0xB4, 0x18, 0x2D);    
    public static final Color PALETTE_WINE = new Color(0x54, 0x16, 0x2B);   

    
    public static final Color PRIMARY_COLOR = PALETTE_RED;
    public static final Color PRIMARY_LIGHT = new Color(0xC9, 0x31, 0x45);
    public static final Color PRIMARY_DARK = new Color(0x8F, 0x12, 0x23);
    public static final Color SECONDARY_COLOR = PALETTE_PEACH;
    public static final Color ACCENT_COLOR = PALETTE_PEACH;

    
    public static final Color BACKGROUND_COLOR = PALETTE_NAVY;
    public static final Color BACKGROUND_LIGHT = PALETTE_INDIGO;
    public static final Color CARD_BACKGROUND = PALETTE_INDIGO;
    public static final Color CARD_HOVER = PALETTE_SLATE;
    public static final Color SURFACE_COLOR = PALETTE_SLATE;
    public static final Color SURFACE_ELEVATED = PALETTE_INDIGO;
    public static final Color SURFACE_SOFT = PALETTE_SLATE;

    
    public static final Color INPUT_BG = new Color(0x1F, 0x28, 0x3F);
    public static final Color INPUT_BG_FOCUS = new Color(0x2A, 0x35, 0x52);
    public static final Color INPUT_FOCUS = new Color(0xFD, 0xA4, 0x81, 185);

    
    public static final Color TEXT_COLOR = new Color(239, 244, 255);
    public static final Color TEXT_SECONDARY = new Color(198, 206, 224);
    public static final Color TEXT_LIGHT = new Color(154, 166, 195);

    
    public static final Color FILM_COLOR = PALETTE_RED;
    public static final Color GAME_COLOR = PALETTE_PEACH;
    public static final Color BOOK_COLOR = PALETTE_WINE;
    public static final Color WATCHLIST_COLOR = PALETTE_RED;

    
    public static final Color SUCCESS_COLOR = new Color(46, 204, 113);      
    public static final Color ERROR_COLOR = new Color(211, 64, 69);         
    public static final Color WARNING_COLOR = new Color(241, 196, 15);      
    public static final Color STAR_COLOR = PALETTE_PEACH;
    public static final Color STAR_COLOR_EMPTY = new Color(170, 180, 195);  

    
    public static final Color BORDER_COLOR = new Color(107, 119, 150);
    public static final Color DIVIDER_COLOR = new Color(107, 119, 150, 120);
    public static final Color SURFACE_STROKE = new Color(255, 255, 255, 28);
    public static final Color PANEL_GLOW_PRIMARY = new Color(180, 24, 45, 46);
    public static final Color PANEL_GLOW_SECONDARY = new Color(253, 164, 129, 34);

    
    public static final Color GRADIENT_START = PRIMARY_COLOR;
    public static final Color GRADIENT_END = SECONDARY_COLOR;

    
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 36);
    public static final Font FONT_LARGE_NUMBER = new Font("Segoe UI", Font.BOLD, 46);
}