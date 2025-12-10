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
     * @param code the subject code
     * @return Optional containing the subject if found
     */
    Optional<Subject> findByCode(String code);

    /**
     * Check if subject exists by code
     * @param code the subject code
     * @return true if subject exists, false otherwise
     */
    boolean existsByCode(String code);

    /**
     * Find subjects by credits
     * @param credits the number of credits
     * @return List of subjects with matching credits
     */
    List<Subject> findByCredits(Integer credits);

    /**
     * Find subjects by credits range
     * @param minCredits minimum credits
     * @param maxCredits maximum credits
     * @return List of subjects within the credit range
     */
    @Query("SELECT s FROM Subject s WHERE s.credits BETWEEN :minCredits AND :maxCredits")
    List<Subject> findByCreditsRange(@Param("minCredits") Integer minCredits, @Param("maxCredits") Integer maxCredits);

    /**
     * Find subjects by code pattern
     * @param codePattern the code pattern to search for
     * @return List of subjects matching the pattern
     */
    List<Subject> findByCodeContaining(String codePattern);

    /**
     * Find subjects taught by a specific teacher (active assignments only)
     * @param teacherId the teacher's user ID
     * @return List of subjects taught by the teacher
     */
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.teacherSubjects ts WHERE ts.teacher.user.userId = :teacherId AND ts.isActive = true")
    List<Subject> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find all subjects taught by a teacher (including inactive)
     * @param teacherId the teacher's user ID
     * @return List of all subjects assigned to the teacher
     */
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.teacherSubjects ts WHERE ts.teacher.user.userId = :teacherId")
    List<Subject> findAllByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find subjects assigned to a specific group
     * @param groupId the group ID
     * @return List of subjects assigned to the group
     */
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.groupSubjects gs WHERE gs.group.groupId = :groupId")
    List<Subject> findByGroupId(@Param("groupId") Long groupId);

    /**
     * Find subjects by group and semester
     * @param groupId the group ID
     * @param semester the academic semester
     * @return List of subjects for the group in the semester
     */
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.groupSubjects gs WHERE gs.group.groupId = :groupId AND gs.academicSemester = :semester")
    List<Subject> findByGroupIdAndSemester(@Param("groupId") Long groupId, @Param("semester") String semester);

    /**
     * Find subjects by academic semester
     * @param semester the academic semester
     * @return List of subjects taught in the semester
     */
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.groupSubjects gs WHERE gs.academicSemester = :semester")
    List<Subject> findByAcademicSemester(@Param("semester") String semester);

    /**
     * Find subjects for a specific student
     * @param studentId the student's user ID
     * @return List of subjects the student is enrolled in
     */
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.groupSubjects gs JOIN gs.group.students st WHERE st.user.userId = :studentId")
    List<Subject> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find subjects without any active teachers
     * @return List of subjects without active teacher assignments
     */
    @Query("SELECT s FROM Subject s WHERE s.subjectId NOT IN (SELECT DISTINCT ts.subject.subjectId FROM TeacherSubject ts WHERE ts.isActive = true)")
    List<Subject> findSubjectsWithoutTeachers();

    /**
     * Find subjects without any group assignments
     * @return List of subjects not assigned to any group
     */
    @Query("SELECT s FROM Subject s WHERE s.subjectId NOT IN (SELECT DISTINCT gs.subject.subjectId FROM GroupSubject gs)")
    List<Subject> findSubjectsWithoutGroups();

    /**
     * Find subjects with active teachers
     * @return List of subjects with at least one active teacher
     */
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.teacherSubjects ts WHERE ts.isActive = true")
    List<Subject> findSubjectsWithActiveTeachers();

    /**
     * Count active teachers for a subject
     * @param subjectId the subject ID
     * @return count of active teachers
     */
    @Query("SELECT COUNT(DISTINCT ts.teacher.pk) FROM TeacherSubject ts WHERE ts.subject.subjectId = :subjectId AND ts.isActive = true")
    long countActiveTeachersBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Count groups assigned to a subject
     * @param subjectId the subject ID
     * @return count of groups
     */
    @Query("SELECT COUNT(DISTINCT gs.group.groupId) FROM GroupSubject gs WHERE gs.subject.subjectId = :subjectId")
    long countGroupsBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Find all distinct credit values
     * @return List of unique credit values
     */
    @Query("SELECT DISTINCT s.credits FROM Subject s WHERE s.credits IS NOT NULL ORDER BY s.credits")
    List<Integer> findAllDistinctCredits();

    /**
     * Find subjects by program initials (through groups)
     * @param programInitials the program initials
     * @return List of subjects taught to groups with matching program
     */
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.groupSubjects gs WHERE gs.group.programInitials = :programInitials")
    List<Subject> findByProgramInitials(@Param("programInitials") String programInitials);

    /**
     * Find subjects by start year (through groups)
     * @param startYear the start year
     * @return List of subjects taught to groups starting in that year
     */
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.groupSubjects gs WHERE gs.group.startYear = :startYear")
    List<Subject> findByStartYear(@Param("startYear") Integer startYear);

    /**
     * Find subjects by group code
     * @param groupCode the group code
     * @return List of subjects taught to that specific group
     */
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.groupSubjects gs WHERE gs.group.groupCode = :groupCode")
    List<Subject> findByGroupCode(@Param("groupCode") String groupCode);
}