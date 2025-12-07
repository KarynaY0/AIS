package ais.controller;

import ais.entity.*;
import ais.repository.*;
import ais.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database Test Controller
 * Provides REST API endpoints for testing database operations
 * WARNING: This controller is for testing purposes only - do not use in production
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:8080")
public class DatabaseTestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private TeacherSubjectRepository teacherSubjectRepository;

    @Autowired
    private GroupSubjectRepository groupSubjectRepository;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private TeacherService teacherService;

    // ==================== SYSTEM INFO ====================

    @GetMapping("/system-info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("totalUsers", userRepository.count());
        info.put("totalAdmins", adminRepository.count());
        info.put("totalStudents", studentRepository.count());
        info.put("totalTeachers", teacherRepository.count());
        info.put("totalSubjects", subjectRepository.count());
        info.put("totalGroups", groupRepository.count());
        info.put("totalGrades", gradeRepository.count());
        info.put("totalTeacherSubjectAssignments", teacherSubjectRepository.count());
        info.put("totalGroupSubjectAssignments", groupSubjectRepository.count());
        info.put("status", "Database is operational");
        return ResponseEntity.ok(info);
    }

    // ==================== USER OPERATIONS ====================

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");

            if (username == null || password == null) {
                return ResponseEntity.badRequest().build();
            }

            if (userRepository.existsByUsername(username)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            User user = new User(username, password);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(userRepository.save(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ADMIN OPERATIONS ====================

    @GetMapping("/admins")
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(adminRepository.findAll());
    }

    @PostMapping("/admins")
    public ResponseEntity<Admin> createAdmin(@RequestBody Map<String, Long> request) {
        try {
            Long userId = request.get("userId");
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (adminRepository.existsByUser(user)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            Admin admin = new Admin(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(adminRepository.save(admin));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== STUDENT OPERATIONS ====================

    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentRepository.findAll());
    }

    @GetMapping("/students/user/{userId}")
    public ResponseEntity<Student> getStudentByUserId(@PathVariable Long userId) {
        return studentRepository.findByUser_UserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/students/group/{groupId}")
    public ResponseEntity<List<Student>> getStudentsByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(studentRepository.findByGroup_GroupId(groupId));
    }

    @PostMapping("/students")
    public ResponseEntity<Student> createStudent(@RequestBody Map<String, Long> request) {
        try {
            Long userId = request.get("userId");
            Long groupId = request.get("groupId");

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (studentRepository.existsByUser(user)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            Group group = null;
            if (groupId != null) {
                group = groupRepository.findById(groupId).orElse(null);
            }

            Student student = new Student(user, group);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(studentRepository.save(student));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/students/{studentId}/group/{groupId}")
    public ResponseEntity<Student> assignStudentToGroup(
            @PathVariable Long studentId,
            @PathVariable Long groupId) {
        try {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found"));
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new IllegalArgumentException("Group not found"));

            student.setGroup(group);
            return ResponseEntity.ok(studentRepository.save(student));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== TEACHER OPERATIONS ====================

    @GetMapping("/teachers")
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        return ResponseEntity.ok(teacherRepository.findAll());
    }

    @GetMapping("/teachers/user/{userId}")
    public ResponseEntity<Teacher> getTeacherByUserId(@PathVariable Long userId) {
        return teacherRepository.findByUser_UserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/teachers/department/{department}")
    public ResponseEntity<List<Teacher>> getTeachersByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(teacherRepository.findByDepartment(department));
    }

    @PostMapping("/teachers")
    public ResponseEntity<Teacher> createTeacher(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String department = (String) request.get("department");

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (teacherRepository.existsByUser(user)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            Teacher teacher = new Teacher(user, department);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(teacherRepository.save(teacher));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== SUBJECT OPERATIONS ====================

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        return ResponseEntity.ok(subjectRepository.findAll());
    }

    @GetMapping("/subjects/{id}")
    public ResponseEntity<Subject> getSubjectById(@PathVariable Long id) {
        return subjectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/subjects/code/{code}")
    public ResponseEntity<Subject> getSubjectByCode(@PathVariable String code) {
        return subjectRepository.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/subjects")
    public ResponseEntity<Subject> createSubject(@RequestBody Map<String, Object> request) {
        try {
            String code = (String) request.get("code");
            Integer credits = Integer.valueOf(request.get("credits").toString());

            Subject subject = subjectService.createSubject(code, credits);
            return ResponseEntity.status(HttpStatus.CREATED).body(subject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/subjects/{id}")
    public ResponseEntity<Subject> updateSubject(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            String code = (String) request.get("code");
            Integer credits = Integer.valueOf(request.get("credits").toString());

            Subject subject = subjectService.updateSubject(id, code, credits);
            return ResponseEntity.ok(subject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        try {
            subjectService.deleteSubject(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== GROUP OPERATIONS ====================

    @GetMapping("/groups")
    public ResponseEntity<List<Group>> getAllGroups() {
        return ResponseEntity.ok(groupRepository.findAll());
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        return groupRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/groups")
    public ResponseEntity<Group> createGroup(@RequestBody Map<String, String> request) {
        try {
            String courseYear = request.get("courseYear");
            Group group = groupService.createGroup(courseYear);
            return ResponseEntity.status(HttpStatus.CREATED).body(group);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/groups/{id}")
    public ResponseEntity<Group> updateGroup(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String courseYear = request.get("courseYear");
            Group group = groupService.updateGroup(id, courseYear);
            return ResponseEntity.ok(group);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== TEACHER-SUBJECT ASSIGNMENTS ====================

    @PostMapping("/teacher-subjects")
    public ResponseEntity<TeacherSubject> assignTeacherToSubject(
            @RequestBody Map<String, Long> request) {
        try {
            Long teacherUserId = request.get("teacherUserId");
            Long subjectId = request.get("subjectId");

            Teacher teacher = teacherRepository.findByUser_UserId(teacherUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

            if (teacherSubjectRepository.existsByTeacherAndSubject(teacher, subject)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            TeacherSubject ts = new TeacherSubject(teacher, subject);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(teacherSubjectRepository.save(ts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/teacher-subjects")
    public ResponseEntity<List<TeacherSubject>> getAllTeacherSubjects() {
        return ResponseEntity.ok(teacherSubjectRepository.findAll());
    }

    @GetMapping("/teacher-subjects/teacher/{teacherUserId}")
    public ResponseEntity<List<Subject>> getSubjectsByTeacher(@PathVariable Long teacherUserId) {
        return ResponseEntity.ok(subjectRepository.findByTeacherId(teacherUserId));
    }

    @DeleteMapping("/teacher-subjects/teacher/{teacherUserId}/subject/{subjectId}")
    public ResponseEntity<Void> removeTeacherFromSubject(
            @PathVariable Long teacherUserId,
            @PathVariable Long subjectId) {
        try {
            TeacherSubject ts = teacherSubjectRepository
                    .findByTeacherIdAndSubjectId(teacherUserId, subjectId)
                    .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

            teacherSubjectRepository.delete(ts);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== GROUP-SUBJECT ASSIGNMENTS ====================

    @PostMapping("/group-subjects")
    public ResponseEntity<GroupSubject> assignSubjectToGroup(
            @RequestBody Map<String, Object> request) {
        try {
            Long groupId = Long.valueOf(request.get("groupId").toString());
            Long subjectId = Long.valueOf(request.get("subjectId").toString());
            String semester = (String) request.get("academicSemester");

            GroupSubject gs = groupService.assignSubject(groupId, subjectId, semester);
            return ResponseEntity.status(HttpStatus.CREATED).body(gs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/group-subjects")
    public ResponseEntity<List<GroupSubject>> getAllGroupSubjects() {
        return ResponseEntity.ok(groupSubjectRepository.findAll());
    }

    @GetMapping("/group-subjects/group/{groupId}")
    public ResponseEntity<List<Subject>> getSubjectsByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(subjectRepository.findByGroupId(groupId));
    }

    @DeleteMapping("/group-subjects/group/{groupId}/subject/{subjectId}")
    public ResponseEntity<Void> removeSubjectFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long subjectId) {
        try {
            groupService.removeSubject(groupId, subjectId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== GRADE OPERATIONS ====================

    @GetMapping("/grades")
    public ResponseEntity<List<Grade>> getAllGrades() {
        return ResponseEntity.ok(gradeRepository.findAll());
    }

    @GetMapping("/grades/{id}")
    public ResponseEntity<Grade> getGradeById(@PathVariable Long id) {
        return gradeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/grades/student/{studentUserId}")
    public ResponseEntity<List<Grade>> getGradesByStudent(@PathVariable Long studentUserId) {
        return ResponseEntity.ok(gradeRepository.findByStudent_User_UserId(studentUserId));
    }

    @GetMapping("/grades/subject/{subjectId}")
    public ResponseEntity<List<Grade>> getGradesBySubject(@PathVariable Long subjectId) {
        return ResponseEntity.ok(gradeRepository.findBySubject_SubjectId(subjectId));
    }

    @PostMapping("/grades")
    public ResponseEntity<Grade> createGrade(@RequestBody Map<String, Object> request) {
        try {
            Long studentUserId = Long.valueOf(request.get("studentUserId").toString());
            Long subjectId = Long.valueOf(request.get("subjectId").toString());
            BigDecimal gradeValue = new BigDecimal(request.get("gradeValue").toString());
            String comment = (String) request.get("comment");

            Grade grade = gradeService.createGrade(studentUserId, subjectId, gradeValue, comment);
            return ResponseEntity.status(HttpStatus.CREATED).body(grade);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/grades/{id}")
    public ResponseEntity<Grade> updateGrade(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            BigDecimal gradeValue = new BigDecimal(request.get("gradeValue").toString());
            String comment = (String) request.get("comment");

            Grade grade = gradeService.updateGrade(id, gradeValue, comment);
            return ResponseEntity.ok(grade);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/grades/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        try {
            gradeService.deleteGrade(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/grades/student/{studentUserId}/average")
    public ResponseEntity<Map<String, Object>> getStudentAverage(@PathVariable Long studentUserId) {
        BigDecimal average = gradeService.calculateStudentAverage(studentUserId);
        Map<String, Object> response = new HashMap<>();
        response.put("studentUserId", studentUserId);
        response.put("average", average);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/grades/subject/{subjectId}/average")
    public ResponseEntity<Map<String, Object>> getSubjectAverage(@PathVariable Long subjectId) {
        BigDecimal average = gradeService.calculateSubjectAverage(subjectId);
        Map<String, Object> response = new HashMap<>();
        response.put("subjectId", subjectId);
        response.put("average", average);
        return ResponseEntity.ok(response);
    }

    // ==================== HEALTH CHECK ====================

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("message", "Database Test Controller is running");
        return ResponseEntity.ok(health);
    }
}