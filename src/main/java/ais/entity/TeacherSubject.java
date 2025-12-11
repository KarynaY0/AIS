package ais.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TeacherSubject", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"UserID", "SubjectID"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SubjectID", nullable = false)
    private Subject subject;

    @Column(name = "IsActive")
    private Boolean isActive = true;

    public TeacherSubject(Teacher teacher, Subject subject) {
        this.teacher = teacher;
        this.subject = subject;
        this.isActive = true;
    }
}