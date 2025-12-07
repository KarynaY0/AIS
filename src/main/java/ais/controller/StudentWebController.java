package ais.controller;

import ais.entity.Grade;
import ais.entity.Student;
import ais.entity.Subject;
import ais.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Student Web Controller
 * Handles web pages for student operations - viewing grades
 */
@Controller
@RequestMapping("/student")
public class StudentWebController {

    @Autowired
    private StudentService studentService;

    /**
     * Check if user is student
     */
    private boolean isStudent(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "STUDENT".equals(role);
    }

    /**
     * Student Dashboard - Main page to view grades
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model,
                            @RequestParam(required = false) String filter) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long userId = (Long) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");

        try {
            // Get student info
            Student student = studentService.getStudentByUserId(userId);

            // Get all grades for the student
            List<Grade> allGrades = studentService.getMyGrades(userId);

            // Get all subjects the student is enrolled in
            List<Subject> subjects = studentService.getMySubjects(userId);

            // Calculate overall average
            BigDecimal overallAverage = studentService.calculateMyAverage(userId);

            // Group grades by subject for display
            Map<Long, List<Grade>> gradesBySubject = new HashMap<>();
            if (allGrades != null && !allGrades.isEmpty()) {
                gradesBySubject = allGrades.stream()
                        .collect(Collectors.groupingBy(grade -> grade.getSubject().getSubjectId()));
            }

            // Calculate average per subject
            Map<Long, BigDecimal> subjectAverages = new HashMap<>();
            for (Map.Entry<Long, List<Grade>> entry : gradesBySubject.entrySet()) {
                List<Grade> grades = entry.getValue();
                if (!grades.isEmpty()) {
                    BigDecimal sum = grades.stream()
                            .map(Grade::getGradeValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal average = sum.divide(new BigDecimal(grades.size()), 2, RoundingMode.HALF_UP);
                    subjectAverages.put(entry.getKey(), average);
                }
            }

            // Apply filter if specified
            List<Grade> filteredGrades = allGrades;
            if (filter != null && !filter.isEmpty()) {
                try {
                    Long filterSubjectId = Long.parseLong(filter);
                    filteredGrades = allGrades.stream()
                            .filter(grade -> grade.getSubject().getSubjectId().equals(filterSubjectId))
                            .collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    // Invalid filter, show all grades
                    filteredGrades = allGrades;
                }
            }

            // Add data to model
            model.addAttribute("username", username);
            model.addAttribute("student", student);
            model.addAttribute("grades", filteredGrades != null ? filteredGrades : new ArrayList<>());
            model.addAttribute("allGrades", allGrades != null ? allGrades : new ArrayList<>());
            model.addAttribute("subjects", subjects != null ? subjects : new ArrayList<>());
            model.addAttribute("overallAverage", overallAverage != null ? overallAverage : BigDecimal.ZERO);
            model.addAttribute("gradesBySubject", gradesBySubject);
            model.addAttribute("subjectAverages", subjectAverages);
            model.addAttribute("gradeCount", allGrades != null ? allGrades.size() : 0);
            model.addAttribute("selectedFilter", filter);

            // Determine if student has any grades
            model.addAttribute("hasGrades", allGrades != null && !allGrades.isEmpty());

            return "student-dashboard";

        } catch (Exception e) {
            e.printStackTrace(); // This will help debug
            model.addAttribute("errorMessage", "Error loading dashboard: " + e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("student", null);
            model.addAttribute("grades", new ArrayList<>());
            model.addAttribute("allGrades", new ArrayList<>());
            model.addAttribute("subjects", new ArrayList<>());
            model.addAttribute("overallAverage", BigDecimal.ZERO);
            model.addAttribute("gradesBySubject", new HashMap<>());
            model.addAttribute("subjectAverages", new HashMap<>());
            model.addAttribute("gradeCount", 0);
            model.addAttribute("selectedFilter", filter);
            model.addAttribute("hasGrades", false);

            return "student-dashboard";
        }
    }

    /**
     * Logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}