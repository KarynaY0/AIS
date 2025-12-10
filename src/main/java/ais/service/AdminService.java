package ais.service;

import ais.entity.*;
import ais.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Admin Service - Complete Implementation with SOLID Principles
 * Updated for new Group format with comprehensive exception handling
 *
 * SOLID Principles Applied:
 * - Single Responsibility: Each method has one clear purpose
 * - Open/Closed: Extensible through dependency injection, closed for modification
 * - Liskov Substitution: Properly implements service contracts
 * - Interface Segregation: Focused on admin-specific operations only
 * - Dependency Inversion: Depends on repository abstractions (interfaces)
 */
@Service
@Transactional
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final GroupSubjectRepository groupSubjectRepository;
    private final GradeRepository gradeRepository;

    @Autowired
    public AdminService(UserRepository userRepository,
                        StudentRepository studentRepository,
                        TeacherRepository teacherRepository,
                        GroupRepository groupRepository,
                        SubjectRepository subjectRepository,
                        TeacherSubjectRepository teacherSubjectRepository,
                        GroupSubjectRepository groupSubjectRepository,
                        GradeRepository gradeRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.groupRepository = groupRepository;
        this.subjectRepository = subjectRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.groupSubjectRepository = groupSubjectRepository;
        this.gradeRepository = gradeRepository;
    }

    // ==================== STUDENT MANAGEMENT ====================

    /**
     * Create a new student with validation
     * SRP: Single responsibility - creates student with proper validation
     *
     * @param firstName First name (will be username)
     * @param lastName Last name (will be password)
     * @param groupId Optional group ID to assign student to
     * @return Created Student entity
     * @throws IllegalArgumentException if validation fails
     */
    public Student createStudent(String firstName, String lastName, Long groupId) {
        logger.info("Attempting to create student with firstName: {}, groupId: {}", firstName, groupId);
        try {
            validateName(firstName, "First name");
            validateName(lastName, "Last name");

            if (userRepository.existsByUsername(firstName)) {
                logger.warn("Username already exists: {}", firstName);
                throw new IllegalArgumentException("Username already exists: " + firstName);
            }

            User user = new User(firstName, lastName);
            user = userRepository.save(user);
            logger.debug("User created with ID: {}", user.getUserId());

            Group group = findGroupById(groupId);
            Student student = new Student(user, group);
            student = studentRepository.save(student);

            logger.info("Successfully created student with userId: {}, username: {}",
                    student.getUser().getUserId(), student.getUser().getUsername());
            return student;
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error creating student: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating student: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create student. Please try again later.", e);
        }
    }

    /**
     * Delete student with proper cascade handling
     * SRP: Handles all related data cleanup in correct order
     *
     * Deletion order (maintains referential integrity):
     * 1. Delete all grades (child records referencing student)
     * 2. Delete student record
     * 3. Delete user record (parent record)
     *
     * @param studentUserId The user ID of the student to delete
     * @throws IllegalArgumentException if student not found
     */
    public void deleteStudent(Long studentUserId) {
        logger.info("Attempting to delete student with userId: {}", studentUserId);
        try {
            Student student = studentRepository.findByUser_UserId(studentUserId)
                    .orElseThrow(() -> {
                        logger.error("Student not found with user ID: {}", studentUserId);
                        return new IllegalArgumentException("Student not found with user ID: " + studentUserId);
                    });

            User user = student.getUser();

            // Step 1: Delete all grades associated with this student
            List<Grade> grades = gradeRepository.findByStudent_User_UserId(studentUserId);
            if (!grades.isEmpty()) {
                logger.debug("Deleting {} grades for student userId: {}", grades.size(), studentUserId);
                gradeRepository.deleteAll(grades);
            }

            // Step 2: Delete the student record
            studentRepository.delete(student);
            logger.debug("Deleted student record for userId: {}", studentUserId);

            // Step 3: Delete the user record
            if (user != null) {
                userRepository.delete(user);
                logger.debug("Deleted user record for userId: {}", studentUserId);
            }

            logger.info("Successfully deleted student with userId: {}", studentUserId);
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error deleting student: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error deleting student: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete student. Please try again later.", e);
        }
    }

    /**
     * Assign student to a group
     * SRP: Single purpose - manage student-group relationship
     *
     * @param studentUserId The user ID of the student
     * @param groupId The group ID to assign to
     * @return Updated Student entity
     * @throws IllegalArgumentException if student or group not found
     */
    public Student assignStudentToGroup(Long studentUserId, Long groupId) {
        logger.info("Assigning student userId: {} to groupId: {}", studentUserId, groupId);

        Student student = findStudentByUserId(studentUserId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> {
                    logger.error("Group not found with ID: {}", groupId);
                    return new IllegalArgumentException("Group not found with ID: " + groupId);
                });

        student.setGroup(group);
        student = studentRepository.save(student);

        logger.info("Successfully assigned student userId: {} to group: {}", studentUserId, group.getGroupCode());
        return student;
    }

    /**
     * Get all students in the system
     *
     * @return List of all students
     */
    public List<Student> getAllStudents() {
        logger.debug("Retrieving all students");
        List<Student> students = studentRepository.findAll();
        logger.debug("Found {} students", students.size());
        return students;
    }

    // ==================== TEACHER MANAGEMENT ====================

    /**
     * Create a new teacher with validation
     * SRP: Single responsibility - creates teacher with proper validation
     *
     * @param firstName First name (will be username)
     * @param lastName Last name (will be password)
     * @param department Optional department name
     * @return Created Teacher entity
     * @throws IllegalArgumentException if validation fails
     */
    public Teacher createTeacher(String firstName, String lastName, String department) {
        logger.info("Attempting to create teacher with firstName: {}, department: {}", firstName, department);
        try {
            validateName(firstName, "First name");
            validateName(lastName, "Last name");

            if (userRepository.existsByUsername(firstName)) {
                logger.warn("Username already exists: {}", firstName);
                throw new IllegalArgumentException("Username already exists: " + firstName);
            }

            User user = new User(firstName, lastName);
            user = userRepository.save(user);
            logger.debug("User created with ID: {}", user.getUserId());

            Teacher teacher = new Teacher(user, department);
            teacher = teacherRepository.save(teacher);

            logger.info("Successfully created teacher with userId: {}, username: {}",
                    teacher.getUser().getUserId(), teacher.getUser().getUsername());
            return teacher;
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error creating teacher: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating teacher: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create teacher. Please try again later.", e);
        }
    }

    /**
     * Delete teacher with proper cascade handling
     * SRP: Handles all related data cleanup in correct order
     *
     * Deletion order (maintains referential integrity):
     * 1. Delete all teacher-subject assignments
     * 2. Delete teacher record
     * 3. Delete user record
     *
     * Note: Grades are NOT deleted as they belong to students
     *
     * @param teacherUserId The user ID of the teacher to delete
     * @throws IllegalArgumentException if teacher not found
     */
    public void deleteTeacher(Long teacherUserId) {
        logger.info("Attempting to delete teacher with userId: {}", teacherUserId);
        try {
            Teacher teacher = teacherRepository.findByUser_UserId(teacherUserId)
                    .orElseThrow(() -> {
                        logger.error("Teacher not found with user ID: {}", teacherUserId);
                        return new IllegalArgumentException("Teacher not found with user ID: " + teacherUserId);
                    });

            User user = teacher.getUser();

            // Step 1: Delete all teacher-subject assignments
            List<TeacherSubject> assignments = teacherSubjectRepository.findByTeacher_User_UserId(teacherUserId);
            if (!assignments.isEmpty()) {
                logger.debug("Deleting {} teacher-subject assignments for teacher userId: {}",
                        assignments.size(), teacherUserId);
                teacherSubjectRepository.deleteAll(assignments);
            }

            // Step 2: Delete the teacher record
            teacherRepository.delete(teacher);
            logger.debug("Deleted teacher record for userId: {}", teacherUserId);

            // Step 3: Delete the user record
            if (user != null) {
                userRepository.delete(user);
                logger.debug("Deleted user record for userId: {}", teacherUserId);
            }

            logger.info("Successfully deleted teacher with userId: {}", teacherUserId);
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error deleting teacher: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error deleting teacher: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete teacher. Please try again later.", e);
        }
    }

    /**
     * Assign teacher to subject
     * SRP: Manages teacher-subject relationships
     *
     * @param teacherUserId The user ID of the teacher
     * @param subjectId The subject ID
     * @return Created TeacherSubject assignment
     * @throws IllegalArgumentException if teacher/subject not found or already assigned
     */
    public TeacherSubject assignTeacherToSubject(Long teacherUserId, Long subjectId) {
        logger.info("Assigning teacher userId: {} to subjectId: {}", teacherUserId, subjectId);

        Teacher teacher = findTeacherByUserId(teacherUserId);
        Subject subject = findSubjectById(subjectId);

        if (teacherSubjectRepository.existsByTeacherAndSubject(teacher, subject)) {
            logger.warn("Teacher userId: {} already assigned to subjectId: {}", teacherUserId, subjectId);
            throw new IllegalArgumentException("Teacher already assigned to this subject");
        }

        TeacherSubject assignment = new TeacherSubject(teacher, subject);
        assignment = teacherSubjectRepository.save(assignment);

        logger.info("Successfully assigned teacher userId: {} to subject: {}",
                teacherUserId, subject.getCode());
        return assignment;
    }

    /**
     * Remove teacher from subject
     *
     * @param teacherUserId The user ID of the teacher
     * @param subjectId The subject ID
     * @throws IllegalArgumentException if assignment not found
     */
    public void removeTeacherFromSubject(Long teacherUserId, Long subjectId) {
        logger.info("Removing teacher userId: {} from subjectId: {}", teacherUserId, subjectId);

        TeacherSubject assignment = teacherSubjectRepository
                .findByTeacherIdAndSubjectId(teacherUserId, subjectId)
                .orElseThrow(() -> {
                    logger.error("Assignment not found for teacher userId: {} and subjectId: {}",
                            teacherUserId, subjectId);
                    return new IllegalArgumentException("Assignment not found");
                });

        teacherSubjectRepository.delete(assignment);
        logger.info("Successfully removed teacher userId: {} from subjectId: {}", teacherUserId, subjectId);
    }

    /**
     * Get all teachers in the system
     *
     * @return List of all teachers
     */
    public List<Teacher> getAllTeachers() {
        logger.debug("Retrieving all teachers");
        List<Teacher> teachers = teacherRepository.findAll();
        logger.debug("Found {} teachers", teachers.size());
        return teachers;
    }

    // ==================== GROUP MANAGEMENT ====================

    /**
     * Create a new group with the new format
     * Format: [ProgramInitials][Year][LanguageCode]
     * Example: PI24E, CS23, BA24L
     *
     * @param programInitials Program initials (2-3 letters)
     * @param startYear Start year (2-digit: 23, 24, etc.)
     * @param languageCode Language code (1 letter, optional)
     * @return Created Group entity
     */
    public Group createGroup(String programInitials, Integer startYear, String languageCode) {
        logger.info("Attempting to create group with programInitials: {}, startYear: {}, languageCode: {}",
                programInitials, startYear, languageCode);
        try {
            // Normalize input
            programInitials = programInitials.trim().toUpperCase();
            if (languageCode != null && !languageCode.trim().isEmpty()) {
                languageCode = languageCode.trim().toUpperCase();
            } else {
                languageCode = null;
            }

            // Validate input
            if (programInitials.length() < 2 || programInitials.length() > 3) {
                logger.error("Invalid program initials length: {}", programInitials.length());
                throw new IllegalArgumentException("Program initials must be 2-3 characters");
            }
            if (startYear < 0 || startYear > 99) {
                logger.error("Invalid start year: {}", startYear);
                throw new IllegalArgumentException("Start year must be between 0 and 99");
            }
            if (languageCode != null && languageCode.length() != 1) {
                logger.error("Invalid language code length: {}", languageCode.length());
                throw new IllegalArgumentException("Language code must be a single character");
            }

            // Generate the group code
            StringBuilder groupCodeBuilder = new StringBuilder();
            groupCodeBuilder.append(programInitials);
            groupCodeBuilder.append(startYear);
            if (languageCode != null) {
                groupCodeBuilder.append(languageCode);
            }
            String groupCode = groupCodeBuilder.toString();

            if (groupRepository.existsByGroupCode(groupCode)) {
                logger.warn("Group with code {} already exists", groupCode);
                throw new IllegalArgumentException("Group with code " + groupCode + " already exists");
            }

            // Create new group
            Group group = new Group(groupCode, programInitials, startYear, languageCode);

            logger.debug("Creating group - GroupCode: {}, ProgramInitials: {}, StartYear: {}, LanguageCode: {}",
                    group.getGroupCode(), group.getProgramInitials(),
                    group.getStartYear(), group.getLanguageCode());

            group = groupRepository.save(group);
            logger.info("Successfully created group with code: {}", group.getGroupCode());
            return group;
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error creating group: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create group. Please try again later.", e);
        }
    }

    /**
     * Delete group
     * Note: Students in this group will have their group set to null (not deleted)
     *
     * @param groupId The group ID to delete
     * @throws IllegalArgumentException if group not found
     */
    public void deleteGroup(Long groupId) {
        logger.info("Attempting to delete group with ID: {}", groupId);
        try {
            if (!groupRepository.existsById(groupId)) {
                logger.error("Group not found with ID: {}", groupId);
                throw new IllegalArgumentException("Group not found with ID: " + groupId);
            }

            groupRepository.deleteById(groupId);
            logger.info("Successfully deleted group with ID: {}", groupId);
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error deleting group: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error deleting group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete group. Please try again later.", e);
        }
    }

    /**
     * Assign subject to group
     *
     * @param groupId The group ID
     * @param subjectId The subject ID
     * @param academicSemester Optional academic semester
     * @return Created GroupSubject assignment
     * @throws IllegalArgumentException if group/subject not found or already assigned
     */
    public GroupSubject assignSubjectToGroup(Long groupId, Long subjectId, String academicSemester) {
        logger.info("Assigning subjectId: {} to groupId: {} for semester: {}",
                subjectId, groupId, academicSemester);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> {
                    logger.error("Group not found with ID: {}", groupId);
                    return new IllegalArgumentException("Group not found with ID: " + groupId);
                });

        Subject subject = findSubjectById(subjectId);

        if (groupSubjectRepository.existsByGroupAndSubject(group, subject)) {
            logger.warn("Subject already assigned to group - subjectId: {}, groupId: {}",
                    subjectId, groupId);
            throw new IllegalArgumentException("Subject already assigned to this group");
        }

        GroupSubject assignment = new GroupSubject(group, subject, academicSemester);
        assignment = groupSubjectRepository.save(assignment);

        logger.info("Successfully assigned subject: {} to group: {}",
                subject.getCode(), group.getGroupCode());
        return assignment;
    }

    /**
     * Remove subject from group
     *
     * @param groupId The group ID
     * @param subjectId The subject ID
     * @throws IllegalArgumentException if assignment not found
     */
    public void removeSubjectFromGroup(Long groupId, Long subjectId) {
        logger.info("Removing subjectId: {} from groupId: {}", subjectId, groupId);

        GroupSubject assignment = groupSubjectRepository
                .findByGroupIdAndSubjectId(groupId, subjectId)
                .orElseThrow(() -> {
                    logger.error("Assignment not found for groupId: {} and subjectId: {}",
                            groupId, subjectId);
                    return new IllegalArgumentException("Assignment not found");
                });

        groupSubjectRepository.delete(assignment);
        logger.info("Successfully removed subjectId: {} from groupId: {}", subjectId, groupId);
    }

    /**
     * Get all groups in the system
     *
     * @return List of all groups
     */
    public List<Group> getAllGroups() {
        logger.debug("Retrieving all groups");
        List<Group> groups = groupRepository.findAll();
        logger.debug("Found {} groups", groups.size());
        return groups;
    }

    // ==================== SUBJECT MANAGEMENT ====================

    /**
     * Create a new subject
     *
     * @param code Subject code (must be unique)
     * @param credits Number of credits
     * @return Created Subject entity
     * @throws IllegalArgumentException if code is empty or already exists
     */
    public Subject createSubject(String code, Integer credits) {
        logger.info("Attempting to create subject with code: {}, credits: {}", code, credits);
        try {
            if (code == null || code.trim().isEmpty()) {
                logger.error("Subject code is required but was null or empty");
                throw new IllegalArgumentException("Subject code is required");
            }

            if (subjectRepository.existsByCode(code)) {
                logger.warn("Subject code already exists: {}", code);
                throw new IllegalArgumentException("Subject code already exists: " + code);
            }

            Subject subject = new Subject(code, credits);
            subject = subjectRepository.save(subject);

            logger.info("Successfully created subject with code: {}", code);
            return subject;
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error creating subject: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating subject: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create subject. Please try again later.", e);
        }
    }

    /**
     * Delete subject
     * Note: This will cascade delete related assignments and grades
     *
     * @param subjectId The subject ID to delete
     * @throws IllegalArgumentException if subject not found
     */
    public void deleteSubject(Long subjectId) {
        logger.info("Attempting to delete subject with ID: {}", subjectId);
        try {
            if (!subjectRepository.existsById(subjectId)) {
                logger.error("Subject not found with ID: {}", subjectId);
                throw new IllegalArgumentException("Subject not found with ID: " + subjectId);
            }

            subjectRepository.deleteById(subjectId);
            logger.info("Successfully deleted subject with ID: {}", subjectId);
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error deleting subject: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error deleting subject: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete subject. Please try again later.", e);
        }
    }

    /**
     * Get all subjects in the system
     *
     * @return List of all subjects
     */
    public List<Subject> getAllSubjects() {
        logger.debug("Retrieving all subjects");
        List<Subject> subjects = subjectRepository.findAll();
        logger.debug("Found {} subjects", subjects.size());
        return subjects;
    }

    // ==================== PRIVATE HELPER METHODS ====================
    // DRY Principle: Reusable validation and lookup methods

    /**
     * Validate name fields
     * SRP: Single validation logic for reuse
     *
     * @param name The name to validate
     * @param fieldName The field name for error messages
     * @throws IllegalArgumentException if name is null or empty
     */
    private void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            logger.error("{} validation failed - null or empty", fieldName);
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    /**
     * Find group by ID or return null if ID is null
     * DRY: Reusable group lookup
     *
     * @param groupId The group ID (can be null)
     * @return Group entity or null
     */
    private Group findGroupById(Long groupId) {
        return groupId != null ? groupRepository.findById(groupId).orElse(null) : null;
    }

    /**
     * Find student by user ID with error handling
     * DRY: Reusable student lookup
     *
     * @param userId The user ID
     * @return Student entity
     * @throws IllegalArgumentException if student not found
     */
    private Student findStudentByUserId(Long userId) {
        return studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> {
                    logger.error("Student not found with user ID: {}", userId);
                    return new IllegalArgumentException("Student not found with user ID: " + userId);
                });
    }

    /**
     * Find teacher by user ID with error handling
     * DRY: Reusable teacher lookup
     *
     * @param userId The user ID
     * @return Teacher entity
     * @throws IllegalArgumentException if teacher not found
     */
    private Teacher findTeacherByUserId(Long userId) {
        return teacherRepository.findByUser_UserId(userId)
                .orElseThrow(() -> {
                    logger.error("Teacher not found with user ID: {}", userId);
                    return new IllegalArgumentException("Teacher not found with user ID: " + userId);
                });
    }

    /**
     * Find subject by ID with error handling
     * DRY: Reusable subject lookup
     *
     * @param subjectId The subject ID
     * @return Subject entity
     * @throws IllegalArgumentException if subject not found
     */
    private Subject findSubjectById(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> {
                    logger.error("Subject not found with ID: {}", subjectId);
                    return new IllegalArgumentException("Subject not found with ID: " + subjectId);
                });
    }
}