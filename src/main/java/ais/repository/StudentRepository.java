package ais.repository;

import ais.entity.Group;
import ais.entity.Student;
import ais.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Find student by user
     * @param user the user to search for
     * @return Optional containing the student if found
     */
    Optional<Student> findByUser(User user);

    /**
     * Find student by user ID
     * @param userId the user ID to search for
     * @return Optional containing the student if found
     */
    Optional<Student> findByUser_UserId(Long userId);

    /**
     * Find student by username
     * @param username the username to search for
     * @return Optional containing the student if found
     */
    @Query("SELECT s FROM Student s WHERE s.user.username = :username")
    Optional<Student> findByUsername(@Param("username") String username);

    /**
     * Find all students in a group
     * @param group the group to search for
     * @return List of students in the group
     */
    List<Student> findByGroup(Group group);

    /**
     * Find all students by group ID
     * @param groupId the group ID to search for
     * @return List of students in the group
     */
    List<Student> findByGroup_GroupId(Long groupId);

    /**
     * Find all students without a group
     * @return List of students without a group
     */
    List<Student> findByGroupIsNull();

    /**
     * Check if student exists for a user
     * @param user the user to check
     * @return true if student exists, false otherwise
     */
    boolean existsByUser(User user);

    /**
     * Check if student exists by user ID
     * @param userId the user ID to check
     * @return true if student exists, false otherwise
     */
    boolean existsByUser_UserId(Long userId);

    /**
     * Find students enrolled in a specific subject
     * @param subjectId the subject ID
     * @return List of students enrolled in the subject
     */
    @Query("SELECT DISTINCT s FROM Student s JOIN s.group g JOIN g.groupSubjects gs WHERE gs.subject.subjectId = :subjectId")
    List<Student> findBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Find students by group code (NEW FORMAT)
     * @param groupCode the group code (e.g., "PI24E", "CS23")
     * @return List of students in the specified group
     */
    @Query("SELECT s FROM Student s WHERE s.group.groupCode = :groupCode")
    List<Student> findByGroupCode(@Param("groupCode") String groupCode);

    /**
     * Find students by program initials
     * @param programInitials the program initials (e.g., "PI", "CS")
     * @return List of students in groups with matching program
     */
    @Query("SELECT s FROM Student s WHERE s.group.programInitials = :programInitials")
    List<Student> findByProgramInitials(@Param("programInitials") String programInitials);

    /**
     * Find students by start year
     * @param startYear the start year (e.g., 24, 23)
     * @return List of students in groups starting in that year
     */
    @Query("SELECT s FROM Student s WHERE s.group.startYear = :startYear")
    List<Student> findByStartYear(@Param("startYear") Integer startYear);

    /**
     * Find students by language code
     * @param languageCode the language code (e.g., "E", "L")
     * @return List of students in groups with matching language
     */
    @Query("SELECT s FROM Student s WHERE s.group.languageCode = :languageCode")
    List<Student> findByLanguageCode(@Param("languageCode") String languageCode);

    /**
     * Find students by program and year
     * @param programInitials the program initials
     * @param startYear the start year
     * @return List of students matching both criteria
     */
    @Query("SELECT s FROM Student s WHERE s.group.programInitials = :programInitials AND s.group.startYear = :startYear")
    List<Student> findByProgramAndYear(@Param("programInitials") String programInitials, @Param("startYear") Integer startYear);

    /**
     * Count students in a specific group
     * @param groupId the group ID
     * @return count of students in the group
     */
    long countByGroup_GroupId(Long groupId);

    /**
     * Count students without a group
     * @return count of students without a group
     */
    long countByGroupIsNull();

    /**
     * Find students with grades in a specific subject
     * @param subjectId the subject ID
     * @return List of students who have grades in the subject
     */
    @Query("SELECT DISTINCT s FROM Student s JOIN s.grades g WHERE g.subject.subjectId = :subjectId")
    List<Student> findStudentsWithGradesInSubject(@Param("subjectId") Long subjectId);

    /**
     * Find students taught by a specific teacher
     * @param teacherId the teacher's user ID
     * @return List of students taught by the teacher
     */
    @Query("SELECT DISTINCT s FROM Student s " +
            "JOIN s.group g " +
            "JOIN g.groupSubjects gs " +
            "JOIN gs.subject.teacherSubjects ts " +
            "WHERE ts.teacher.user.userId = :teacherId AND ts.isActive = true")
    List<Student> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find students by academic semester
     * @param semester the academic semester
     * @return List of students enrolled in subjects during the semester
     */
    @Query("SELECT DISTINCT s FROM Student s " +
            "JOIN s.group g " +
            "JOIN g.groupSubjects gs " +
            "WHERE gs.academicSemester = :semester")
    List<Student> findByAcademicSemester(@Param("semester") String semester);
}