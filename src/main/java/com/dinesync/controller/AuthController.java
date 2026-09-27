package com.dinesync.controller;

import com.dinesync.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController — handles user registration and login API endpoints.
 *
 * Endpoints:
 *   POST /api/auth/signup  — Create a new customer account
 *   POST /api/auth/login   — Login with email and password
 *   GET  /api/auth/profile — Get user profile
 *   PUT  /api/auth/profile — Update user profile
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")  // Allow frontend JavaScript to call this API
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/signup
     * Request body: { "name": "...", "email": "...", "phone": "...", "password": "..." }
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> body) {
        String name = body != null ? body.get("name") : null;
        String email = body != null ? body.get("email") : null;
        String phone = body != null ? body.get("phone") : null;
        String password = body != null ? body.get("password") : null;

        // Basic validation
        if (name == null || email == null || password == null ||
            name.isBlank() || email.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Name, email, and password are required."));
        }

        if (password.length() < 6) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Password must be at least 6 characters."));
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Please enter a valid email address."));
        }

        Map<String, Object> result = authService.signup(name, email, phone, password);
        if (Boolean.FALSE.equals(result.get("success"))) {
            return ResponseEntity.status(409).body(result);
        }
        return ResponseEntity.status(201).body(result);
    }

    /**
     * POST /api/auth/login
     * Request body: { "email": "...", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String email = body != null ? body.get("email") : null;
        String password = body != null ? body.get("password") : null;

        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Email and password are required."));
        }

        Map<String, Object> result = authService.login(email, password);
        if (Boolean.FALSE.equals(result.get("success"))) {
            return ResponseEntity.status(401).body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/auth/profile?userId=1
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestParam int userId) {
        Map<String, Object> result = authService.getProfile(userId);
        if (Boolean.FALSE.equals(result.get("success"))) {
            return ResponseEntity.status(404).body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * PUT /api/auth/profile
     * Request body: { "userId": 1, "name": "...", "phone": "..." }
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> body) {
        if (body == null || body.get("userId") == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "User ID is required."));
        }
        try {
            int userId = Integer.parseInt(body.get("userId").toString());
            String name = (String) body.get("name");
            String phone = (String) body.get("phone");

            Map<String, Object> result = authService.updateProfile(userId, name, phone);
            if (Boolean.FALSE.equals(result.get("success"))) {
                return ResponseEntity.status(404).body(result);
            }
            return ResponseEntity.ok(result);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Invalid user ID."));
        }
    }
}
