package ais.repository;

import ais.entity.Group;
import ais.entity.GroupSubject;
import ais.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupSubjectRepository extends JpaRepository<GroupSubject, Long> {

    /**
     * Find all group-subject assignments for a specific group
     * @param group the group to search for
     * @return List of group-subject assignments
     */
    List<GroupSubject> findByGroup(Group group);

    /**
     * Find all group-subject assignments by group ID
     * @param groupId the group ID
     * @return List of group-subject assignments
     */
    List<GroupSubject> findByGroup_GroupId(Long groupId);

    /**
     * Find all group-subject assignments for a specific subject
     * @param subject the subject to search for
     * @return List of group-subject assignments
     */
    List<GroupSubject> findBySubject(Subject subject);

    /**
     * Find all group-subject assignments by subject ID
     * @param subjectId the subject ID
     * @return List of group-subject assignments
     */
    List<GroupSubject> findBySubject_SubjectId(Long subjectId);

    /**
     * Find specific group-subject assignment
     * @param group the group
     * @param subject the subject
     * @return Optional containing the assignment if found
     */
    Optional<GroupSubject> findByGroupAndSubject(Group group, Subject subject);

    /**
     * Find assignment by group ID and subject ID
     * @param groupId the group ID
     * @param subjectId the subject ID
     * @return Optional containing the assignment if found
     */
    @Query("SELECT gs FROM GroupSubject gs WHERE gs.group.groupId = :groupId AND gs.subject.subjectId = :subjectId")
    Optional<GroupSubject> findByGroupIdAndSubjectId(@Param("groupId") Long groupId, @Param("subjectId") Long subjectId);

    /**
     * Find all assignments by academic semester
     * @param academicSemester the academic semester
     * @return List of group-subject assignments in the semester
     */
    List<GroupSubject> findByAcademicSemester(String academicSemester);

    /**
     * Find assignments for a group in a specific semester
     * @param group the group
     * @param academicSemester the academic semester
     * @return List of group-subject assignments
     */
    List<GroupSubject> findByGroupAndAcademicSemester(Group group, String academicSemester);

    /**
     * Find assignments by group ID and semester
     * @param groupId the group ID
     * @param academicSemester the academic semester
     * @return List of group-subject assignments
     */
    @Query("SELECT gs FROM GroupSubject gs WHERE gs.group.groupId = :groupId AND gs.academicSemester = :semester")
    List<GroupSubject> findByGroupIdAndAcademicSemester(@Param("groupId") Long groupId, @Param("semester") String academicSemester);

    /**
     * Check if group is assigned to subject
     * @param group the group
     * @param subject the subject
     * @return true if assignment exists, false otherwise
     */
    boolean existsByGroupAndSubject(Group group, Subject subject);
}