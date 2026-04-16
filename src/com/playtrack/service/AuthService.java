package com.playtrack.service;

import com.playtrack.dao.UserDAO;
import com.playtrack.dao.ProfileDAO;
import com.playtrack.model.User;
import com.playtrack.model.Profile;
import com.playtrack.util.PasswordUtil;
import com.playtrack.util.SessionManager;

// Service layer component: coordinates business logic.
public class AuthService {
    private UserDAO userDAO = new UserDAO();
    private ProfileDAO profileDAO = new ProfileDAO();

    // register.
    public boolean register(String username, String email, String password) {
        if (userDAO.isUsernameTaken(username))
            return false;

        String hash = PasswordUtil.hashPassword(password);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(hash);

        if (userDAO.register(user)) {
         
            Profile profile = new Profile();
            profile.setUserId(user.getId());
            profile.setUsername(username);
            profile.setBio("Welcome to my PlayTrack profile!");
            profileDAO.createProfile(profile);

          
            com.playtrack.util.DatabaseLogger.logActivity(username, "Sign Up", "User registered a new account");

            return true;
        }
        return false;
    }

    // Root login
    public boolean login(String identifier, String password) {
        String hash = PasswordUtil.hashPassword(password);
        User user = userDAO.login(identifier, hash);
        if (user != null) {
            SessionManager.setCurrentUser(user);
            return true;
        }
        return false;
    }

    // isUsernameTaken.
    public boolean isUsernameTaken(String username) {
        return userDAO.isUsernameTaken(username);
    }

    // updateSettings.
    public boolean updateSettings(User user, String currentPasswordUnHashed, String newPasswordUnHashed) {
        String newHash = null;
        if (newPasswordUnHashed != null && !newPasswordUnHashed.isEmpty()) {
            String currentHash = PasswordUtil.hashPassword(currentPasswordUnHashed);
            if (!currentHash.equals(user.getPasswordHash())) {
                return false;
            }
            newHash = PasswordUtil.hashPassword(newPasswordUnHashed);
            user.setPasswordHash(newHash);
        }

        boolean success = userDAO.updateUserAndAuth(user.getId(), user.getUsername(), user.getEmail(), newHash);
        if (success) {
            Profile p = profileDAO.getProfile(user.getId());
            if (p != null) {
                p.setUsername(user.getUsername());
                profileDAO.updateProfile(p);
            }
        }
        return success;
    }
}
