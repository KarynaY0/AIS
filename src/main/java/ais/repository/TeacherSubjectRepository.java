package ais.repository;

import ais.entity.Subject;
import ais.entity.Teacher;
import ais.entity.TeacherSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, Long> {

    List<TeacherSubject> findByTeacher(Teacher teacher);

    List<TeacherSubject> findByTeacher_User_UserId(Long userId);

    List<TeacherSubject> findByTeacherAndIsActive(Teacher teacher, Boolean isActive);

    @Query("SELECT ts FROM TeacherSubject ts WHERE ts.teacher.user.userId = :userId AND ts.isActive = :isActive")
    List<TeacherSubject> findByTeacherIdAndIsActive(@Param("userId") Long userId, @Param("isActive") Boolean isActive);

    List<TeacherSubject> findBySubject(Subject subject);

    List<TeacherSubject> findBySubject_SubjectId(Long subjectId);

    List<TeacherSubject> findBySubjectAndIsActive(Subject subject, Boolean isActive);

    @Query("SELECT ts FROM TeacherSubject ts WHERE ts.subject.subjectId = :subjectId AND ts.isActive = :isActive")
    List<TeacherSubject> findBySubjectIdAndIsActive(@Param("subjectId") Long subjectId, @Param("isActive") Boolean isActive);

    Optional<TeacherSubject> findByTeacherAndSubject(Teacher teacher, Subject subject);

    @Query("SELECT ts FROM TeacherSubject ts WHERE ts.teacher.user.userId = :userId AND ts.subject.subjectId = :subjectId")
    Optional<TeacherSubject> findByTeacherIdAndSubjectId(@Param("userId") Long userId, @Param("subjectId") Long subjectId);

    boolean existsByTeacherAndSubject(Teacher teacher, Subject subject);

    @Query("SELECT CASE WHEN COUNT(ts) > 0 THEN true ELSE false END FROM TeacherSubject ts " +
            "WHERE ts.teacher.user.userId = :userId AND ts.subject.subjectId = :subjectId AND ts.isActive = true")
    boolean existsActiveAssignment(@Param("userId") Long userId, @Param("subjectId") Long subjectId);

    List<TeacherSubject> findByIsActive(Boolean isActive);

    @Query("SELECT ts FROM TeacherSubject ts WHERE ts.isActive = false")
    List<TeacherSubject> findInactiveAssignments();

    @Query("SELECT COUNT(ts) FROM TeacherSubject ts WHERE ts.teacher.user.userId = :userId AND ts.isActive = true")
    long countActiveAssignmentsByTeacherId(@Param("userId") Long userId);

    long countByTeacher_User_UserId(Long userId);

    @Query("SELECT COUNT(ts) FROM TeacherSubject ts WHERE ts.subject.subjectId = :subjectId AND ts.isActive = true")
    long countActiveTeachersBySubjectId(@Param("subjectId") Long subjectId);

    long countBySubject_SubjectId(Long subjectId);

    @Query("SELECT ts FROM TeacherSubject ts WHERE ts.teacher.department = :department")
    List<TeacherSubject> findByDepartment(@Param("department") String department);

    @Query("SELECT ts FROM TeacherSubject ts WHERE ts.teacher.department = :department AND ts.isActive = true")
    List<TeacherSubject> findActiveAssignmentsByDepartment(@Param("department") String department);

    @Query("SELECT DISTINCT ts FROM TeacherSubject ts " +
            "JOIN ts.subject.groupSubjects gs " +
            "WHERE gs.group.groupId = :groupId AND ts.isActive = true")
    List<TeacherSubject> findByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT DISTINCT ts FROM TeacherSubject ts " +
            "JOIN ts.subject.groupSubjects gs " +
            "WHERE gs.academicSemester = :semester AND ts.isActive = true")
    List<TeacherSubject> findByAcademicSemester(@Param("semester") String semester);

    @Query("SELECT s FROM Subject s WHERE s.subjectId NOT IN " +
            "(SELECT ts.subject.subjectId FROM TeacherSubject ts WHERE ts.isActive = true)")
    List<Subject> findSubjectsWithoutActiveTeachers();

    @Modifying
    @Query("UPDATE TeacherSubject ts SET ts.isActive = false WHERE ts.teacher.user.userId = :userId")
    void deactivateAllByTeacherId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE TeacherSubject ts SET ts.isActive = true " +
            "WHERE ts.teacher.user.userId = :userId AND ts.subject.subjectId = :subjectId")
    void activateAssignment(@Param("userId") Long userId, @Param("subjectId") Long subjectId);

    @Modifying
    @Query("UPDATE TeacherSubject ts SET ts.isActive = false " +
            "WHERE ts.teacher.user.userId = :userId AND ts.subject.subjectId = :subjectId")
    void deactivateAssignment(@Param("userId") Long userId, @Param("subjectId") Long subjectId);
}