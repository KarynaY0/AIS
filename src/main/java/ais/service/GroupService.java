package ais.service;

import ais.entity.Group;
import ais.entity.Student;
import ais.entity.Subject;
import ais.entity.GroupSubject;
import ais.repository.GroupRepository;
import ais.repository.StudentRepository;
import ais.repository.SubjectRepository;
import ais.repository.GroupSubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class GroupService {

    private static final Pattern COURSE_YEAR_PATTERN = Pattern.compile("^\\d{4}/\\d{4}$");

    private final GroupRepository groupRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final GroupSubjectRepository groupSubjectRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository,
                        StudentRepository studentRepository,
                        SubjectRepository subjectRepository,
                        GroupSubjectRepository groupSubjectRepository) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.groupSubjectRepository = groupSubjectRepository;
    }

    public Group createGroup(String courseYear) {
        if (courseYear != null && !courseYear.trim().isEmpty()) {
            if (!COURSE_YEAR_PATTERN.matcher(courseYear).matches()) {
                throw new IllegalArgumentException("Course year must be in format YYYY/YYYY (e.g., 2023/2024)");
            }
        }

        Group group = new Group(courseYear);
        return groupRepository.save(group);
    }

    public Group updateGroup(Long groupId, String newCourseYear) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        if (newCourseYear != null && !newCourseYear.trim().isEmpty()) {
            if (!COURSE_YEAR_PATTERN.matcher(newCourseYear).matches()) {
                throw new IllegalArgumentException("Course year must be in format YYYY/YYYY (e.g., 2023/2024)");
            }
            group.setCourseYear(newCourseYear);
        }

        return groupRepository.save(group);
    }

    public void deleteGroup(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("Group not found with ID: " + groupId);
        }
        groupRepository.deleteById(groupId);
    }

    @Transactional(readOnly = true)
    public Optional<Group> findById(Long groupId) {
        return groupRepository.findById(groupId);
    }

    @Transactional(readOnly = true)
    public Optional<Group> findByCourseYear(String courseYear) {
        return groupRepository.findByCourseYear(courseYear);
    }

    @Transactional(readOnly = true)
    public List<Group> findAllByCourseYear(String courseYear) {
        return groupRepository.findAllByCourseYear(courseYear);
    }

    @Transactional(readOnly = true)
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Group> getGroupsBySubject(Long subjectId) {
        return groupRepository.findBySubjectId(subjectId);
    }

    @Transactional(readOnly = true)
    public List<Group> getGroupsBySemester(String academicSemester) {
        return groupRepository.findByAcademicSemester(academicSemester);
    }

    @Transactional(readOnly = true)
    public List<Group> getAllGroupsWithStudents() {
        return groupRepository.findAllGroupsWithStudents();
    }

    @Transactional(readOnly = true)
    public List<Group> getAllEmptyGroups() {
        return groupRepository.findAllEmptyGroups();
    }

    @Transactional(readOnly = true)
    public List<Group> getGroupsByTeacher(Long teacherId) {
        return groupRepository.findByTeacherId(teacherId);
    }

    @Transactional(readOnly = true)
    public List<Student> getStudentsInGroup(Long groupId) {
        return studentRepository.findByGroup_GroupId(groupId);
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjectsInGroup(Long groupId) {
        return subjectRepository.findByGroupId(groupId);
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjectsByGroupAndSemester(Long groupId, String semester) {
        return subjectRepository.findByGroupIdAndSemester(groupId, semester);
    }

    @Transactional(readOnly = true)
    public long countStudents(Long groupId) {
        return groupRepository.countStudentsInGroup(groupId);
    }

    @Transactional(readOnly = true)
    public long countSubjects(Long groupId) {
        return groupRepository.countSubjectsInGroup(groupId);
    }

    public GroupSubject assignSubject(Long groupId, Long subjectId, String academicSemester) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + subjectId));

        if (groupSubjectRepository.existsByGroupAndSubject(group, subject)) {
            throw new IllegalArgumentException("Subject already assigned to this group");
        }

        GroupSubject groupSubject = new GroupSubject(group, subject, academicSemester);
        return groupSubjectRepository.save(groupSubject);
    }

    public void removeSubject(Long groupId, Long subjectId) {
        GroupSubject groupSubject = groupSubjectRepository
                .findByGroupIdAndSubjectId(groupId, subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not assigned to this group"));

        groupSubjectRepository.delete(groupSubject);
    }

    @Transactional(readOnly = true)
    public boolean existsByCourseYear(String courseYear) {
        return groupRepository.existsByCourseYear(courseYear);
    }

    @Transactional(readOnly = true)
    public List<Group> searchByCourseYear(String yearPattern) {
        return groupRepository.findByCourseYearContaining(yearPattern);
    }

    @Transactional(readOnly = true)
    public List<String> getAllCourseYears() {
        return groupRepository.findAllCourseYears();
    }

    @Transactional(readOnly = true)
    public List<Group> getGroupsBySemesterAndCourseYear(String semester, String courseYear) {
        return groupRepository.findBySemesterAndCourseYear(semester, courseYear);
    }

    @Transactional(readOnly = true)
    public long getTotalGroupCount() {
        return groupRepository.count();
    }

    @Transactional(readOnly = true)
    public String getGroupInfo(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        long studentCount = countStudents(groupId);
        long subjectCount = countSubjects(groupId);

        return String.format(
                "Group ID: %d\n" +
                        "Course Year: %s\n" +
                        "Students: %d\n" +
                        "Subjects: %d",
                group.getGroupId(),
                group.getCourseYear() != null ? group.getCourseYear() : "Not set",
                studentCount,
                subjectCount
        );
    }

    @Transactional(readOnly = true)
    public boolean hasStudents(Long groupId) {
        return countStudents(groupId) > 0;
    }

    @Transactional(readOnly = true)
    public boolean hasSubjects(Long groupId) {
        return countSubjects(groupId) > 0;
    }

    @Transactional(readOnly = true)
    public boolean isAssignedToSubject(Long groupId, Long subjectId) {
        return groupSubjectRepository.findByGroupIdAndSubjectId(groupId, subjectId).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean canBeDeleted(Long groupId) {
        return !hasStudents(groupId) && !hasSubjects(groupId);
    }

    @Transactional(readOnly = true)
    public String getGroupStatistics(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        long studentCount = countStudents(groupId);
        long subjectCount = countSubjects(groupId);

        StringBuilder stats = new StringBuilder();
        stats.append("=== Group Statistics ===\n");
        stats.append(String.format("Group ID: %d\n", group.getGroupId()));
        stats.append(String.format("Course Year: %s\n",
                group.getCourseYear() != null ? group.getCourseYear() : "Not set"));
        stats.append(String.format("Total Students: %d\n", studentCount));
        stats.append(String.format("Total Subjects: %d\n", subjectCount));
        stats.append(String.format("Status: %s\n",
                studentCount > 0 ? "Active" : "Empty"));

        return stats.toString();
    }
}