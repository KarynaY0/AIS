package ais.controller;

import ais.entity.*;
import ais.service.TeacherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * Teacher Web Controller
 * Handles web pages and form submissions for teacher operations
 * ✅ VERIFIED: All types are correct (Long throughout)
 */
@Controller
@RequestMapping("/teacher")
public class TeacherWebController {

    @Autowired
    private TeacherService teacherService;

    /**
     * Check if user is teacher
     */
    private boolean isTeacher(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "TEACHER".equals(role);
    }

    /**
     * Teacher Dashboard - Main page
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isTeacher(session)) {
            return "redirect:/login";
        }

        Long userId = (Long) session.getAttribute("userId");  // ✅ Long
        String username = (String) session.getAttribute("username");

        model.addAttribute("username", username);

        // Get subjects taught by teacher
        List<Subject> subjects = teacherService.getSubjectsTaughtByTeacher(userId);
        model.addAttribute("subjects", subjects);

        return "teacher-dashboard";
    }

    /**
     * Teacher Grades page - View and manage grades for a subject
     */
    @GetMapping("/grades")
    public String gradesPage(@RequestParam Long subjectId,  // ✅ Long
                             HttpSession session,
                             Model model) {
        if (!isTeacher(session)) {
            return "redirect:/login";
        }

        Long userId = (Long) session.getAttribute("userId");  // ✅ Long
        String username = (String) session.getAttribute("username");

        try {
            // Verify teacher teaches this subject
            if (!teacherService.isTeachingSubject(userId, subjectId)) {
                model.addAttribute("errorMessage", "You do not teach this subject.");
                return "redirect:/teacher/dashboard";
            }

            // Get subject details
            Subject subject = teacherService.getSubjectsTaughtByTeacher(userId).stream()
                    .filter(s -> s.getSubjectId().equals(subjectId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

            // Get all grades for this subject
            List<Grade> grades = teacherService.getGradesBySubject(userId, subjectId);

            // Get all students taught by this teacher (for adding new grades)
            List<Student> students = teacherService.getStudentsTaughtByTeacher(userId);

            model.addAttribute("username", username);
            model.addAttribute("subject", subject);
            model.addAttribute("grades", grades);
            model.addAttribute("students", students);

            return "teacher-grades";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/teacher/dashboard";
        }
    }

    /**
     * Enter a new grade (allows multiple grades per student per subject)
     */
    @PostMapping("/grades/enter")
    public String enterGrade(@RequestParam Long subjectId,    // ✅ Long
                             @RequestParam Long studentId,    // ✅ Long
                             @RequestParam BigDecimal gradeValue,
                             @RequestParam(required = false) String comment,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!isTeacher(session)) {
            return "redirect:/login";
        }

        Long teacherUserId = (Long) session.getAttribute("userId");  // ✅ Long

        try {
            teacherService.enterGrade(teacherUserId, studentId, subjectId, gradeValue, comment);
            redirectAttributes.addFlashAttribute("successMessage", "Grade entered successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/teacher/grades?subjectId=" + subjectId;
    }

    /**
     * Edit an existing grade
     */
    @PostMapping("/grades/edit")
    public String editGrade(@RequestParam Long gradeId,      // ✅ Long
                            @RequestParam Long subjectId,    // ✅ Long
                            @RequestParam BigDecimal gradeValue,
                            @RequestParam(required = false) String comment,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (!isTeacher(session)) {
            return "redirect:/login";
        }

        Long teacherUserId = (Long) session.getAttribute("userId");  // ✅ Long

        try {
            teacherService.editGrade(teacherUserId, gradeId, gradeValue, comment);
            redirectAttributes.addFlashAttribute("successMessage", "Grade updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/teacher/grades?subjectId=" + subjectId;
    }

    /**
     * Delete a grade
     */
    @PostMapping("/grades/delete/{gradeId}")
    public String deleteGrade(@PathVariable Long gradeId,    // ✅ Long
                              @RequestParam Long subjectId,  // ✅ Long
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isTeacher(session)) {
            return "redirect:/login";
        }

        Long teacherUserId = (Long) session.getAttribute("userId");  // ✅ Long

        try {
            teacherService.deleteGrade(teacherUserId, gradeId);
            redirectAttributes.addFlashAttribute("successMessage", "Grade deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/teacher/grades?subjectId=" + subjectId;
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