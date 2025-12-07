package ais.repository;

import ais.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /**
     * Find subject by code
     * @param code the subject code to search for
     * @return Optional containing the subject if found
     */
    Optional<Subject> findByCode(String code);

    /**
     * Check if subject exists with the given code
     * @param code the subject code to check
     * @return true if subject exists, false otherwise
     */
    boolean existsByCode(String code);

    /**
     * Find all subjects by credits
     * @param credits the number of credits
     * @return List of subjects with the specified credits
     */
    List<Subject> findByCredits(Integer credits);

    /**
     * Find subjects by code pattern (case-insensitive)
     * @param codePattern the code pattern to search for
     * @return List of subjects matching the pattern
     */
    @Query("SELECT s FROM Subject s WHERE LOWER(s.code) LIKE LOWER(CONCAT('%', :codePattern, '%'))")
    List<Subject> findByCodeContaining(@Param("codePattern") String codePattern);

    /**
     * Find subjects taught by a specific teacher
     * @param teacherId the teacher's user ID
     * @return List of subjects taught by the teacher
     */
    @Query("SELECT s FROM Subject s JOIN s.teacherSubjects ts WHERE ts.teacher.user.userId = :teacherId AND ts.isActive = true")
    List<Subject> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find all subjects taught by a teacher (including inactive)
     * @param teacherId the teacher's user ID
     * @return List of all subjects taught by the teacher
     */
    @Query("SELECT s FROM Subject s JOIN s.teacherSubjects ts WHERE ts.teacher.user.userId = :teacherId")
    List<Subject> findAllByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find subjects assigned to a specific group
     * @param groupId the group ID
     * @return List of subjects assigned to the group
     */
    @Query("SELECT s FROM Subject s JOIN s.groupSubjects gs WHERE gs.group.groupId = :groupId")
    List<Subject> findByGroupId(@Param("groupId") Long groupId);

    /**
     * Find subjects by academic semester for a specific group
     * @param groupId the group ID
     * @param semester the academic semester
     * @return List of subjects for the group in the semester
     */
    @Query("SELECT s FROM Subject s JOIN s.groupSubjects gs WHERE gs.group.groupId = :groupId AND gs.academicSemester = :semester")
    List<Subject> findByGroupIdAndSemester(@Param("groupId") Long groupId, @Param("semester") String semester);

    /**
     * Find subjects by academic semester
     * @param semester the academic semester
     * @return List of subjects in the semester
     */
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.groupSubjects gs WHERE gs.academicSemester = :semester")
    List<Subject> findByAcademicSemester(@Param("semester") String semester);

    /**
     * Find subjects for a specific student
     * @param studentId the student's user ID
     * @return List of subjects the student is enrolled in
     */
    @Query("SELECT DISTINCT s FROM Subject s " +
            "JOIN s.groupSubjects gs " +
            "JOIN gs.group.students st " +
            "WHERE st.user.userId = :studentId")
    List<Subject> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find subjects without any teacher assignments
     * @return List of subjects without teachers
     */
    @Query("SELECT s FROM Subject s WHERE SIZE(s.teacherSubjects) = 0")
    List<Subject> findSubjectsWithoutTeachers();

    /**
     * Find subjects without any group assignments
     * @return List of subjects without groups
     */
    @Query("SELECT s FROM Subject s WHERE SIZE(s.groupSubjects) = 0")
    List<Subject> findSubjectsWithoutGroups();

    /**
     * Find subjects with active teacher assignments
     * @return List of subjects with at least one active teacher
     */
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.teacherSubjects ts WHERE ts.isActive = true")
    List<Subject> findSubjectsWithActiveTeachers();

    /**
     * Count teachers assigned to a subject
     * @param subjectId the subject ID
     * @return count of teachers assigned to the subject
     */
    @Query("SELECT COUNT(ts) FROM TeacherSubject ts WHERE ts.subject.subjectId = :subjectId AND ts.isActive = true")
    long countActiveTeachersBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Count groups assigned to a subject
     * @param subjectId the subject ID
     * @return count of groups assigned to the subject
     */
    @Query("SELECT COUNT(gs) FROM GroupSubject gs WHERE gs.subject.subjectId = :subjectId")
    long countGroupsBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Find subjects by credits range
     * @param minCredits minimum credits
     * @param maxCredits maximum credits
     * @return List of subjects within the credits range
     */
    @Query("SELECT s FROM Subject s WHERE s.credits BETWEEN :minCredits AND :maxCredits")
    List<Subject> findByCreditsRange(@Param("minCredits") Integer minCredits, @Param("maxCredits") Integer maxCredits);

    /**
     * Find all distinct credit values
     * @return List of all unique credit values, sorted
     */
    @Query("SELECT DISTINCT s.credits FROM Subject s WHERE s.credits IS NOT NULL ORDER BY s.credits")
    List<Integer> findAllDistinctCredits();
}