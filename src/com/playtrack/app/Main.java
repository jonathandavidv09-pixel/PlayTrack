package com.playtrack.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.playtrack.config.AuthDatabaseSetup;
import com.playtrack.config.SystemDatabaseSetup;
import com.playtrack.ui.auth.AuthFrame;
import com.playtrack.ui.main.MainFrame;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

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

            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 8);
            UIManager.put("Component.arc", 10);
            UIManager.put("Button.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ProgressBar.arc", 999);

            
            UIManager.put("ToolTip.background", new ColorUIResource(36, 46, 72));
            UIManager.put("ToolTip.foreground", new ColorUIResource(255, 255, 255));
            UIManager.put("ToolTip.font", new FontUIResource("Segoe UI", java.awt.Font.PLAIN, 12));
            UIManager.put("ToolTip.border", new CompoundBorder(
                    BorderFactory.createLineBorder(new ColorUIResource(new java.awt.Color(255, 255, 255, 70)), 1),
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
 
