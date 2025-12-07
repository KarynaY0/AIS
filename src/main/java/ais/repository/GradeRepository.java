package ais.repository;

import ais.entity.Grade;
import ais.entity.Student;
import ais.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    /**
     * Find all grades for a specific student
     * @param student the student to search for
     * @return List of grades for the student
     */
    List<Grade> findByStudent(Student student);

    /**
     * Find all grades by student user ID
     * @param userId the student's user ID
     * @return List of grades for the student
     */
    List<Grade> findByStudent_User_UserId(Long userId);

    /**
     * Find all grades for a specific subject
     * @param subject the subject to search for
     * @return List of grades for the subject
     */
    List<Grade> findBySubject(Subject subject);

    /**
     * Find all grades by subject ID
     * @param subjectId the subject ID
     * @return List of grades for the subject
     */
    List<Grade> findBySubject_SubjectId(Long subjectId);

    /**
     * Find grade for a specific student and subject
     * @param student the student
     * @param subject the subject
     * @return Optional containing the grade if found
     */
    Optional<Grade> findByStudentAndSubject(Student student, Subject subject);

    /**
     * Find grade by student user ID and subject ID
     * @param userId the student's user ID
     * @param subjectId the subject ID
     * @return Optional containing the grade if found
     */
    @Query("SELECT g FROM Grade g WHERE g.student.user.userId = :userId AND g.subject.subjectId = :subjectId")
    Optional<Grade> findByStudentIdAndSubjectId(@Param("userId") Long userId, @Param("subjectId") Long subjectId);

    /**
     * Find all grades for students in a specific group
     * @param groupId the group ID
     * @return List of grades for students in the group
     */
    @Query("SELECT g FROM Grade g WHERE g.student.group.groupId = :groupId")
    List<Grade> findByGroupId(@Param("groupId") Long groupId);

    /**
     * Find all grades for a subject in a specific group
     * @param groupId the group ID
     * @param subjectId the subject ID
     * @return List of grades for the subject in the group
     */
    @Query("SELECT g FROM Grade g WHERE g.student.group.groupId = :groupId AND g.subject.subjectId = :subjectId")
    List<Grade> findByGroupIdAndSubjectId(@Param("groupId") Long groupId, @Param("subjectId") Long subjectId);

    /**
     * Calculate average grade for a student
     * @param userId the student's user ID
     * @return Average grade value
     */
    @Query("SELECT AVG(g.gradeValue) FROM Grade g WHERE g.student.user.userId = :userId")
    BigDecimal calculateAverageByStudentId(@Param("userId") Long userId);

    /**
     * Calculate average grade for a subject
     * @param subjectId the subject ID
     * @return Average grade value
     */
    @Query("SELECT AVG(g.gradeValue) FROM Grade g WHERE g.subject.subjectId = :subjectId")
    BigDecimal calculateAverageBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Calculate average grade for a group
     * @param groupId the group ID
     * @return Average grade value for all students in the group
     */
    @Query("SELECT AVG(g.gradeValue) FROM Grade g WHERE g.student.group.groupId = :groupId")
    BigDecimal calculateAverageByGroupId(@Param("groupId") Long groupId);

    /**
     * Find grades by grade value range
     * @param minGrade minimum grade value
     * @param maxGrade maximum grade value
     * @return List of grades within the range
     */
    @Query("SELECT g FROM Grade g WHERE g.gradeValue BETWEEN :minGrade AND :maxGrade")
    List<Grade> findByGradeValueRange(@Param("minGrade") BigDecimal minGrade, @Param("maxGrade") BigDecimal maxGrade);

    /**
     * Find grades updated after a specific date
     * @param dateTime the date and time to compare
     * @return List of grades updated after the specified date
     */
    List<Grade> findByUpdatedTimeAfter(LocalDateTime dateTime);

    /**
     * Find grades updated between dates
     * @param startDate start date
     * @param endDate end date
     * @return List of grades updated within the date range
     */
    List<Grade> findByUpdatedTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find failing grades (below passing threshold)
     * @param passingGrade the passing grade threshold
     * @return List of grades below the passing threshold
     */
    @Query("SELECT g FROM Grade g WHERE g.gradeValue < :passingGrade")
    List<Grade> findFailingGrades(@Param("passingGrade") BigDecimal passingGrade);

    /**
     * Find failing grades for a specific student
     * @param userId the student's user ID
     * @param passingGrade the passing grade threshold
     * @return List of failing grades for the student
     */
    @Query("SELECT g FROM Grade g WHERE g.student.user.userId = :userId AND g.gradeValue < :passingGrade")
    List<Grade> findFailingGradesByStudentId(@Param("userId") Long userId, @Param("passingGrade") BigDecimal passingGrade);

    /**
     * Find top grades for a subject
     * @param subjectId the subject ID
     * @return List of top grades for the subject
     */
    @Query("SELECT g FROM Grade g WHERE g.subject.subjectId = :subjectId ORDER BY g.gradeValue DESC")
    List<Grade> findTopGradesBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Count grades for a student
     * @param userId the student's user ID
     * @return count of grades for the student
     */
    long countByStudent_User_UserId(Long userId);

    /**
     * Count grades for a subject
     * @param subjectId the subject ID
     * @return count of grades for the subject
     */
    long countBySubject_SubjectId(Long subjectId);

    /**
     * Find grades with comments
     * @return List of grades that have comments
     */
    @Query("SELECT g FROM Grade g WHERE g.comment IS NOT NULL AND g.comment <> ''")
    List<Grade> findGradesWithComments();

    /**
     * Find recent grades for a student
     * @param userId the student's user ID
     * @return List of most recent grades for the student
     */
    @Query("SELECT g FROM Grade g WHERE g.student.user.userId = :userId ORDER BY g.updatedTime DESC")
    List<Grade> findRecentGradesByStudentId(@Param("userId") Long userId);

    /**
     * Find students with grades in a subject taught by a teacher
     * @param teacherId the teacher's user ID
     * @param subjectId the subject ID
     * @return List of grades for the teacher's subject
     */
    @Query("SELECT g FROM Grade g " +
            "JOIN g.subject.teacherSubjects ts " +
            "WHERE ts.teacher.user.userId = :teacherId " +
            "AND g.subject.subjectId = :subjectId " +
            "AND ts.isActive = true")
    List<Grade> findByTeacherIdAndSubjectId(@Param("teacherId") Long teacherId, @Param("subjectId") Long subjectId);

    /**
     * Calculate minimum grade for a subject
     * @param subjectId the subject ID
     * @return minimum grade value
     */
    @Query("SELECT MIN(g.gradeValue) FROM Grade g WHERE g.subject.subjectId = :subjectId")
    BigDecimal findMinGradeBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Calculate maximum grade for a subject
     * @param subjectId the subject ID
     * @return maximum grade value
     */
    @Query("SELECT MAX(g.gradeValue) FROM Grade g WHERE g.subject.subjectId = :subjectId")
    BigDecimal findMaxGradeBySubjectId(@Param("subjectId") Long subjectId);
}