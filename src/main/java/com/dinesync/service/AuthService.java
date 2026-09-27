package com.dinesync.service;

import com.dinesync.dao.UserDAO;
import com.dinesync.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AuthService — handles user signup and login logic.
 *
 * For this college project, we use a simple approach:
 * - Passwords are hashed using BCrypt before storing.
 * - On login, we verify the password and return user info.
 * - No JWT tokens — we store user info in browser localStorage.
 * - The server validates userId from request params for protected routes.
 */
@Service
public class AuthService {

    private final UserDAO userDAO;
    // BCryptPasswordEncoder: hashes passwords securely
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Register a new customer.
     *
     * @param name     Full name
     * @param email    Email (must be unique)
     * @param phone    Phone number
     * @param password Plain-text password (will be hashed before saving)
     * @return Map with success/error message and user info
     */
    public Map<String, Object> signup(String name, String email, String phone, String password) {
        Map<String, Object> response = new HashMap<>();

        // Check if email is already registered
        if (userDAO.emailExists(email)) {
            response.put("success", false);
            response.put("message", "An account with this email already exists.");
            return response;
        }

        // Hash the password — NEVER store plain text passwords
        String hashedPassword = passwordEncoder.encode(password);

        // Create and save the new user
        User user = new User(name, email, phone, hashedPassword, "CUSTOMER");
        int newUserId = userDAO.save(user);

        response.put("success", true);
        response.put("message", "Account created successfully!");
        response.put("userId", newUserId);
        response.put("name", name);
        response.put("email", email);
        response.put("role", "CUSTOMER");
        return response;
    }

    /**
     * Log in an existing user.
     *
     * @param email    Email address
     * @param password Plain-text password (will be verified against BCrypt hash)
     * @return Map with success/error message and user info
     */
    public Map<String, Object> login(String email, String password) {
        Map<String, Object> response = new HashMap<>();

        // Find user by email
        Optional<User> userOpt = userDAO.findByEmail(email);

        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Invalid email or password.");
            return response;
        }

        User user = userOpt.get();

        // Verify password using BCrypt (compares plain-text with stored hash)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("message", "Invalid email or password.");
            return response;
        }

        // Login successful — return user info (NOT the password hash)
        response.put("success", true);
        response.put("message", "Login successful!");
        response.put("userId", user.getUserId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("role", user.getRole());
        return response;
    }

    /**
     * Get user profile by ID.
     */
    public Map<String, Object> getProfile(int userId) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> userOpt = userDAO.findById(userId);

        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found.");
            return response;
        }

        User user = userOpt.get();
        response.put("success", true);
        response.put("userId", user.getUserId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("role", user.getRole());
        return response;
    }

    /**
     * Update user profile (name, phone).
     */
    public Map<String, Object> updateProfile(int userId, String name, String phone) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> userOpt = userDAO.findById(userId);

        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found.");
            return response;
        }

        User user = userOpt.get();
        user.setName(name);
        user.setPhone(phone);
        userDAO.update(user);

        response.put("success", true);
        response.put("message", "Profile updated successfully.");
        response.put("name", name);
        response.put("phone", phone);
        return response;
    }
}
