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

@Service
@Transactional
public class GroupService {

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

    /**
     * Create a new group with the new format
     * Format: [ProgramInitials][Year][LanguageCode]
     * Example: PI24E, CS23, BA24L
     */
    public Group createGroup(String programInitials, Integer startYear, String languageCode) {
        // Validate program initials (2-3 letters)
        if (programInitials == null || programInitials.trim().isEmpty()) {
            throw new IllegalArgumentException("Program initials are required");
        }
        if (programInitials.length() < 2 || programInitials.length() > 3) {
            throw new IllegalArgumentException("Program initials must be 2-3 letters");
        }

        // Validate start year (0-99)
        if (startYear == null) {
            throw new IllegalArgumentException("Start year is required");
        }
        if (startYear < 0 || startYear > 99) {
            throw new IllegalArgumentException("Start year must be between 0 and 99");
        }

        // Validate language code (1 letter, optional)
        if (languageCode != null && !languageCode.trim().isEmpty() && languageCode.length() != 1) {
            throw new IllegalArgumentException("Language code must be exactly 1 letter");
        }

        // Build group code
        String groupCode = programInitials + startYear + (languageCode != null ? languageCode : "");

        // Check if group code already exists
        if (groupRepository.existsByGroupCode(groupCode)) {
            throw new IllegalArgumentException("Group code already exists: " + groupCode);
        }

        Group group = new Group();
        group.setGroupCode(groupCode);
        group.setProgramInitials(programInitials);
        group.setStartYear(startYear);
        group.setLanguageCode(languageCode);

        return groupRepository.save(group);
    }

    /**
     * Update group with new format
     */
    public Group updateGroup(Long groupId, String newProgramInitials, Integer newStartYear, String newLanguageCode) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        boolean changed = false;

        // Update program initials if provided
        if (newProgramInitials != null && !newProgramInitials.trim().isEmpty()) {
            if (newProgramInitials.length() < 2 || newProgramInitials.length() > 3) {
                throw new IllegalArgumentException("Program initials must be 2-3 letters");
            }
            group.setProgramInitials(newProgramInitials);
            changed = true;
        }

        // Update start year if provided
        if (newStartYear != null) {
            if (newStartYear < 0 || newStartYear > 99) {
                throw new IllegalArgumentException("Start year must be between 0 and 99");
            }
            group.setStartYear(newStartYear);
            changed = true;
        }

        // Update language code if provided
        if (newLanguageCode != null) {
            if (!newLanguageCode.trim().isEmpty() && newLanguageCode.length() != 1) {
                throw new IllegalArgumentException("Language code must be exactly 1 letter");
            }
            group.setLanguageCode(newLanguageCode.trim().isEmpty() ? null : newLanguageCode);
            changed = true;
        }

        // Rebuild group code if anything changed
        if (changed) {
            String newGroupCode = group.getProgramInitials() + group.getStartYear() +
                    (group.getLanguageCode() != null ? group.getLanguageCode() : "");

            // Check if new code already exists (and it's not the current group)
            Optional<Group> existing = groupRepository.findByGroupCode(newGroupCode);
            if (existing.isPresent() && !existing.get().getGroupId().equals(groupId)) {
                throw new IllegalArgumentException("Group code already exists: " + newGroupCode);
            }

            group.setGroupCode(newGroupCode);
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
    public Optional<Group> findByGroupCode(String groupCode) {
        return groupRepository.findByGroupCode(groupCode);
    }

    @Transactional(readOnly = true)
    public List<Group> findByProgramInitials(String programInitials) {
        return groupRepository.findByProgramInitials(programInitials);
    }

    @Transactional(readOnly = true)
    public List<Group> findByStartYear(Integer startYear) {
        return groupRepository.findByStartYear(startYear);
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
    public boolean existsByGroupCode(String groupCode) {
        return groupRepository.existsByGroupCode(groupCode);
    }

    @Transactional(readOnly = true)
    public List<String> getAllProgramInitials() {
        return groupRepository.findAllProgramInitials();
    }

    @Transactional(readOnly = true)
    public List<Integer> getAllStartYears() {
        return groupRepository.findAllStartYears();
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
                        "Group Code: %s\n" +
                        "Program: %s\n" +
                        "Start Year: %d\n" +
                        "Language: %s\n" +
                        "Students: %d\n" +
                        "Subjects: %d",
                group.getGroupId(),
                group.getGroupCode(),
                group.getProgramInitials(),
                group.getStartYear(),
                group.getLanguageCode() != null ? group.getLanguageCode() : "Not specified",
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
        stats.append(String.format("Group Code: %s\n", group.getGroupCode()));
        stats.append(String.format("Program: %s\n", group.getProgramInitials()));
        stats.append(String.format("Start Year: %d\n", group.getStartYear()));
        stats.append(String.format("Language: %s\n",
                group.getLanguageCode() != null ? group.getLanguageCode() : "Not specified"));
        stats.append(String.format("Total Students: %d\n", studentCount));
        stats.append(String.format("Total Subjects: %d\n", subjectCount));
        stats.append(String.format("Status: %s\n",
                studentCount > 0 ? "Active" : "Empty"));

        return stats.toString();
    }
}