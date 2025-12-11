package ais.service;

import ais.entity.*;
import ais.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UpdateEntityService - Handles entity updates with SOLID principles
 */
@Service
@Transactional
public class UpdateEntityService {

    // Dependencies injected via constructor (DIP - Dependency Inversion Principle)
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final GroupService groupService;

    @Autowired
    public UpdateEntityService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            GroupRepository groupRepository,
            SubjectRepository subjectRepository,
            GroupService groupService) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.groupRepository = groupRepository;
        this.subjectRepository = subjectRepository;
        this.groupService = groupService;
    }

    /**
     * Update student information
     *
     * @param studentUserId The user ID of the student to update
     * @param newUsername New username (optional, null to keep current)
     * @param newPassword New password (optional, null to keep current)
     * @param newGroupId New group ID (optional, null to keep current)
     * @return Updated Student entity
     * @throws IllegalArgumentException if student not found or validation fails
     */
    public Student updateStudent(Long studentUserId, String newUsername,
                                 String newPassword, Long newGroupId) {
        // Find existing student
        Student student = findStudentByUserId(studentUserId);
        User user = student.getUser();

        // Update username if provided and different
        if (newUsername != null && !newUsername.trim().isEmpty()
                && !newUsername.equals(user.getUsername())) {
            validateUsernameNotTaken(newUsername, user.getUserId());
            user.setUsername(newUsername);
        }

        // Update password if provided
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            validatePassword(newPassword);
            user.setPassword(newPassword);
        }

        // Update group if provided
        if (newGroupId != null) {
            Group group = findGroupById(newGroupId);
            student.setGroup(group);
        }

        // Save changes (cascades to user due to entity relationships)
        userRepository.save(user);
        return studentRepository.save(student);
    }

    /**
     * Update teacher information
     */
    public Teacher updateTeacher(Long teacherUserId, String newUsername,
                                 String newPassword, String newDepartment) {
        // Find existing teacher
        Teacher teacher = findTeacherByUserId(teacherUserId);
        User user = teacher.getUser();

        // Update username if provided and different
        if (newUsername != null && !newUsername.trim().isEmpty()
                && !newUsername.equals(user.getUsername())) {
            validateUsernameNotTaken(newUsername, user.getUserId());
            user.setUsername(newUsername);
        }

        // Update password if provided
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            validatePassword(newPassword);
            user.setPassword(newPassword);
        }

        // Update department if provided
        if (newDepartment != null && !newDepartment.trim().isEmpty()) {
            teacher.setDepartment(newDepartment);
        }

        // Save changes
        userRepository.save(user);
        return teacherRepository.save(teacher);
    }

    /**
     * Update group - NEW FORMAT
     *
     * @param groupId Group ID to update
     * @param newProgramInitials New program initials (2-3 letters)
     * @param newStartYear New start year (2-digit)
     * @param newLanguageCode New language code (1 letter, optional)
     * @return Updated Group entity
     */
    public Group updateGroup(Long groupId, String newProgramInitials,
                             Integer newStartYear, String newLanguageCode) {
        return groupService.updateGroup(groupId, newProgramInitials, newStartYear, newLanguageCode);
    }

    /**
     * Update subject
     */
    public Subject updateSubject(Long subjectId, String newCode, Integer newCredits) {
        Subject subject = findSubjectById(subjectId);

        // Update code if provided and different
        if (newCode != null && !newCode.trim().isEmpty()
                && !newCode.equals(subject.getCode())) {
            validateSubjectCodeNotTaken(newCode, subjectId);
            subject.setCode(newCode);
        }

        // Update credits if provided
        if (newCredits != null) {
            validateCredits(newCredits);
            subject.setCredits(newCredits);
        }

        return subjectRepository.save(subject);
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validate that username is not already taken by another user
     * SRP: Single validation responsibility
     */
    private void validateUsernameNotTaken(String username, Long currentUserId) {
        userRepository.findByUsername(username).ifPresent(existingUser -> {
            if (!existingUser.getUserId().equals(currentUserId)) {
                throw new IllegalArgumentException(
                        "Username '" + username + "' is already taken");
            }
        });
    }

    /**
     * Validate that subject code is not already taken by another subject
     * SRP: Single validation responsibility
     */
    private void validateSubjectCodeNotTaken(String code, Long currentSubjectId) {
        subjectRepository.findByCode(code).ifPresent(existingSubject -> {
            if (!existingSubject.getSubjectId().equals(currentSubjectId)) {
                throw new IllegalArgumentException(
                        "Subject code '" + code + "' is already taken");
            }
        });
    }

    /**
     * Validate password requirements
     * SRP: Single validation responsibility
     */
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 3) {
            throw new IllegalArgumentException("Password must be at least 3 characters");
        }
    }

    /**
     * Validate credits value
     * SRP: Single validation responsibility
     */
    private void validateCredits(Integer credits) {
        if (credits <= 0) {
            throw new IllegalArgumentException("Credits must be positive");
        }
    }

    // ==================== PRIVATE FINDER METHODS ====================
    // DRY Principle: Reusable entity lookup with consistent error handling

    /**
     * Find student by user ID with proper error handling
     * DRY: Reusable lookup logic
     */
    private Student findStudentByUserId(Long userId) {
        return studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student not found with user ID: " + userId));
    }

    /**
     * Find teacher by user ID with proper error handling
     * DRY: Reusable lookup logic
     */
    private Teacher findTeacherByUserId(Long userId) {
        return teacherRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Teacher not found with user ID: " + userId));
    }

    /**
     * Find group by ID with proper error handling
     * DRY: Reusable lookup logic
     */
    private Group findGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Group not found with ID: " + groupId));
    }

    /**
     * Find subject by ID with proper error handling
     * DRY: Reusable lookup logic
     */
    private Subject findSubjectById(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subject not found with ID: " + subjectId));
    }
}