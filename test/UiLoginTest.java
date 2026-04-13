package com.playtrack.test;

import com.playtrack.ui.components.PlaceholderPasswordField;
import com.playtrack.ui.components.PlaceholderTextField;

// Simple UI input field smoke test.
public class UiLoginTest {
    public static void main(String[] args) {
        PlaceholderTextField userField = new PlaceholderTextField("Enter username", "USER");
        userField.setText("testuser");
        System.out.println("Username: " + userField.getText());

        PlaceholderPasswordField passField = new PlaceholderPasswordField("Enter password", "LOCK");
        passField.setText("password123");
        System.out.println("Password: " + new String(passField.getPassword()));
    }
}
