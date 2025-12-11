package ais.repository;

import ais.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    /**
     * Find group by complete group code
     * @param groupCode the group code (e.g., "PI24E", "CS23")
     * @return Optional containing the group if found
     */
    Optional<Group> findByGroupCode(String groupCode);

    /**
     * Check if group exists with the given code
     * @param groupCode the group code to check
     * @return true if group exists, false otherwise
     */
    boolean existsByGroupCode(String groupCode);

    /**
     * Find groups by program initials
     * @param programInitials the program initials (e.g., "PI", "CS")
     * @return List of groups with matching program initials
     */
    List<Group> findByProgramInitials(String programInitials);

    /**
     * Find groups by start year
     * @param startYear the start year (2-digit: 23, 24, etc.)
     * @return List of groups starting in that year
     */
    List<Group> findByStartYear(Integer startYear);

    /**
     * Find groups by language code
     * @param languageCode the language code (e.g., "E", "L")
     * @return List of groups with matching language code
     */
    List<Group> findByLanguageCode(String languageCode);

    /**
     * Find groups with null language code
     * @return List of groups without language specification
     */
    List<Group> findByLanguageCodeIsNull();

    /**
     * Find groups enrolled in a specific subject
     * @param subjectId the subject ID
     * @return List of groups enrolled in the subject
     */
    @Query("SELECT DISTINCT g FROM Group g JOIN g.groupSubjects gs WHERE gs.subject.subjectId = :subjectId")
    List<Group> findBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Find all groups with students
     * @return List of groups that have at least one student
     */
    @Query("SELECT DISTINCT g FROM Group g WHERE SIZE(g.students) > 0")
    List<Group> findAllGroupsWithStudents();

    /**
     * Find all empty groups (without students)
     * @return List of groups without students
     */
    @Query("SELECT g FROM Group g WHERE SIZE(g.students) = 0")
    List<Group> findAllEmptyGroups();

    /**
     * Find groups taught by a specific teacher
     * @param teacherId the teacher's user ID
     * @return List of groups taught by the teacher
     */
    @Query("SELECT DISTINCT g FROM Group g " +
            "JOIN g.groupSubjects gs " +
            "JOIN gs.subject.teacherSubjects ts " +
            "WHERE ts.teacher.user.userId = :teacherId AND ts.isActive = true")
    List<Group> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Count students in a group
     * @param groupId the group ID
     * @return count of students in the group
     */
    @Query("SELECT COUNT(s) FROM Student s WHERE s.group.groupId = :groupId")
    long countStudentsInGroup(@Param("groupId") Long groupId);

    /**
     * Count subjects assigned to a group
     * @param groupId the group ID
     * @return count of subjects assigned to the group
     */
    @Query("SELECT COUNT(gs) FROM GroupSubject gs WHERE gs.group.groupId = :groupId")
    long countSubjectsInGroup(@Param("groupId") Long groupId);

    /**
     * Find groups by group code pattern (e.g., "PI" to find all PI groups)
     * @param codePattern the code pattern to search for
     * @return List of groups matching the pattern
     */
    @Query("SELECT g FROM Group g WHERE g.groupCode LIKE CONCAT(:codePattern, '%')")
    List<Group> findByGroupCodeStartingWith(@Param("codePattern") String codePattern);

    /**
     * Find all distinct program initials
     * @return List of all unique program initials, sorted
     */
    @Query("SELECT DISTINCT g.programInitials FROM Group g ORDER BY g.programInitials")
    List<String> findAllProgramInitials();

    /**
     * Find all distinct start years
     * @return List of all unique start years, sorted descending
     */
    @Query("SELECT DISTINCT g.startYear FROM Group g ORDER BY g.startYear DESC")
    List<Integer> findAllStartYears();

    /**
     * Find all distinct language codes (excluding null)
     * @return List of all unique language codes, sorted
     */
    @Query("SELECT DISTINCT g.languageCode FROM Group g WHERE g.languageCode IS NOT NULL ORDER BY g.languageCode")
    List<String> findAllLanguageCodes();

    /**
     * Find groups by program and year
     * @param programInitials the program initials
     * @param startYear the start year
     * @return List of groups matching program and year
     */
    List<Group> findByProgramInitialsAndStartYear(String programInitials, Integer startYear);

    /**
     * Find groups by program, year, and language
     * @param programInitials the program initials
     * @param startYear the start year
     * @param languageCode the language code
     * @return List of groups matching all criteria
     */
    List<Group> findByProgramInitialsAndStartYearAndLanguageCode(
            String programInitials, Integer startYear, String languageCode);
}