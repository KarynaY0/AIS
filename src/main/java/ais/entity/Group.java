package ais.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "`Group`")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "groupid")
    private Long groupId;

    @Column(name = "group_code", unique = true, nullable = false, length = 10)
    private String groupCode;

    @Column(name = "program_initials", nullable = false, length = 3)
    private String programInitials;

    @Column(name = "start_year", nullable = false)
    private Integer startYear;

    @Column(name = "language_code", length = 1)
    private String languageCode;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Student> students = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupSubject> groupSubjects = new ArrayList<>();

    // Simple constructor without relationships
    public Group(String groupCode, String programInitials, Integer startYear, String languageCode) {
        this.groupCode = groupCode;
        this.programInitials = programInitials;
        this.startYear = startYear;
        this.languageCode = languageCode;
        this.students = new ArrayList<>();
        this.groupSubjects = new ArrayList<>();
    }
}