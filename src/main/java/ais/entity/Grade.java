package ais.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Grade")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GradeID")
    private Long gradeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SubjectID", nullable = false)
    private Subject subject;

    @Column(name = "GradeValue", nullable = false)
    private BigDecimal gradeValue;

    @Column(name = "UpdatedTime")
    private LocalDateTime updatedTime;

    @Column(name = "Comment")
    private String comment;

    // Constructors
    public Grade() {}

    public Grade(Student student, Subject subject, BigDecimal gradeValue, String comment) {
        this.student = student;
        this.subject = subject;
        this.gradeValue = gradeValue;
        this.comment = comment;
        this.updatedTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public BigDecimal getGradeValue() {
        return gradeValue;
    }

    public void setGradeValue(BigDecimal gradeValue) {
        this.gradeValue = gradeValue;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    // Helper methods to get IDs (useful for some cases)
    public Long getUserId() {
        return student != null ? student.getUserId() : null;
    }

    public Long getSubjectId() {
        return subject != null ? subject.getSubjectId() : null;
    }
}