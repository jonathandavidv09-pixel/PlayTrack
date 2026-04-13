package com.playtrack.util;

import com.playtrack.model.User;

// Utility component: shared helpers for the system layer.
public class SessionManager {
    private static User currentUser;

    // setCurrentUser.
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // getCurrentUser.
    public static User getCurrentUser() {
        return currentUser;
    }

    // isLoggedIn.
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // logout.
    public static void logout() {
        currentUser = null;
    }
}