package ais.service;

import ais.entity.Grade;
import ais.entity.Student;
import ais.entity.Subject;
import ais.repository.GradeRepository;
import ais.repository.StudentRepository;
import ais.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final SubjectRepository subjectRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository,
                          GradeRepository gradeRepository,
                          SubjectRepository subjectRepository) {
        this.studentRepository = studentRepository;
        this.gradeRepository = gradeRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional(readOnly = true)
    public List<Grade> getMyGrades(Long studentUserId) {
        logger.debug("Retrieving grades for student userId: {}", studentUserId);
        List<Grade> grades = gradeRepository.findByStudent_User_UserId(studentUserId);
        logger.debug("Found {} grades for student userId: {}", grades.size(), studentUserId);
        return grades;
    }

    @Transactional(readOnly = true)
    public List<Subject> getMySubjects(Long studentUserId) {
        logger.debug("Retrieving subjects for student userId: {}", studentUserId);
        List<Subject> subjects = subjectRepository.findByStudentId(studentUserId);
        logger.debug("Found {} subjects for student userId: {}", subjects.size(), studentUserId);
        return subjects;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateMyAverage(Long studentUserId) {
        logger.debug("Calculating average grade for student userId: {}", studentUserId);
        BigDecimal average = gradeRepository.calculateAverageByStudentId(studentUserId);
        BigDecimal result = average != null ? average : BigDecimal.ZERO;
        logger.debug("Average grade for student userId: {} is: {}", studentUserId, result);
        return result;
    }

    @Transactional(readOnly = true)
    public Student getStudentByUserId(Long userId) {
        logger.debug("Retrieving student with userId: {}", userId);
        return studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> {
                    logger.error("Student not found with userId: {}", userId);
                    return new IllegalArgumentException("Student not found");
                });
    }

    @Transactional(readOnly = true)
    public boolean isStudent(Long userId) {
        boolean exists = studentRepository.existsByUser_UserId(userId);
        logger.debug("User ID: {} is{} a student", userId, exists ? "" : " not");
        return exists;
    }

    @Transactional(readOnly = true)
    public long countMyGrades(Long studentUserId) {
        logger.debug("Counting grades for student userId: {}", studentUserId);
        long count = gradeRepository.countByStudent_User_UserId(studentUserId);
        logger.debug("Student userId: {} has {} grades", studentUserId, count);
        return count;
    }
}