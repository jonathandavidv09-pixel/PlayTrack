package com.playtrack.service;

import com.playtrack.dao.ProfileDAO;
import com.playtrack.model.Profile;

// Service layer component: coordinates business logic.
public class ProfileService {
    private ProfileDAO profileDAO = new ProfileDAO();

    // getProfile.
    public Profile getProfile(int userId) {
        return profileDAO.getProfile(userId);
    }

    // updateProfile.
    public boolean updateProfile(Profile profile) {
        return profileDAO.updateProfile(profile);
    }
}