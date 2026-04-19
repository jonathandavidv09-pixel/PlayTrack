package com.playtrack.service;

import com.playtrack.dao.ProfileDAO;
import com.playtrack.model.Profile;

// Service layer component: coordinates profile loading and profile updates.
public class ProfileService {
    private ProfileDAO profileDAO = new ProfileDAO();

    // Start: load profile service function.
    public Profile getProfile(int userId) {
        return profileDAO.getProfile(userId);
    }
    // End: load profile service function.

    // Start: update profile service function.
    public boolean updateProfile(Profile profile) {
        return profileDAO.updateProfile(profile);
    }
    // End: update profile service function.
}
