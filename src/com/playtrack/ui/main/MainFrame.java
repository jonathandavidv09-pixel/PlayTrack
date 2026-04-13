package com.playtrack.ui.main;

import com.playtrack.ui.components.Navbar;
import com.playtrack.ui.home.HomePanel;
import com.playtrack.ui.library.LibraryPanel;
import com.playtrack.ui.summary.SummaryPanel;
import com.playtrack.ui.profile.ProfilePanel;
import com.playtrack.ui.review.ReviewFormDialog;
import com.playtrack.util.SessionManager;
import javax.swing.*;
import java.awt.*;
// Main application window.
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private Navbar navbar;
    private HomePanel homePanel;
    private LibraryPanel libraryPanel;
    private SummaryPanel summaryPanel;
    private ProfilePanel profilePanel;
    private Runnable onLogout;
    // Constructor.
    public MainFrame(Runnable onLogout) {
        this.onLogout = onLogout;
        setTitle("PlayTrack");
        setSize(1400, 900);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        // Navigation bar with callbacks for page switching and logout.
        navbar = new Navbar(
            () -> showPage("home"),
            () -> showPage("library"),
            () -> showPage("summary"),
            () -> showPage("profile"),
            () -> logout()
        );
        add(navbar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        homePanel = new HomePanel(
            category -> showAddMediaDialog(category),
            category -> navigateToLibrary(category)
        );
        libraryPanel = new LibraryPanel();
        summaryPanel = new SummaryPanel();
        profilePanel = new ProfilePanel();
        // Add panels to the content panel with corresponding names for the CardLayout.
        contentPanel.add(homePanel, "home");
        contentPanel.add(libraryPanel, "library");
        contentPanel.add(summaryPanel, "summary");
        contentPanel.add(profilePanel, "profile");

        add(contentPanel, BorderLayout.CENTER);
    }
    // Method to refresh all panels.
    public void refreshAll() {
        if (navbar != null) navbar.refreshUser();
        if (homePanel != null) { homePanel.refreshStats(); homePanel.refreshRecentActivity(); }
        if (libraryPanel != null) libraryPanel.refreshLibrary();
        if (summaryPanel != null) summaryPanel.refreshSummary();
        if (profilePanel != null) profilePanel.refreshProfile();
        if (contentPanel != null) { contentPanel.revalidate(); contentPanel.repaint(); }
    }
    // Method to show a specific page based on its name.
    private void showPage(String name) {
        if (navbar != null) navbar.refreshUser();
        // Refresh the target panel's content before showing it.
        switch (name) {
            case "home":
                if (homePanel != null) { homePanel.refreshStats(); homePanel.refreshRecentActivity(); }
                break;
            case "library":
                if (libraryPanel != null) libraryPanel.reset();
                break;
            case "summary":
                if (summaryPanel != null) summaryPanel.refreshSummary();
                break;
            case "profile":
                if (profilePanel != null) profilePanel.refreshProfile();
                break;
        }
        
        if (contentPanel != null) {
            contentPanel.revalidate();
            contentPanel.repaint();
        }
        
        cardLayout.show(contentPanel, name);

       
        SwingUtilities.invokeLater(() -> {
            JScrollPane targetScroll = findScrollPane(getPanelByName(name));
            if (targetScroll != null) {
                targetScroll.getVerticalScrollBar().setValue(0);
            }
        });
    }
    // Helper method to get a panel by its name.
    private JPanel getPanelByName(String name) {
        if (name == null) return null;
        switch (name) {
            case "home": return homePanel;
            case "library": return libraryPanel;
            case "summary": return summaryPanel;
            case "profile": return profilePanel;
            default: return null;
        }
    }

    private JScrollPane findScrollPane(Container container) {
        if (container == null) return null;
        for (Component c : container.getComponents()) {
            if (c instanceof JScrollPane) return (JScrollPane) c;
            if (c instanceof Container) {
                JScrollPane found = findScrollPane((Container) c);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void showAddMediaDialog(String category) {
        ReviewFormDialog dialog = new ReviewFormDialog(this, category, () -> refreshAll());
        dialog.setVisible(true);
    }
    // Method to navigate to the library page with a specific category filter.
    private void navigateToLibrary(String category) {
        if (navbar != null) navbar.setActiveNav("Library");
        showPage("library");
        if (libraryPanel != null) {
            libraryPanel.setCategory(category);
        }
    }

    private void logout() {
        SessionManager.logout();
        onLogout.run();
        dispose();
    }
}
