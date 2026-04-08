package com.playtrack.service;

import com.playtrack.dao.ProfileDAO;
import com.playtrack.model.Profile;

public class ProfileService {
    private ProfileDAO profileDAO = new ProfileDAO();

    public Profile getProfile(int userId) {
        return profileDAO.getProfile(userId);
    }

    public boolean updateProfile(Profile profile) {
        return profileDAO.updateProfile(profile);
    }
}
