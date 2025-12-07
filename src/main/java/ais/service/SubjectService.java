package ais.service;

import ais.entity.Subject;
import ais.repository.SubjectRepository;
import ais.repository.TeacherSubjectRepository;
import ais.repository.GroupSubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final GroupSubjectRepository groupSubjectRepository;

    @Autowired
    public SubjectService(SubjectRepository subjectRepository,
                          TeacherSubjectRepository teacherSubjectRepository,
                          GroupSubjectRepository groupSubjectRepository) {
        this.subjectRepository = subjectRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.groupSubjectRepository = groupSubjectRepository;
    }

    public Subject createSubject(String code, Integer credits) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject code cannot be empty");
        }

        if (subjectRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Subject with code already exists: " + code);
        }

        if (credits != null && credits <= 0) {
            throw new IllegalArgumentException("Credits must be positive");
        }

        Subject subject = new Subject(code, credits);
        return subjectRepository.save(subject);
    }

    public Subject updateSubject(Long subjectId, String newCode, Integer newCredits) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));

        if (newCode != null && !newCode.trim().isEmpty() && !newCode.equals(subject.getCode())) {
            if (subjectRepository.existsByCode(newCode)) {
                throw new IllegalArgumentException("Subject with code already exists: " + newCode);
            }
            subject.setCode(newCode);
        }

        if (newCredits != null) {
            if (newCredits <= 0) {
                throw new IllegalArgumentException("Credits must be positive");
            }
            subject.setCredits(newCredits);
        }

        return subjectRepository.save(subject);
    }

    public void deleteSubject(Long subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new IllegalArgumentException("Subject not found with ID: " + subjectId);
        }
        subjectRepository.deleteById(subjectId);
    }

    @Transactional(readOnly = true)
    public Optional<Subject> findById(Long subjectId) {
        return subjectRepository.findById(subjectId);
    }

    @Transactional(readOnly = true)
    public Optional<Subject> findByCode(String code) {
        return subjectRepository.findByCode(code);
    }

    @Transactional(readOnly = true)
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Subject> findByCredits(Integer credits) {
        return subjectRepository.findByCredits(credits);
    }

    @Transactional(readOnly = true)
    public List<Subject> findByCreditsRange(Integer minCredits, Integer maxCredits) {
        return subjectRepository.findByCreditsRange(minCredits, maxCredits);
    }

    @Transactional(readOnly = true)
    public List<Subject> searchByCode(String codePattern) {
        return subjectRepository.findByCodeContaining(codePattern);
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjectsByTeacher(Long teacherId) {
        return subjectRepository.findByTeacherId(teacherId);
    }

    @Transactional(readOnly = true)
    public List<Subject> getAllSubjectsByTeacher(Long teacherId) {
        return subjectRepository.findAllByTeacherId(teacherId);
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjectsByGroup(Long groupId) {
        return subjectRepository.findByGroupId(groupId);
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjectsByGroupAndSemester(Long groupId, String semester) {
        return subjectRepository.findByGroupIdAndSemester(groupId, semester);
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjectsBySemester(String semester) {
        return subjectRepository.findByAcademicSemester(semester);
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjectsByStudent(Long studentId) {
        return subjectRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjectsWithoutTeachers() {
        return subjectRepository.findSubjectsWithoutTeachers();
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjectsWithoutGroups() {
        return subjectRepository.findSubjectsWithoutGroups();
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjectsWithActiveTeachers() {
        return subjectRepository.findSubjectsWithActiveTeachers();
    }

    @Transactional(readOnly = true)
    public long countActiveTeachers(Long subjectId) {
        return subjectRepository.countActiveTeachersBySubjectId(subjectId);
    }

    @Transactional(readOnly = true)
    public long countGroups(Long subjectId) {
        return subjectRepository.countGroupsBySubjectId(subjectId);
    }

    @Transactional(readOnly = true)
    public List<Integer> getAllDistinctCredits() {
        return subjectRepository.findAllDistinctCredits();
    }

    @Transactional(readOnly = true)
    public boolean codeExists(String code) {
        return subjectRepository.existsByCode(code);
    }

    @Transactional(readOnly = true)
    public long getTotalSubjectCount() {
        return subjectRepository.count();
    }

    @Transactional(readOnly = true)
    public String getSubjectInfo(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));

        long teacherCount = countActiveTeachers(subjectId);
        long groupCount = countGroups(subjectId);

        return String.format(
                "Subject: %s\n" +
                        "Credits: %d\n" +
                        "Active Teachers: %d\n" +
                        "Groups Assigned: %d",
                subject.getCode(),
                subject.getCredits() != null ? subject.getCredits() : 0,
                teacherCount,
                groupCount
        );
    }

    @Transactional(readOnly = true)
    public boolean hasActiveAssignments(Long subjectId) {
        return countActiveTeachers(subjectId) > 0 || countGroups(subjectId) > 0;
    }

    @Transactional(readOnly = true)
    public boolean canBeDeleted(Long subjectId) {
        return !hasActiveAssignments(subjectId);
    }
}