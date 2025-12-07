package ais.service;

import ais.entity.*;
import ais.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Teacher operations
 * Handles business logic for managing and viewing grades for subjects taught by teachers
 */
@Service
@Transactional
public class TeacherService {

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
        // Validate teacher teaches this subject
        validateTeacherTeachesSubject(teacherUserId, subjectId);

        // Validate grade value
        if (gradeValue == null || gradeValue.compareTo(BigDecimal.ZERO) < 0 || gradeValue.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Grade value must be between 0 and 100");
        }

        // Find student and subject
        Student student = studentRepository.findByUser_UserId(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with user ID: " + studentUserId));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));

        // Create and save grade (no duplicate check - allows multiple grades)
        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setSubject(subject);
        grade.setGradeValue(gradeValue);
        grade.setComment(comment);
        grade.setUpdatedTime(LocalDateTime.now());

        return gradeRepository.save(grade);
    }

    /**
     * Save a grade directly (helper method for controller)
     */
    public Grade saveGrade(Grade grade) {
        return gradeRepository.save(grade);
    }

    /**
     * Get student by user ID (helper method)
     */
    public Student getStudentByUserId(Long userId) {
        return studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with user ID: " + userId));
    }

    /**
     * Get subject by ID (helper method)
     */
    public Subject getSubjectById(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));
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
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with ID: " + gradeId));

        // Validate teacher teaches this subject
        validateTeacherTeachesSubject(teacherUserId, grade.getSubject().getSubjectId());

        // Validate grade value
        if (newGradeValue.compareTo(BigDecimal.ZERO) < 0 || newGradeValue.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Grade value must be between 0 and 100");
        }

        // Update grade
        grade.setGradeValue(newGradeValue);
        grade.setComment(newComment);
        grade.setUpdatedTime(LocalDateTime.now());

        return gradeRepository.save(grade);
    }

    /**
     * Delete a grade
     * Teacher can only delete grades for subjects they teach
     * @param teacherUserId the teacher's user ID
     * @param gradeId the grade ID to delete
     * @throws IllegalArgumentException if validation fails
     */
    public void deleteGrade(Long teacherUserId, Long gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with ID: " + gradeId));

        // Validate teacher teaches this subject
        validateTeacherTeachesSubject(teacherUserId, grade.getSubject().getSubjectId());

        gradeRepository.delete(grade);
    }

    // ==================== View Grade Information ====================

    /**
     * Get all grades for a subject taught by the teacher
     * @param teacherUserId the teacher's user ID
     * @param subjectId the subject ID
     * @return List of grades for the subject
     * @throws IllegalArgumentException if teacher doesn't teach this subject
     */
    public List<Grade> getGradesBySubject(Long teacherUserId, Long subjectId) {
        validateTeacherTeachesSubject(teacherUserId, subjectId);
        return gradeRepository.findBySubject_SubjectId(subjectId);
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
        validateTeacherTeachesSubject(teacherUserId, subjectId);
        return gradeRepository.findByStudent_User_UserId(studentUserId).stream()
                .filter(grade -> grade.getSubject().getSubjectId().equals(subjectId))
                .toList();
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
        validateTeacherTeachesSubject(teacherUserId, subjectId);
        return gradeRepository.findByGroupIdAndSubjectId(groupId, subjectId);
    }

    /**
     * Get all subjects taught by a teacher
     * @param teacherUserId the teacher's user ID
     * @return List of subjects taught by the teacher
     */
    public List<Subject> getSubjectsTaughtByTeacher(Long teacherUserId) {
        return subjectRepository.findByTeacherId(teacherUserId);
    }

    /**
     * Get all students taught by a teacher (across all subjects)
     * @param teacherUserId the teacher's user ID
     * @return List of students taught by the teacher
     */
    public List<Student> getStudentsTaughtByTeacher(Long teacherUserId) {
        return studentRepository.findByTeacherId(teacherUserId);
    }

    /**
     * Get all groups taught by a teacher
     * @param teacherUserId the teacher's user ID
     * @return List of groups taught by the teacher
     */
    public List<Group> getGroupsTaughtByTeacher(Long teacherUserId) {
        Teacher teacher = teacherRepository.findByUser_UserId(teacherUserId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with user ID: " + teacherUserId));
        // Using GroupRepository method that was defined in the schema
        return teacher.getTeacherSubjects().stream()
                .flatMap(ts -> ts.getSubject().getGroupSubjects().stream())
                .map(GroupSubject::getGroup)
                .distinct()
                .toList();
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
        validateTeacherTeachesSubject(teacherUserId, subjectId);
        BigDecimal average = gradeRepository.calculateAverageBySubjectId(subjectId);
        return average != null ? average : BigDecimal.ZERO;
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
        validateTeacherTeachesSubject(teacherUserId, subjectId);
        return gradeRepository.findBySubject_SubjectId(subjectId).stream()
                .filter(grade -> grade.getGradeValue().compareTo(passingGrade) < 0)
                .toList();
    }

    /**
     * Get top performing students in a subject
     * @param teacherUserId the teacher's user ID
     * @param subjectId the subject ID
     * @return List of grades sorted by grade value (descending)
     * @throws IllegalArgumentException if teacher doesn't teach this subject
     */
    public List<Grade> getTopGradesInSubject(Long teacherUserId, Long subjectId) {
        validateTeacherTeachesSubject(teacherUserId, subjectId);
        return gradeRepository.findTopGradesBySubjectId(subjectId);
    }

    /**
     * Count total number of grades entered by teacher in a subject
     * @param teacherUserId the teacher's user ID
     * @param subjectId the subject ID
     * @return count of grades
     * @throws IllegalArgumentException if teacher doesn't teach this subject
     */
    public long countGradesInSubject(Long teacherUserId, Long subjectId) {
        validateTeacherTeachesSubject(teacherUserId, subjectId);
        return gradeRepository.countBySubject_SubjectId(subjectId);
    }

    /**
     * Get grades with comments for a subject
     * @param teacherUserId the teacher's user ID
     * @param subjectId the subject ID
     * @return List of grades with comments
     * @throws IllegalArgumentException if teacher doesn't teach this subject
     */
    public List<Grade> getGradesWithCommentsInSubject(Long teacherUserId, Long subjectId) {
        validateTeacherTeachesSubject(teacherUserId, subjectId);
        return gradeRepository.findBySubject_SubjectId(subjectId).stream()
                .filter(grade -> grade.getComment() != null && !grade.getComment().isEmpty())
                .toList();
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
        return teacherSubjectRepository.existsActiveAssignment(teacherUserId, subjectId);
    }

    /**
     * Get teacher by user ID
     * @param userId the user ID
     * @return Teacher entity
     * @throws IllegalArgumentException if teacher not found
     */
    public Teacher getTeacherByUserId(Long userId) {
        return teacherRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with user ID: " + userId));
    }

    /**
     * Check if user is a teacher
     * @param userId the user ID
     * @return true if user is a teacher, false otherwise
     */
    public boolean isTeacher(Long userId) {
        return teacherRepository.existsByUser_UserId(userId);
    }
}