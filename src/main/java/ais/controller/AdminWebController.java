package ais.controller;

import ais.entity.*;
import ais.service.AdminService;
import ais.service.UpdateEntityService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/admin")
public class AdminWebController {

    private final AdminService adminService;
    private final UpdateEntityService updateEntityService;

    @Autowired
    public AdminWebController(AdminService adminService,
                              UpdateEntityService updateEntityService) {
        this.adminService = adminService;
        this.updateEntityService = updateEntityService;
    }

    /**
     * Check if user is admin
     */
    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "ADMINISTRATOR".equals(role);
    }

    /**
     * Admin Dashboard - Main page
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }
        model.addAttribute("username", session.getAttribute("username"));
        return "admin-dashboard";
    }

    // ==================== STUDENT PAGES ====================

    /**
     * Students management page
     */
    @GetMapping("/students")
    public String studentsPage(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("students", adminService.getAllStudents());
        model.addAttribute("groups", adminService.getAllGroups());
        return "admin-students";
    }

    /**
     * Create student
     */
    @PostMapping("/students/create")
    public String createStudent(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam(required = false) Long groupId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            adminService.createStudent(firstName, lastName, groupId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Student created successfully! Username: " + firstName + ", Password: " + lastName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error: " + e.getMessage());
        }

        return "redirect:/admin/students";
    }

    /**
     * Update student
     */
    @PostMapping("/students/update/{userId}")
    public String updateStudent(@PathVariable Long userId,
                                @RequestParam(required = false) String username,
                                @RequestParam(required = false) String password,
                                @RequestParam(required = false) Long groupId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            updateEntityService.updateStudent(userId, username, password, groupId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Student updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error: " + e.getMessage());
        }

        return "redirect:/admin/students";
    }

    /**
     * Delete student
     */
    @PostMapping("/students/delete/{userId}")
    public String deleteStudent(@PathVariable Long userId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            adminService.deleteStudent(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Student deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/admin/students";
    }

    // ==================== TEACHER PAGES ====================

    /**
     * Teachers management page
     */
    @GetMapping("/teachers")
    public String teachersPage(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("teachers", adminService.getAllTeachers());
        return "admin-teachers";
    }

    /**
     * Create teacher
     */
    @PostMapping("/teachers/create")
    public String createTeacher(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam(required = false) String department,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            adminService.createTeacher(firstName, lastName, department);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Teacher created successfully! Username: " + firstName + ", Password: " + lastName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/admin/teachers";
    }

    /**
     * Update teacher
     */
    @PostMapping("/teachers/update/{userId}")
    public String updateTeacher(@PathVariable Long userId,
                                @RequestParam(required = false) String username,
                                @RequestParam(required = false) String password,
                                @RequestParam(required = false) String department,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            updateEntityService.updateTeacher(userId, username, password, department);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Teacher updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error: " + e.getMessage());
        }

        return "redirect:/admin/teachers";
    }

    /**
     * Delete teacher
     */
    @PostMapping("/teachers/delete/{userId}")
    public String deleteTeacher(@PathVariable Long userId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            adminService.deleteTeacher(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/admin/teachers";
    }

    // ==================== GROUP PAGES ====================

    /**
     * Groups management page
     */
    @GetMapping("/groups")
    public String groupsPage(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("groups", adminService.getAllGroups());
        return "admin-groups";
    }

    /**
     * Create group - NEW FORMAT
     * Expects: programInitials, startYear, languageCode
     * Validates input and ensures groupCode is properly generated
     */
    @PostMapping("/groups/create")
    public String createGroup(@RequestParam String programInitials,
                              @RequestParam Integer startYear,
                              @RequestParam(required = false) String languageCode,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            // Validate and normalize input
            programInitials = programInitials.trim().toUpperCase();
            if (languageCode != null && !languageCode.isEmpty()) {
                languageCode = languageCode.trim().toUpperCase();
            } else {
                languageCode = null; // Set to null if empty
            }

            // Validate programInitials (2-3 characters)
            if (programInitials.length() < 2 || programInitials.length() > 3) {
                throw new IllegalArgumentException("Program initials must be 2-3 characters");
            }

            // Validate startYear (0-99)
            if (startYear < 0 || startYear > 99) {
                throw new IllegalArgumentException("Start year must be between 0 and 99");
            }

            // Validate languageCode (single character if provided)
            if (languageCode != null && languageCode.length() != 1) {
                throw new IllegalArgumentException("Language code must be a single character");
            }

            // Create group through service
            Group group = adminService.createGroup(programInitials, startYear, languageCode);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Group created successfully! Code: " + group.getGroupCode());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/admin/groups";
    }

    /**
     * Update group - NEW FORMAT
     * Validates input and ensures groupCode is regenerated
     */
    @PostMapping("/groups/update/{groupId}")
    public String updateGroup(@PathVariable Long groupId,
                              @RequestParam String programInitials,
                              @RequestParam Integer startYear,
                              @RequestParam(required = false) String languageCode,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            // Validate and normalize input
            programInitials = programInitials.trim().toUpperCase();
            if (languageCode != null && !languageCode.isEmpty()) {
                languageCode = languageCode.trim().toUpperCase();
            } else {
                languageCode = null; // Set to null if empty
            }

            // Validate programInitials (2-3 characters)
            if (programInitials.length() < 2 || programInitials.length() > 3) {
                throw new IllegalArgumentException("Program initials must be 2-3 characters");
            }

            // Validate startYear (0-99)
            if (startYear < 0 || startYear > 99) {
                throw new IllegalArgumentException("Start year must be between 0 and 99");
            }

            // Validate languageCode (single character if provided)
            if (languageCode != null && languageCode.length() != 1) {
                throw new IllegalArgumentException("Language code must be a single character");
            }

            updateEntityService.updateGroup(groupId, programInitials, startYear, languageCode);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Group updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error: " + e.getMessage());
        }

        return "redirect:/admin/groups";
    }

    /**
     * Delete group
     */
    @PostMapping("/groups/delete/{groupId}")
    public String deleteGroup(@PathVariable Long groupId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            adminService.deleteGroup(groupId);
            redirectAttributes.addFlashAttribute("successMessage", "Group deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/admin/groups";
    }

    // ==================== SUBJECT PAGES ====================

    /**
     * Subjects management page
     */
    @GetMapping("/subjects")
    public String subjectsPage(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("subjects", adminService.getAllSubjects());
        return "admin-subjects";
    }

    /**
     * Create subject
     */
    @PostMapping("/subjects/create")
    public String createSubject(@RequestParam String code,
                                @RequestParam Long credits,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            adminService.createSubject(code, Math.toIntExact(credits));
            redirectAttributes.addFlashAttribute("successMessage", "Subject created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/admin/subjects";
    }

    /**
     * Update subject
     */
    @PostMapping("/subjects/update/{subjectId}")
    public String updateSubject(@PathVariable Long subjectId,
                                @RequestParam(required = false) String code,
                                @RequestParam(required = false) Integer credits,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            updateEntityService.updateSubject(subjectId, code, credits);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Subject updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error: " + e.getMessage());
        }

        return "redirect:/admin/subjects";
    }

    /**
     * Delete subject
     */
    @PostMapping("/subjects/delete/{subjectId}")
    public String deleteSubject(@PathVariable Long subjectId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            adminService.deleteSubject(subjectId);
            redirectAttributes.addFlashAttribute("successMessage", "Subject deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/admin/subjects";
    }

    // ==================== ASSIGNMENT PAGES ====================

    /**
     * Assignments page
     */
    @GetMapping("/assignments")
    public String assignmentsPage(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("students", adminService.getAllStudents());
        model.addAttribute("teachers", adminService.getAllTeachers());
        model.addAttribute("groups", adminService.getAllGroups());
        model.addAttribute("subjects", adminService.getAllSubjects());
        return "admin-assignments";
    }

    /**
     * Assign teacher to subject
     */
    @PostMapping("/assignments/teacher-subject")
    public String assignTeacherToSubject(@RequestParam Long teacherId,
                                         @RequestParam Long subjectId,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            adminService.assignTeacherToSubject(teacherId, subjectId);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher assigned to subject successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/admin/assignments";
    }

    /**
     * Assign subject to group
     */
    @PostMapping("/assignments/group-subject")
    public String assignSubjectToGroup(@RequestParam Long groupId,
                                       @RequestParam Long subjectId,
                                       @RequestParam(required = false) String academicSemester,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            adminService.assignSubjectToGroup(groupId, subjectId, academicSemester);
            redirectAttributes.addFlashAttribute("successMessage", "Subject assigned to group successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/admin/assignments";
    }

    /**
     * Assign student to group
     */
    @PostMapping("/assignments/student-group")
    public String assignStudentToGroup(@RequestParam Long studentId,
                                       @RequestParam Long groupId,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login.html";
        }

        try {
            adminService.assignStudentToGroup(studentId, groupId);
            redirectAttributes.addFlashAttribute("successMessage", "Student assigned to group successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/admin/assignments";
    }

    /**
     * Logout - Invalidate session and redirect to login
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}