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

@Service
@Transactional
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public GradeService(GradeRepository gradeRepository,
                        StudentRepository studentRepository,
                        SubjectRepository subjectRepository,
                        GroupRepository groupRepository) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Grade> findById(Long gradeId) {
        return gradeRepository.findById(gradeId);
    }

    @Transactional(readOnly = true)
    public List<Grade> getAllGrades() {
        return gradeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesByStudent(Long studentUserId) {
        return gradeRepository.findByStudent_User_UserId(studentUserId);
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesBySubject(Long subjectId) {
        return gradeRepository.findBySubject_SubjectId(subjectId);
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesByGroup(Long groupId) {
        return gradeRepository.findByGroupId(groupId);
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesByGroupAndSubject(Long groupId, Long subjectId) {
        return gradeRepository.findByGroupIdAndSubjectId(groupId, subjectId);
    }

    @Transactional(readOnly = true)
    public Optional<Grade> getGradeByStudentAndSubject(Long studentUserId, Long subjectId) {
        return gradeRepository.findByStudentIdAndSubjectId(studentUserId, subjectId);
    }

    public Grade createGrade(Long studentUserId, Long subjectId, BigDecimal gradeValue, String comment) {
        // CRITICAL: Validate grade value is not null (prevents SQL error)
        if (gradeValue == null) {
            throw new IllegalArgumentException("Grade value is required");
        }

        if (gradeValue.compareTo(BigDecimal.ZERO) < 0 || gradeValue.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Grade value must be between 0 and 100");
        }

        Student student = studentRepository.findByUser_UserId(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with user ID: " + studentUserId));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));

        Optional<Grade> existingGrade = gradeRepository.findByStudentAndSubject(student, subject);
        if (existingGrade.isPresent()) {
            throw new IllegalArgumentException("Grade already exists for this student and subject");
        }

        Grade grade = new Grade(student, subject, gradeValue, comment);
        return gradeRepository.save(grade);
    }

    public Grade updateGrade(Long gradeId, BigDecimal newGradeValue, String newComment) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with ID: " + gradeId));

        if (newGradeValue == null) {
            throw new IllegalArgumentException("Grade value is required");
        }

        if (newGradeValue.compareTo(BigDecimal.ZERO) < 0 || newGradeValue.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Grade value must be between 0 and 100");
        }

        grade.setGradeValue(newGradeValue);
        grade.setComment(newComment);
        grade.setUpdatedTime(LocalDateTime.now());

        return gradeRepository.save(grade);
    }

    public void deleteGrade(Long gradeId) {
        if (!gradeRepository.existsById(gradeId)) {
            throw new IllegalArgumentException("Grade not found with ID: " + gradeId);
        }
        gradeRepository.deleteById(gradeId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateStudentAverage(Long studentUserId) {
        BigDecimal average = gradeRepository.calculateAverageByStudentId(studentUserId);
        return average != null ? average : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateSubjectAverage(Long subjectId) {
        BigDecimal average = gradeRepository.calculateAverageBySubjectId(subjectId);
        return average != null ? average : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateGroupAverage(Long groupId) {
        BigDecimal average = gradeRepository.calculateAverageByGroupId(groupId);
        return average != null ? average : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<Grade> getFailingGrades(BigDecimal passingGrade) {
        return gradeRepository.findFailingGrades(passingGrade);
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesByRange(BigDecimal minGrade, BigDecimal maxGrade) {
        return gradeRepository.findByGradeValueRange(minGrade, maxGrade);
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesUpdatedAfter(LocalDateTime dateTime) {
        return gradeRepository.findByUpdatedTimeAfter(dateTime);
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesUpdatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return gradeRepository.findByUpdatedTimeBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesWithComments() {
        return gradeRepository.findGradesWithComments();
    }

    @Transactional(readOnly = true)
    public List<Grade> getTopGradesBySubject(Long subjectId) {
        return gradeRepository.findTopGradesBySubjectId(subjectId);
    }

    @Transactional(readOnly = true)
    public long countGradesByStudent(Long studentUserId) {
        return gradeRepository.countByStudent_User_UserId(studentUserId);
    }

    @Transactional(readOnly = true)
    public long countGradesBySubject(Long subjectId) {
        return gradeRepository.countBySubject_SubjectId(subjectId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getMinGradeBySubject(Long subjectId) {
        BigDecimal min = gradeRepository.findMinGradeBySubjectId(subjectId);
        return min != null ? min : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getMaxGradeBySubject(Long subjectId) {
        BigDecimal max = gradeRepository.findMaxGradeBySubjectId(subjectId);
        return max != null ? max : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public String getSubjectStatistics(Long subjectId) {
        long count = countGradesBySubject(subjectId);

        if (count == 0) {
            return "No grades recorded for this subject.";
        }

        BigDecimal average = calculateSubjectAverage(subjectId);
        BigDecimal min = getMinGradeBySubject(subjectId);
        BigDecimal max = getMaxGradeBySubject(subjectId);

        return String.format(
                "Subject Statistics:\n" +
                        "Total grades: %d\n" +
                        "Average: %.2f\n" +
                        "Minimum: %.2f\n" +
                        "Maximum: %.2f",
                count, average, min, max
        );
    }

    @Transactional(readOnly = true)
    public String getGroupStatistics(Long groupId) {
        List<Grade> grades = getGradesByGroup(groupId);

        if (grades.isEmpty()) {
            return "No grades recorded for this group.";
        }

        BigDecimal average = calculateGroupAverage(groupId);
        long studentCount = studentRepository.countByGroup_GroupId(groupId);

        return String.format(
                "Group Statistics:\n" +
                        "Total students: %d\n" +
                        "Total grades: %d\n" +
                        "Average grade: %.2f",
                studentCount, grades.size(), average
        );
    }

    @Transactional(readOnly = true)
    public boolean hasGrades(Long studentUserId) {
        return countGradesByStudent(studentUserId) > 0;
    }

    @Transactional(readOnly = true)
    public long getTotalGradeCount() {
        return gradeRepository.count();
    }

    public void deleteAllGradesByStudent(Long studentUserId) {
        List<Grade> grades = gradeRepository.findByStudent_User_UserId(studentUserId);
        gradeRepository.deleteAll(grades);
    }

    public void deleteAllGradesBySubject(Long subjectId) {
        List<Grade> grades = gradeRepository.findBySubject_SubjectId(subjectId);
        gradeRepository.deleteAll(grades);
    }
}