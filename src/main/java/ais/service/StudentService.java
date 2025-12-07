package ais.service;

import ais.entity.Grade;
import ais.entity.Student;
import ais.entity.Subject;
import ais.repository.GradeRepository;
import ais.repository.StudentRepository;
import ais.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class StudentService {

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
        return gradeRepository.findByStudent_User_UserId(studentUserId);
    }

    @Transactional(readOnly = true)
    public List<Subject> getMySubjects(Long studentUserId) {
        return subjectRepository.findByStudentId(studentUserId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateMyAverage(Long studentUserId) {
        BigDecimal average = gradeRepository.calculateAverageByStudentId(studentUserId);
        return average != null ? average : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public Student getStudentByUserId(Long userId) {
        return studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
    }

    @Transactional(readOnly = true)
    public boolean isStudent(Long userId) {
        return studentRepository.existsByUser_UserId(userId);
    }

    @Transactional(readOnly = true)
    public long countMyGrades(Long studentUserId) {
        return gradeRepository.countByStudent_User_UserId(studentUserId);
    }
}