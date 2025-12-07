package ais.repository;

import ais.entity.Teacher;
import ais.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    /**
     * Find teacher by user
     * @param user the user to search for
     * @return Optional containing the teacher if found
     */
    Optional<Teacher> findByUser(User user);

    /**
     * Find teacher by user ID
     * @param userId the user ID to search for
     * @return Optional containing the teacher if found
     */
    Optional<Teacher> findByUser_UserId(Long userId);

    /**
     * Find teacher by username
     * @param username the username to search for
     * @return Optional containing the teacher if found
     */
    @Query("SELECT t FROM Teacher t WHERE t.user.username = :username")
    Optional<Teacher> findByUsername(@Param("username") String username);

    /**
     * Find all teachers by department
     * @param department the department to search for
     * @return List of teachers in the department
     */
    List<Teacher> findByDepartment(String department);

    /**
     * Find all distinct departments
     * @return List of all unique department names
     */
    @Query("SELECT DISTINCT t.department FROM Teacher t WHERE t.department IS NOT NULL ORDER BY t.department")
    List<String> findAllDepartments();

    /**
     * Check if teacher exists for a user
     * @param user the user to check
     * @return true if teacher exists, false otherwise
     */
    boolean existsByUser(User user);

    /**
     * Check if teacher exists by user ID
     * @param userId the user ID to check
     * @return true if teacher exists, false otherwise
     */
    boolean existsByUser_UserId(Long userId);

    /**
     * Find teachers teaching a specific subject
     * @param subjectId the subject ID
     * @return List of teachers teaching the subject
     */
    @Query("SELECT t FROM Teacher t JOIN t.teacherSubjects ts WHERE ts.subject.subjectId = :subjectId AND ts.isActive = true")
    List<Teacher> findBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Find all active teachers (teachers with at least one active subject assignment)
     * @return List of active teachers
     */
    @Query("SELECT DISTINCT t FROM Teacher t JOIN t.teacherSubjects ts WHERE ts.isActive = true")
    List<Teacher> findAllActiveTeachers();

    /**
     * Count teachers by department
     * @param department the department name
     * @return count of teachers in the department
     */
    long countByDepartment(String department);

    /**
     * Find teachers teaching subjects to a specific group
     * @param groupId the group ID
     * @return List of teachers teaching the group
     */
    @Query("SELECT DISTINCT t FROM Teacher t JOIN t.teacherSubjects ts JOIN ts.subject.groupSubjects gs WHERE gs.group.groupId = :groupId AND ts.isActive = true")
    List<Teacher> findByGroupId(@Param("groupId") Long groupId);
}