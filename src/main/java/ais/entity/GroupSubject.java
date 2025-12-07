package ais.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "GroupSubject", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"GroupID", "SubjectID"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GroupID", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SubjectID", nullable = false)
    private Subject subject;

    @Column(name = "AcademicSemester", length = 20)
    private String academicSemester;

    public GroupSubject(Group group, Subject subject, String academicSemester) {
        this.group = group;
        this.subject = subject;
        this.academicSemester = academicSemester;
    }
}