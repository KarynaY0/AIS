package ais.service;

import ais.entity.*;
import ais.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final GroupSubjectRepository groupSubjectRepository;

    @Autowired
    public AdminService(UserRepository userRepository,
                        StudentRepository studentRepository,
                        TeacherRepository teacherRepository,
                        GroupRepository groupRepository,
                        SubjectRepository subjectRepository,
                        TeacherSubjectRepository teacherSubjectRepository,
                        GroupSubjectRepository groupSubjectRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.groupRepository = groupRepository;
        this.subjectRepository = subjectRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.groupSubjectRepository = groupSubjectRepository;
    }

    // ==================== STUDENT MANAGEMENT ====================

    public Student createStudent(String firstName, String lastName, Long groupId) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (userRepository.existsByUsername(firstName)) {
            throw new IllegalArgumentException("Username already exists: " + firstName);
        }

        User user = new User(firstName, lastName);
        user = userRepository.save(user);

        Group group = null;
        if (groupId != null) {
            group = groupRepository.findById(groupId).orElse(null);
        }

        Student student = new Student(user, group);
        return studentRepository.save(student);
    }

    public void deleteStudent(Long studentUserId) {
        Student student = studentRepository.findByUser_UserId(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        studentRepository.delete(student);
    }

    public Student assignStudentToGroup(Long studentUserId, Long groupId) {
        Student student = studentRepository.findByUser_UserId(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        student.setGroup(group);
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // ==================== TEACHER MANAGEMENT ====================

    public Teacher createTeacher(String firstName, String lastName, String department) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (userRepository.existsByUsername(firstName)) {
            throw new IllegalArgumentException("Username already exists: " + firstName);
        }

        User user = new User(firstName, lastName);
        user = userRepository.save(user);

        Teacher teacher = new Teacher(user, department);
        return teacherRepository.save(teacher);
    }

    public void deleteTeacher(Long teacherUserId) {
        Teacher teacher = teacherRepository.findByUser_UserId(teacherUserId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        teacherRepository.delete(teacher);
    }

    public TeacherSubject assignTeacherToSubject(Long teacherUserId, Long subjectId) {
        Teacher teacher = teacherRepository.findByUser_UserId(teacherUserId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        if (teacherSubjectRepository.existsByTeacherAndSubject(teacher, subject)) {
            throw new IllegalArgumentException("Teacher already assigned to this subject");
        }

        TeacherSubject assignment = new TeacherSubject(teacher, subject);
        return teacherSubjectRepository.save(assignment);
    }

    public void removeTeacherFromSubject(Long teacherUserId, Long subjectId) {
        TeacherSubject assignment = teacherSubjectRepository
                .findByTeacherIdAndSubjectId(teacherUserId, subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        teacherSubjectRepository.delete(assignment);
    }

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    // ==================== GROUP MANAGEMENT ====================

    public Group createGroup(String courseYear) {
        Group group = new Group(courseYear);
        return groupRepository.save(group);
    }

    public void deleteGroup(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("Group not found");
        }
        groupRepository.deleteById(groupId);
    }

    public GroupSubject assignSubjectToGroup(Long groupId, Long subjectId, String academicSemester) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        if (groupSubjectRepository.existsByGroupAndSubject(group, subject)) {
            throw new IllegalArgumentException("Subject already assigned to this group");
        }

        GroupSubject assignment = new GroupSubject(group, subject, academicSemester);
        return groupSubjectRepository.save(assignment);
    }

    public void removeSubjectFromGroup(Long groupId, Long subjectId) {
        GroupSubject assignment = groupSubjectRepository
                .findByGroupIdAndSubjectId(groupId, subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        groupSubjectRepository.delete(assignment);
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    // ==================== SUBJECT MANAGEMENT ====================

    public Subject createSubject(String code, Integer credits) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject code is required");
        }

        if (subjectRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Subject code already exists: " + code);
        }

        Subject subject = new Subject(code, credits);
        return subjectRepository.save(subject);
    }

    public void deleteSubject(Long subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new IllegalArgumentException("Subject not found");
        }
        subjectRepository.deleteById(subjectId);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }
}