package com.playtrack.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.playtrack.config.AuthDatabaseSetup;
import com.playtrack.config.SystemDatabaseSetup;
import com.playtrack.ui.auth.AuthFrame;
import com.playtrack.ui.main.MainFrame;
import javax.swing.*;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.playtrack")
public class Main {
    public static void main(String[] args) {
        // Start Spring Boot
        ApplicationContext context = new SpringApplicationBuilder(Main.class)
                .headless(false)
                .run(args);

        // Store context for non-Spring Swing classes
        com.playtrack.util.SpringContext.setContext(context);

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());

            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 8);
            UIManager.put("Component.arc", 10);
            UIManager.put("Button.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ProgressBar.arc", 999);
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf Dark");
        }

        AuthDatabaseSetup.setup();
        SystemDatabaseSetup.setup();

        SwingUtilities.invokeLater(() -> showAuth());
    }

    private static void showAuth() {
        AuthFrame authFrame = new AuthFrame(() -> showMain());
        authFrame.setVisible(true);
    }

    private static void showMain() {
        MainFrame mainFrame = new MainFrame(() -> showAuth());
        mainFrame.setVisible(true);
    }
}
