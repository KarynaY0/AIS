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
     * Find group by course year
     * @param courseYear the course year to search for (format: YYYY/YYYY)
     * @return Optional containing the group if found
     */
    Optional<Group> findByCourseYear(String courseYear);

    /**
     * Find all groups by course year
     * @param courseYear the course year to search for
     * @return List of groups with the specified course year
     */
    List<Group> findAllByCourseYear(String courseYear);

    /**
     * Check if group exists with the given course year
     * @param courseYear the course year to check
     * @return true if group exists, false otherwise
     */
    boolean existsByCourseYear(String courseYear);

    /**
     * Find groups enrolled in a specific subject
     * @param subjectId the subject ID
     * @return List of groups enrolled in the subject
     */
    @Query("SELECT DISTINCT g FROM Group g JOIN g.groupSubjects gs WHERE gs.subject.subjectId = :subjectId")
    List<Group> findBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Find groups by academic semester
     * @param academicSemester the academic semester
     * @return List of groups in the specified semester
     */
    @Query("SELECT DISTINCT g FROM Group g JOIN g.groupSubjects gs WHERE gs.academicSemester = :semester")
    List<Group> findByAcademicSemester(@Param("semester") String academicSemester);

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
     * Find groups by course year pattern (e.g., "2023" to find "2023/2024")
     * @param yearPattern the year pattern to search for
     * @return List of groups matching the pattern
     */
    @Query("SELECT g FROM Group g WHERE g.courseYear LIKE CONCAT('%', :yearPattern, '%')")
    List<Group> findByCourseYearContaining(@Param("yearPattern") String yearPattern);

    /**
     * Find all distinct course years
     * @return List of all unique course years, sorted
     */
    @Query("SELECT DISTINCT g.courseYear FROM Group g WHERE g.courseYear IS NOT NULL ORDER BY g.courseYear DESC")
    List<String> findAllCourseYears();

    /**
     * Find groups with subjects in a specific semester and academic year
     * @param semester the semester
     * @param courseYear the course year
     * @return List of groups matching the criteria
     */
    @Query("SELECT DISTINCT g FROM Group g " +
            "JOIN g.groupSubjects gs " +
            "WHERE gs.academicSemester = :semester AND g.courseYear = :courseYear")
    List<Group> findBySemesterAndCourseYear(@Param("semester") String semester,
                                            @Param("courseYear") String courseYear);
}