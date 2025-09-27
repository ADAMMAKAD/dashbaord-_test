package com.exam.exam.controller;

import com.exam.exam.entity.Role;
import com.exam.exam.entity.User;
import com.exam.exam.repository.UserRepository;
import com.exam.exam.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication controller for login/register
 * Using JWT tokens for auth
 *  Add rate limiting for login attempts
 * 
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder; 

    @Autowired
    private JwtUtil jwtUtil;

    // login endpoint
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (username == null || password == null || username.trim().isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Username and password required");
            return ResponseEntity.status(400).body(errorResponse);
        }

        try {
            // find user and check password
            User user = userRepo.findByUname(username.trim()).orElse(null);
            if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                String token = jwtUtil.generateToken(user);
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                response.put("username", user.getUsername());
                response.put("role", user.getRole().toString());
                
                return ResponseEntity.ok(response);
            }
            
            // invalid credentials - don't give too much info for security
            System.out.println("Failed login attempt for: " + username); // security log
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid credentials");
            return ResponseEntity.status(401).body(errorResponse);
            
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Login failed");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // register new user endpoint 
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody Map<String, String> userData) {
        String username = userData.get("username");
        String password = userData.get("password");
        String email = userData.get("email");

        // basic validation - should probably do more here
        if (username == null || username.trim().isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Username is required");
            return ResponseEntity.status(400).body(errorResponse);
        }

        if (password == null || password.length() < 6) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Password must be at least 6 characters");
            return ResponseEntity.status(400).body(errorResponse);
        }
        
        if (userRepo.findByUsername(username).isPresent()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Username already exists");
            return ResponseEntity.status(409).body(errorResponse); 
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email); // not validating email format for now
        newUser.setRole(Role.USER); // default role

        try {
            userRepo.save(newUser);
        } catch (Exception e) {
            // database error
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create user");
            return ResponseEntity.status(500).body(errorResponse);
        }

        // generate token for new user
        String token = jwtUtil.generateToken(newUser);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.status(201).body(response);
    }
}