package ais.controller;

import ais.entity.Admin;
import ais.entity.Student;
import ais.entity.Teacher;
import ais.entity.User;
import ais.repository.AdminRepository;
import ais.repository.StudentRepository;
import ais.repository.TeacherRepository;
import ais.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 * Handles login, logout, and session management
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * Login endpoint
     * POST /api/auth/login
     * Body: {"username": "...", "password": "..."}
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> credentials,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            // Find user
            User user = userRepository.findByUsername(username).orElse(null);

            if (user == null || !user.getPassword().equals(password)) {
                response.put("success", false);
                response.put("message", "Invalid username or password");
                return ResponseEntity.ok(response);
            }

            // Determine user role
            String role = null;
            if (adminRepository.existsByUser(user)) {
                role = "ADMINISTRATOR";
            } else if (teacherRepository.existsByUser(user)) {
                role = "TEACHER";
            } else if (studentRepository.existsByUser(user)) {
                role = "STUDENT";
            } else {
                response.put("success", false);
                response.put("message", "User role not found");
                return ResponseEntity.ok(response);
            }

            // Store session data
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", role);

            response.put("success", true);
            response.put("userId", user.getUserId());
            response.put("username", user.getUsername());
            response.put("role", role);
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login error: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Logout endpoint
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            session.invalidate();
            response.put("success", true);
            response.put("message", "Logged out successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Logout error: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get current session information
     * GET /api/auth/session
     */
    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> getSession(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Integer userId = (Integer) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        if (userId != null) {
            response.put("loggedIn", true);
            response.put("userId", userId);
            response.put("username", username);
            response.put("role", role);
        } else {
            response.put("loggedIn", false);
        }

        return ResponseEntity.ok(response);
    }
}