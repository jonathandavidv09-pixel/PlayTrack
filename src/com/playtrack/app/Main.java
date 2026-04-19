package com.playtrack.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.playtrack.config.AuthDatabaseSetup;
import com.playtrack.config.SystemDatabaseSetup;
import com.playtrack.ui.auth.AuthFrame;
import com.playtrack.ui.components.StyleConfig;
import com.playtrack.ui.main.MainFrame;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.awt.Insets;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.playtrack")
// Application bootstrap component: starts the PlayTrack app.
public class Main {
    // main.
    public static void main(String[] args) {
       
        ApplicationContext context = new SpringApplicationBuilder(Main.class)
                .headless(false)
                .run(args);

     
        com.playtrack.util.SpringContext.setContext(context);

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());

            UIManager.put("defaultFont", new FontUIResource("Segoe UI", Font.PLAIN, 13));
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ScrollBar.thumb", new ColorUIResource(StyleConfig.withAlpha(StyleConfig.TEXT_LIGHT, 132)));
            UIManager.put("ScrollBar.track", new ColorUIResource(StyleConfig.withAlpha(StyleConfig.SURFACE_ELEVATED, 210)));
            UIManager.put("Component.arc", 18);
            UIManager.put("Button.arc", 18);
            UIManager.put("TextComponent.arc", 16);
            UIManager.put("ProgressBar.arc", 999);
            UIManager.put("Component.focusWidth", 0);
            UIManager.put("Button.focusedBorderColor", new ColorUIResource(StyleConfig.INPUT_FOCUS));
            UIManager.put("TextComponent.focusedBorderColor", new ColorUIResource(StyleConfig.INPUT_FOCUS));
            UIManager.put("TitlePane.unifiedBackground", Boolean.TRUE);
            UIManager.put("Panel.background", new ColorUIResource(StyleConfig.BACKGROUND_COLOR));
            UIManager.put("PopupMenu.borderInsets", new Insets(8, 8, 8, 8));

            
            UIManager.put("ToolTip.background", new ColorUIResource(StyleConfig.SURFACE_ELEVATED));
            UIManager.put("ToolTip.foreground", new ColorUIResource(StyleConfig.TEXT_COLOR));
            UIManager.put("ToolTip.font", new FontUIResource("Segoe UI", java.awt.Font.PLAIN, 12));
            UIManager.put("ToolTip.border", new CompoundBorder(
                    BorderFactory.createLineBorder(new ColorUIResource(StyleConfig.withAlpha(StyleConfig.TEXT_COLOR, 86)), 1),
                    new EmptyBorder(6, 10, 6, 10)));

            ToolTipManager.sharedInstance().setInitialDelay(140);
            ToolTipManager.sharedInstance().setReshowDelay(60);
            ToolTipManager.sharedInstance().setDismissDelay(6000);
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf Dark");
        }

        AuthDatabaseSetup.setup();
        SystemDatabaseSetup.setup();

        SwingUtilities.invokeLater(() -> {
            showAuth();
        });
    }

    // showAuth.
    private static void showAuth() {
        AuthFrame authFrame = new AuthFrame(() -> showMain());
        authFrame.setVisible(true);
    }

    // showMain.
    private static void showMain() {
        MainFrame mainFrame = new MainFrame(() -> showAuth());
        mainFrame.setVisible(true);
    }
}
 
