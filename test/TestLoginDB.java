package com.playtrack.test;

import com.playtrack.dao.UserDAO;
import com.playtrack.model.User;
import com.playtrack.util.PasswordUtil;

public class TestLoginDB {
    public static void main(String[] args) {
        UserDAO dao = new UserDAO();
        User u = new User();
        u.setUsername("testuser");
        u.setEmail("testuser@test.com");
        u.setPasswordHash(PasswordUtil.hashPassword("password123"));
        
        System.out.println("Register: " + dao.register(u));
        
        User loggedIn = dao.login("testuser", PasswordUtil.hashPassword("password123"));
        if (loggedIn != null) {
            System.out.println("Login Success: " + loggedIn.getUsername());
        } else {
            System.out.println("Login Failed!");
        }
    }
}
