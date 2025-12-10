package ais.service;

import ais.entity.*;
import ais.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(GradeService.class);

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
        logger.debug("Finding grade by ID: {}", gradeId);
        return gradeRepository.findById(gradeId);
    }

    @Transactional(readOnly = true)
    public List<Grade> getAllGrades() {
        logger.debug("Retrieving all grades");
        List<Grade> grades = gradeRepository.findAll();
        logger.debug("Found {} grades", grades.size());
        return grades;
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesByStudent(Long studentUserId) {
        logger.debug("Retrieving grades for student userId: {}", studentUserId);
        List<Grade> grades = gradeRepository.findByStudent_User_UserId(studentUserId);
        logger.debug("Found {} grades for student userId: {}", grades.size(), studentUserId);
        return grades;
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesBySubject(Long subjectId) {
        logger.debug("Retrieving grades for subject: {}", subjectId);
        List<Grade> grades = gradeRepository.findBySubject_SubjectId(subjectId);
        logger.debug("Found {} grades for subject: {}", grades.size(), subjectId);
        return grades;
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesByGroup(Long groupId) {
        logger.debug("Retrieving grades for group: {}", groupId);
        List<Grade> grades = gradeRepository.findByGroupId(groupId);
        logger.debug("Found {} grades for group: {}", grades.size(), groupId);
        return grades;
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesByGroupAndSubject(Long groupId, Long subjectId) {
        logger.debug("Retrieving grades for group: {} and subject: {}", groupId, subjectId);
        List<Grade> grades = gradeRepository.findByGroupIdAndSubjectId(groupId, subjectId);
        logger.debug("Found {} grades for group: {} and subject: {}", grades.size(), groupId, subjectId);
        return grades;
    }

    @Transactional(readOnly = true)
    public Optional<Grade> getGradeByStudentAndSubject(Long studentUserId, Long subjectId) {
        logger.debug("Retrieving grade for student userId: {} and subject: {}", studentUserId, subjectId);
        return gradeRepository.findByStudentIdAndSubjectId(studentUserId, subjectId);
    }

    public Grade createGrade(Long studentUserId, Long subjectId, BigDecimal gradeValue, String comment) {
        logger.info("Creating grade for student userId: {} in subject: {} with value: {}",
                studentUserId, subjectId, gradeValue);

        // Validate grade value
        if (gradeValue == null) {
            logger.error("Grade value is null");
            throw new IllegalArgumentException("Grade value is required");
        }

        if (gradeValue.compareTo(BigDecimal.ZERO) < 0 || gradeValue.compareTo(new BigDecimal("100")) > 0) {
            logger.error("Invalid grade value: {}", gradeValue);
            throw new IllegalArgumentException("Grade value must be between 0 and 100");
        }

        Student student = studentRepository.findByUser_UserId(studentUserId)
                .orElseThrow(() -> {
                    logger.error("Student not found with user ID: {}", studentUserId);
                    return new IllegalArgumentException("Student not found with user ID: " + studentUserId);
                });

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> {
                    logger.error("Subject not found with ID: {}", subjectId);
                    return new IllegalArgumentException("Subject not found with ID: " + subjectId);
                });

        Optional<Grade> existingGrade = gradeRepository.findByStudentAndSubject(student, subject);
        if (existingGrade.isPresent()) {
            logger.warn("Grade already exists for student userId: {} and subject: {}", studentUserId, subjectId);
            throw new IllegalArgumentException("Grade already exists for this student and subject");
        }

        Grade grade = new Grade(student, subject, gradeValue, comment);
        grade = gradeRepository.save(grade);

        logger.info("Successfully created grade ID: {} for student userId: {} in subject: {}",
                grade.getGradeId(), studentUserId, subjectId);
        return grade;
    }

    public Grade updateGrade(Long gradeId, BigDecimal newGradeValue, String newComment) {
        logger.info("Updating grade ID: {} with new value: {}", gradeId, newGradeValue);

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> {
                    logger.error("Grade not found with ID: {}", gradeId);
                    return new IllegalArgumentException("Grade not found with ID: " + gradeId);
                });

        if (newGradeValue == null) {
            logger.error("New grade value is null");
            throw new IllegalArgumentException("Grade value is required");
        }

        if (newGradeValue.compareTo(BigDecimal.ZERO) < 0 || newGradeValue.compareTo(new BigDecimal("100")) > 0) {
            logger.error("Invalid new grade value: {}", newGradeValue);
            throw new IllegalArgumentException("Grade value must be between 0 and 100");
        }

        grade.setGradeValue(newGradeValue);
        grade.setComment(newComment);
        grade.setUpdatedTime(LocalDateTime.now());

        grade = gradeRepository.save(grade);
        logger.info("Successfully updated grade ID: {} to value: {}", gradeId, newGradeValue);
        return grade;
    }

    public void deleteGrade(Long gradeId) {
        logger.info("Deleting grade with ID: {}", gradeId);

        if (!gradeRepository.existsById(gradeId)) {
            logger.error("Grade not found with ID: {}", gradeId);
            throw new IllegalArgumentException("Grade not found with ID: " + gradeId);
        }

        gradeRepository.deleteById(gradeId);
        logger.info("Successfully deleted grade ID: {}", gradeId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateStudentAverage(Long studentUserId) {
        logger.debug("Calculating average for student userId: {}", studentUserId);
        BigDecimal average = gradeRepository.calculateAverageByStudentId(studentUserId);
        BigDecimal result = average != null ? average : BigDecimal.ZERO;
        logger.debug("Average for student userId: {} is: {}", studentUserId, result);
        return result;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateSubjectAverage(Long subjectId) {
        logger.debug("Calculating average for subject: {}", subjectId);
        BigDecimal average = gradeRepository.calculateAverageBySubjectId(subjectId);
        BigDecimal result = average != null ? average : BigDecimal.ZERO;
        logger.debug("Average for subject: {} is: {}", subjectId, result);
        return result;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateGroupAverage(Long groupId) {
        logger.debug("Calculating average for group: {}", groupId);
        BigDecimal average = gradeRepository.calculateAverageByGroupId(groupId);
        BigDecimal result = average != null ? average : BigDecimal.ZERO;
        logger.debug("Average for group: {} is: {}", groupId, result);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Grade> getFailingGrades(BigDecimal passingGrade) {
        logger.debug("Retrieving failing grades below: {}", passingGrade);
        List<Grade> grades = gradeRepository.findFailingGrades(passingGrade);
        logger.debug("Found {} failing grades", grades.size());
        return grades;
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesByRange(BigDecimal minGrade, BigDecimal maxGrade) {
        logger.debug("Retrieving grades in range: {} to {}", minGrade, maxGrade);
        List<Grade> grades = gradeRepository.findByGradeValueRange(minGrade, maxGrade);
        logger.debug("Found {} grades in range", grades.size());
        return grades;
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesUpdatedAfter(LocalDateTime dateTime) {
        logger.debug("Retrieving grades updated after: {}", dateTime);
        List<Grade> grades = gradeRepository.findByUpdatedTimeAfter(dateTime);
        logger.debug("Found {} grades updated after: {}", grades.size(), dateTime);
        return grades;
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesUpdatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Retrieving grades updated between: {} and {}", startDate, endDate);
        List<Grade> grades = gradeRepository.findByUpdatedTimeBetween(startDate, endDate);
        logger.debug("Found {} grades in date range", grades.size());
        return grades;
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesWithComments() {
        logger.debug("Retrieving grades with comments");
        List<Grade> grades = gradeRepository.findGradesWithComments();
        logger.debug("Found {} grades with comments", grades.size());
        return grades;
    }

    @Transactional(readOnly = true)
    public List<Grade> getTopGradesBySubject(Long subjectId) {
        logger.debug("Retrieving top grades for subject: {}", subjectId);
        List<Grade> grades = gradeRepository.findTopGradesBySubjectId(subjectId);
        logger.debug("Found {} top grades for subject: {}", grades.size(), subjectId);
        return grades;
    }

    @Transactional(readOnly = true)
    public long countGradesByStudent(Long studentUserId) {
        logger.debug("Counting grades for student userId: {}", studentUserId);
        long count = gradeRepository.countByStudent_User_UserId(studentUserId);
        logger.debug("Student userId: {} has {} grades", studentUserId, count);
        return count;
    }

    @Transactional(readOnly = true)
    public long countGradesBySubject(Long subjectId) {
        logger.debug("Counting grades for subject: {}", subjectId);
        long count = gradeRepository.countBySubject_SubjectId(subjectId);
        logger.debug("Subject: {} has {} grades", subjectId, count);
        return count;
    }

    @Transactional(readOnly = true)
    public BigDecimal getMinGradeBySubject(Long subjectId) {
        logger.debug("Finding minimum grade for subject: {}", subjectId);
        BigDecimal min = gradeRepository.findMinGradeBySubjectId(subjectId);
        BigDecimal result = min != null ? min : BigDecimal.ZERO;
        logger.debug("Minimum grade for subject: {} is: {}", subjectId, result);
        return result;
    }

    @Transactional(readOnly = true)
    public BigDecimal getMaxGradeBySubject(Long subjectId) {
        logger.debug("Finding maximum grade for subject: {}", subjectId);
        BigDecimal max = gradeRepository.findMaxGradeBySubjectId(subjectId);
        BigDecimal result = max != null ? max : BigDecimal.ZERO;
        logger.debug("Maximum grade for subject: {} is: {}", subjectId, result);
        return result;
    }

    @Transactional(readOnly = true)
    public String getSubjectStatistics(Long subjectId) {
        logger.info("Generating statistics for subject: {}", subjectId);

        long count = countGradesBySubject(subjectId);

        if (count == 0) {
            logger.debug("No grades found for subject: {}", subjectId);
            return "No grades recorded for this subject.";
        }

        BigDecimal average = calculateSubjectAverage(subjectId);
        BigDecimal min = getMinGradeBySubject(subjectId);
        BigDecimal max = getMaxGradeBySubject(subjectId);

        String stats = String.format(
                "Subject Statistics:\n" +
                        "Total grades: %d\n" +
                        "Average: %.2f\n" +
                        "Minimum: %.2f\n" +
                        "Maximum: %.2f",
                count, average, min, max
        );

        logger.debug("Generated statistics for subject: {}", subjectId);
        return stats;
    }

    @Transactional(readOnly = true)
    public String getGroupStatistics(Long groupId) {
        logger.info("Generating statistics for group: {}", groupId);

        List<Grade> grades = getGradesByGroup(groupId);

        if (grades.isEmpty()) {
            logger.debug("No grades found for group: {}", groupId);
            return "No grades recorded for this group.";
        }

        BigDecimal average = calculateGroupAverage(groupId);
        long studentCount = studentRepository.countByGroup_GroupId(groupId);

        String stats = String.format(
                "Group Statistics:\n" +
                        "Total students: %d\n" +
                        "Total grades: %d\n" +
                        "Average grade: %.2f",
                studentCount, grades.size(), average
        );

        logger.debug("Generated statistics for group: {}", groupId);
        return stats;
    }

    @Transactional(readOnly = true)
    public boolean hasGrades(Long studentUserId) {
        boolean hasGrades = countGradesByStudent(studentUserId) > 0;
        logger.debug("Student userId: {} {} grades", studentUserId, hasGrades ? "has" : "has no");
        return hasGrades;
    }

    @Transactional(readOnly = true)
    public long getTotalGradeCount() {
        logger.debug("Counting total grades in system");
        long count = gradeRepository.count();
        logger.debug("Total grades in system: {}", count);
        return count;
    }

    public void deleteAllGradesByStudent(Long studentUserId) {
        logger.info("Deleting all grades for student userId: {}", studentUserId);
        List<Grade> grades = gradeRepository.findByStudent_User_UserId(studentUserId);
        gradeRepository.deleteAll(grades);
        logger.info("Deleted {} grades for student userId: {}", grades.size(), studentUserId);
    }

    public void deleteAllGradesBySubject(Long subjectId) {
        logger.info("Deleting all grades for subject: {}", subjectId);
        List<Grade> grades = gradeRepository.findBySubject_SubjectId(subjectId);
        gradeRepository.deleteAll(grades);
        logger.info("Deleted {} grades for subject: {}", grades.size(), subjectId);
    }
}