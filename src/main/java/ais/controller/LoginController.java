package ais.controller;

import ais.entity.User;
import ais.repository.AdminRepository;
import ais.repository.StudentRepository;
import ais.repository.TeacherRepository;
import ais.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        // Find user
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null || !user.getPassword().equals(password)) {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login";
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
            redirectAttributes.addFlashAttribute("error", "User role not found");
            return "redirect:/login";
        }

        // Store session data
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", role);

        // Redirect based on role
        if ("ADMINISTRATOR".equals(role)) {
            return "redirect:/admin/dashboard";
        } else if ("TEACHER".equals(role)) {
            return "redirect:/teacher/dashboard";
        } else if ("STUDENT".equals(role)) {
            return "redirect:/student/dashboard";
        }

        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}