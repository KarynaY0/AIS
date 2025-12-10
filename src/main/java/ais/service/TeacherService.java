package ais.service;

import ais.entity.*;
import ais.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Teacher operations
 * Handles business logic for managing and viewing grades for subjects taught by teachers
 * Updated with comprehensive exception handling
 */
@Service
@Transactional
public class TeacherService {

    private static final Logger logger = LoggerFactory.getLogger(TeacherService.class);

    private final TeacherRepository teacherRepository;
    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;

    @Autowired
    public TeacherService(TeacherRepository teacherRepository,
                          GradeRepository gradeRepository,
                          StudentRepository studentRepository,
                          SubjectRepository subjectRepository,
                          TeacherSubjectRepository teacherSubjectRepository) {
        this.teacherRepository = teacherRepository;
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
    }

    // ==================== Grade Management ====================

    /**
     * Enter a new grade for a student in a subject (allows multiple grades)
     * Teacher can only enter grades for subjects they teach
     * @param teacherUserId the teacher's user ID
     * @param studentUserId the student's user ID
     * @param subjectId the subject ID
     * @param gradeValue the grade value (0-100)
     * @param comment optional comment
     * @return created Grade entity
     * @throws IllegalArgumentException if validation fails
     */
    public Grade enterGrade(Long teacherUserId, Long studentUserId, Long subjectId,
                            BigDecimal gradeValue, String comment) {
        logger.info("Teacher userId: {} entering grade for student userId: {} in subject: {}",
                teacherUserId, studentUserId, subjectId);
        try {
            // Validate teacher teaches this subject
            validateTeacherTeachesSubject(teacherUserId, subjectId);

            // Validate grade value
            if (gradeValue == null || gradeValue.compareTo(BigDecimal.ZERO) < 0
                    || gradeValue.compareTo(new BigDecimal("100")) > 0) {
                logger.error("Invalid grade value: {}", gradeValue);
                throw new IllegalArgumentException("Grade value must be between 0 and 100");
            }

            // Find student and subject
            Student student = studentRepository.findByUser_UserId(studentUserId)
                    .orElseThrow(() -> {
                        logger.error("Student not found with user ID: {}", studentUserId);
                        return new IllegalArgumentException("Student not found with user ID: " + studentUserId);
                    });
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> {
                        logger.error("Subject not found with ID: {}", subjectId);
                        return new IllegalArgumentException("Subject not found with ID: " + subjectId);
                    });

            // Create and save grade
            Grade grade = new Grade();
            grade.setStudent(student);
            grade.setSubject(subject);
            grade.setGradeValue(gradeValue);
            grade.setComment(comment);
            grade.setUpdatedTime(LocalDateTime.now());

            grade = gradeRepository.save(grade);
            logger.info("Successfully entered grade {} for student userId: {} in subject: {}",
                    gradeValue, studentUserId, subject.getCode());
            return grade;
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error entering grade: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error entering grade: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to enter grade. Please try again later.", e);
        }
    }

    /**
     * Save a grade directly (helper method for controller)
     */
    public Grade saveGrade(Grade grade) {
        logger.debug("Saving grade for student: {}, subject: {}",
                grade.getStudent().getUser().getUsername(),
                grade.getSubject().getCode());
        return gradeRepository.save(grade);
    }

    /**
     * Get student by user ID (helper method)
     */
    public Student getStudentByUserId(Long userId) {
        logger.debug("Retrieving student with userId: {}", userId);
        return studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> {
                    logger.error("Student not found with user ID: {}", userId);
                    return new IllegalArgumentException("Student not found with user ID: " + userId);
                });
    }

    /**
     * Get subject by ID (helper method)
     */
    public Subject getSubjectById(Long subjectId) {
        logger.debug("Retrieving subject with ID: {}", subjectId);
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> {
                    logger.error("Subject not found with ID: {}", subjectId);
                    return new IllegalArgumentException("Subject not found with ID: " + subjectId);
                });
    }

    /**
     * Edit an existing grade
     * Teacher can only edit grades for subjects they teach
     * @param teacherUserId the teacher's user ID
     * @param gradeId the grade ID to edit
     * @param newGradeValue the new grade value
     * @param newComment the new comment
     * @return updated Grade entity
     * @throws IllegalArgumentException if validation fails
     */
    public Grade editGrade(Long teacherUserId, Long gradeId, BigDecimal newGradeValue, String newComment) {
        logger.info("Teacher userId: {} editing grade ID: {}", teacherUserId, gradeId);

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> {
                    logger.error("Grade not found with ID: {}", gradeId);
                    return new IllegalArgumentException("Grade not found with ID: " + gradeId);
                });

        // Validate teacher teaches this subject
        validateTeacherTeachesSubject(teacherUserId, grade.getSubject().getSubjectId());

        // Validate grade value
        if (newGradeValue.compareTo(BigDecimal.ZERO) < 0 || newGradeValue.compareTo(new BigDecimal("100")) > 0) {
            logger.error("Invalid new grade value: {}", newGradeValue);
            throw new IllegalArgumentException("Grade value must be between 0 and 100");
        }

        // Update grade
        grade.setGradeValue(newGradeValue);
        grade.setComment(newComment);
        grade.setUpdatedTime(LocalDateTime.now());

        grade = gradeRepository.save(grade);
        logger.info("Successfully updated grade ID: {} to value: {}", gradeId, newGradeValue);
        return grade;
    }

    /**
     * Delete a grade
     * Teacher can only delete grades for subjects they teach
     * @param teacherUserId the teacher's user ID
     * @param gradeId the grade ID to delete
     * @throws IllegalArgumentException if validation fails
     */
    public void deleteGrade(Long teacherUserId, Long gradeId) {
        logger.info("Teacher userId: {} deleting grade ID: {}", teacherUserId, gradeId);

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> {
                    logger.error("Grade not found with ID: {}", gradeId);
                    return new IllegalArgumentException("Grade not found with ID: " + gradeId);
                });

        // Validate teacher teaches this subject
        validateTeacherTeachesSubject(teacherUserId, grade.getSubject().getSubjectId());

        gradeRepository.delete(grade);
        logger.info("Successfully deleted grade ID: {}", gradeId);
    }

    // ==================== View Grade Information ====================

    /**
     * Get all grades for a subject taught by the teacher
     * Filters out grades with deleted students
     * @param teacherUserId the teacher's user ID
     * @param subjectId the subject ID
     * @return List of grades for the subject (excluding orphaned grades)
     * @throws IllegalArgumentException if teacher doesn't teach this subject
     */
    public List<Grade> getGradesBySubject(Long teacherUserId, Long subjectId) {
        logger.debug("Retrieving grades for teacher userId: {} in subject: {}", teacherUserId, subjectId);

        validateTeacherTeachesSubject(teacherUserId, subjectId);
        List<Grade> grades = gradeRepository.findBySubject_SubjectId(subjectId);

        // Filter out grades with deleted students
        List<Grade> filteredGrades = grades.stream()
                .filter(grade -> {
                    try {
                        grade.getStudent().getUser().getUserId();
                        return true;
                    } catch (Exception e) {
                        logger.warn("Filtering out grade with deleted student - grade ID: {}",
                                grade.getGradeId());
                        return false;
                    }
                })
                .collect(Collectors.toList());

        logger.debug("Found {} grades (after filtering) for subject: {}", filteredGrades.size(), subjectId);
        return filteredGrades;
    }

    /**
     * Get all grades for a specific student in a subject
     * @param teacherUserId the teacher's user ID
     * @param studentUserId the student's user ID
     * @param subjectId the subject ID
     * @return List of grades for the student in the subject
     * @throws IllegalArgumentException if teacher doesn't teach this subject
     */
    public List<Grade> getStudentGradesInSubject(Long teacherUserId, Long studentUserId, Long subjectId) {
        logger.debug("Retrieving grades for student userId: {} in subject: {} by teacher userId: {}",
                studentUserId, subjectId, teacherUserId);

        validateTeacherTeachesSubject(teacherUserId, subjectId);
        List<Grade> grades = gradeRepository.findByStudent_User_UserId(studentUserId).stream()
                .filter(grade -> grade.getSubject().getSubjectId().equals(subjectId))
                .collect(Collectors.toList());

        logger.debug("Found {} grades for student userId: {} in subject: {}",
                grades.size(), studentUserId, subjectId);
        return grades;
    }

    /**
     * Get all grades for students in a specific group and subject
     * @param teacherUserId the teacher's user ID
     * @param groupId the group ID
     * @param subjectId the subject ID
     * @return List of grades for the group in the subject
     * @throws IllegalArgumentException if teacher doesn't teach this subject
     */
    public List<Grade> getGradesByGroupAndSubject(Long teacherUserId, Long groupId, Long subjectId) {
        logger.debug("Retrieving grades for group: {} in subject: {} by teacher userId: {}",
                groupId, subjectId, teacherUserId);

        validateTeacherTeachesSubject(teacherUserId, subjectId);
        List<Grade> grades = gradeRepository.findByGroupIdAndSubjectId(groupId, subjectId);

        // Filter out grades with deleted students
        List<Grade> filteredGrades = grades.stream()
                .filter(grade -> {
                    try {
                        grade.getStudent().getUser().getUserId();
                        return true;
                    } catch (Exception e) {
                        logger.warn("Filtering out grade with deleted student - grade ID: {}",
                                grade.getGradeId());
                        return false;
                    }
                })
                .collect(Collectors.toList());

        logger.debug("Found {} grades for group: {} in subject: {}",
                filteredGrades.size(), groupId, subjectId);
        return filteredGrades;
    }

    /**
     * Get all subjects taught by a teacher
     * @param teacherUserId the teacher's user ID
     * @return List of subjects taught by the teacher
     */
    public List<Subject> getSubjectsTaughtByTeacher(Long teacherUserId) {
        logger.debug("Retrieving subjects taught by teacher userId: {}", teacherUserId);
        List<Subject> subjects = subjectRepository.findByTeacherId(teacherUserId);
        logger.debug("Teacher userId: {} teaches {} subjects", teacherUserId, subjects.size());
        return subjects;
    }

    /**
     * Get all students taught by a teacher (across all subjects)
     * @param teacherUserId the teacher's user ID
     * @return List of students taught by the teacher
     */
    public List<Student> getStudentsTaughtByTeacher(Long teacherUserId) {
        logger.debug("Retrieving students taught by teacher userId: {}", teacherUserId);
        List<Student> students = studentRepository.findByTeacherId(teacherUserId);
        logger.debug("Teacher userId: {} teaches {} students", teacherUserId, students.size());
        return students;
    }

    /**
     * Get all groups taught by a teacher
     * @param teacherUserId the teacher's user ID
     * @return List of groups taught by the teacher
     */
    public List<Group> getGroupsTaughtByTeacher(Long teacherUserId) {
        logger.debug("Retrieving groups taught by teacher userId: {}", teacherUserId);

        Teacher teacher = teacherRepository.findByUser_UserId(teacherUserId)
                .orElseThrow(() -> {
                    logger.error("Teacher not found with user ID: {}", teacherUserId);
                    return new IllegalArgumentException("Teacher not found with user ID: " + teacherUserId);
                });

        List<Group> groups = teacher.getTeacherSubjects().stream()
                .flatMap(ts -> ts.getSubject().getGroupSubjects().stream())
                .map(GroupSubject::getGroup)
                .distinct()
                .collect(Collectors.toList());

        logger.debug("Teacher userId: {} teaches {} groups", teacherUserId, groups.size());
        return groups;
    }

    // ==================== Statistics and Reports ====================

    /**
     * Calculate average grade for a subject
     * @param teacherUserId the teacher's user ID
     * @param subjectId the subject ID
     * @return average grade value
     * @throws IllegalArgumentException if teacher doesn't teach this subject
     */
    public BigDecimal calculateAverageGradeForSubject(Long teacherUserId, Long subjectId) {
        logger.debug("Calculating average grade for subject: {} by teacher userId: {}",
                subjectId, teacherUserId);

        validateTeacherTeachesSubject(teacherUserId, subjectId);
        BigDecimal average = gradeRepository.calculateAverageBySubjectId(subjectId);
        BigDecimal result = average != null ? average : BigDecimal.ZERO;

        logger.debug("Average grade for subject: {} is: {}", subjectId, result);
        return result;
    }

    /**
     * Get failing grades in a subject (below passing threshold)
     * @param teacherUserId the teacher's user ID
     * @param subjectId the subject ID
     * @param passingGrade the passing grade threshold (e.g., 50.0)
     * @return List of failing grades
     * @throws IllegalArgumentException if teacher doesn't teach this subject
     */
    public List<Grade> getFailingGradesInSubject(Long teacherUserId, Long subjectId, BigDecimal passingGrade) {
        logger.debug("Retrieving failing grades (below {}) for subject: {} by teacher userId: {}",
                passingGrade, subjectId, teacherUserId);

        validateTeacherTeachesSubject(teacherUserId, subjectId);
        List<Grade> grades = gradeRepository.findBySubject_SubjectId(subjectId);

        List<Grade> failingGrades = grades.stream()
                .filter(grade -> {
                    try {
                        grade.getStudent().getUser().getUserId();
                        return grade.getGradeValue().compareTo(passingGrade) < 0;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        logger.debug("Found {} failing grades in subject: {}", failingGrades.size(), subjectId);
        return failingGrades;
    }

    /**
     * Get top performing students in a subject
     * @param teacherUserId the teacher's user ID
     * @param subjectId the subject ID
     * @return List of grades sorted by grade value (descending)
     * @throws IllegalArgumentException if teacher doesn't teach this subject
     */
    public List<Grade> getTopGradesInSubject(Long teacherUserId, Long subjectId) {
        logger.debug("Retrieving top grades for subject: {} by teacher userId: {}",
                subjectId, teacherUserId);

        validateTeacherTeachesSubject(teacherUserId, subjectId);
        List<Grade> grades = gradeRepository.findTopGradesBySubjectId(subjectId);

        // Filter out grades with deleted students
        List<Grade> topGrades = grades.stream()
                .filter(grade -> {
                    try {
                        grade.getStudent().getUser().getUserId();
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        logger.debug("Found {} top grades in subject: {}", topGrades.size(), subjectId);
        return topGrades;
    }

    /**
     * Count total number of grades entered by teacher in a subject
     * @param teacherUserId the teacher's user ID
     * @param subjectId the subject ID
     * @return count of grades
     * @throws IllegalArgumentException if teacher doesn't teach this subject
     */
    public long countGradesInSubject(Long teacherUserId, Long subjectId) {
        logger.debug("Counting grades in subject: {} by teacher userId: {}", subjectId, teacherUserId);

        validateTeacherTeachesSubject(teacherUserId, subjectId);
        long count = gradeRepository.countBySubject_SubjectId(subjectId);

        logger.debug("Subject: {} has {} grades", subjectId, count);
        return count;
    }

    /**
     * Get grades with comments for a subject
     * @param teacherUserId the teacher's user ID
     * @param subjectId the subject ID
     * @return List of grades with comments
     * @throws IllegalArgumentException if teacher doesn't teach this subject
     */
    public List<Grade> getGradesWithCommentsInSubject(Long teacherUserId, Long subjectId) {
        logger.debug("Retrieving grades with comments for subject: {} by teacher userId: {}",
                subjectId, teacherUserId);

        validateTeacherTeachesSubject(teacherUserId, subjectId);
        List<Grade> grades = gradeRepository.findBySubject_SubjectId(subjectId);

        List<Grade> gradesWithComments = grades.stream()
                .filter(grade -> {
                    try {
                        grade.getStudent().getUser().getUserId();
                        return grade.getComment() != null && !grade.getComment().isEmpty();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        logger.debug("Found {} grades with comments in subject: {}", gradesWithComments.size(), subjectId);
        return gradesWithComments;
    }

    // ==================== Validation Methods ====================

    /**
     * Validate that a teacher teaches a specific subject
     * @param teacherUserId the teacher's user ID
     * @param subjectId the subject ID
     * @throws IllegalArgumentException if teacher doesn't teach this subject
     */
    private void validateTeacherTeachesSubject(Long teacherUserId, Long subjectId) {
        boolean teaches = teacherSubjectRepository.existsActiveAssignment(teacherUserId, subjectId);
        if (!teaches) {
            logger.error("Teacher userId: {} does not teach subject: {}", teacherUserId, subjectId);
            throw new IllegalArgumentException(
                    "Teacher with user ID " + teacherUserId + " does not teach subject with ID " + subjectId);
        }
    }

    /**
     * Check if teacher is assigned to teach a subject
     * @param teacherUserId the teacher's user ID
     * @param subjectId the subject ID
     * @return true if teacher teaches the subject, false otherwise
     */
    public boolean isTeachingSubject(Long teacherUserId, Long subjectId) {
        boolean teaches = teacherSubjectRepository.existsActiveAssignment(teacherUserId, subjectId);
        logger.debug("Teacher userId: {} {} subject: {}",
                teacherUserId, teaches ? "teaches" : "does not teach", subjectId);
        return teaches;
    }

    /**
     * Get teacher by user ID
     * @param userId the user ID
     * @return Teacher entity
     * @throws IllegalArgumentException if teacher not found
     */
    public Teacher getTeacherByUserId(Long userId) {
        logger.debug("Retrieving teacher with userId: {}", userId);
        return teacherRepository.findByUser_UserId(userId)
                .orElseThrow(() -> {
                    logger.error("Teacher not found with user ID: {}", userId);
                    return new IllegalArgumentException("Teacher not found with user ID: " + userId);
                });
    }

    /**
     * Check if user is a teacher
     * @param userId the user ID
     * @return true if user is a teacher, false otherwise
     */
    public boolean isTeacher(Long userId) {
        boolean isTeacher = teacherRepository.existsByUser_UserId(userId);
        logger.debug("User ID: {} is{} a teacher", userId, isTeacher ? "" : " not");
        return isTeacher;
    }
}