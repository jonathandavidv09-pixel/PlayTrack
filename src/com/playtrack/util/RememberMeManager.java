package com.playtrack.util;

import java.util.prefs.Preferences;

public final class RememberMeManager {
    private static final Preferences PREFS = Preferences.userNodeForPackage(RememberMeManager.class);
    private static final String KEY_USER_ID = "remember_me_user_id";

    private RememberMeManager() {
    }

    public static void rememberUser(int userId) {
        if (userId > 0) {
            PREFS.putInt(KEY_USER_ID, userId);
        }
    }

    public static Integer getRememberedUserId() {
        int value = PREFS.getInt(KEY_USER_ID, -1);
        return value > 0 ? value : null;
    }

    public static void clear() {
        PREFS.remove(KEY_USER_ID);
    }
}
