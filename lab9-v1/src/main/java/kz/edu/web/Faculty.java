package kz.edu.web;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Факультет — «один» в связи Faculty 1 — * Student (индивидуальное задание, вариант 1).
 * mappedBy = "faculty": внешним ключом управляет Student.faculty (owning side).
 * cascade = ALL — операции распространяются на студентов; orphanRemoval — студент, убранный из списка, удаляется из БД.
 */
@Entity
@Table(name = "faculties")
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String dean;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @OneToMany(mappedBy = "faculty", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fullName")
    private Set<Student> students = new LinkedHashSet<>();

    public Faculty() {
    }

    public Faculty(String name, String dean) {
        this.name = name;
        this.dean = dean;
    }

    /** Синхронизирует обе стороны связи. */
    public void addStudent(Student student) {
        students.add(student);
        student.setFaculty(this);
    }

    public void removeStudent(Student student) {
        students.remove(student);
        student.setFaculty(null);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDean() {
        return dean;
    }

    public void setDean(String dean) {
        this.dean = dean;
    }

    public University getUniversity() {
        return university;
    }

    public void setUniversity(University university) {
        this.university = university;
    }

    public Set<Student> getStudents() {
        return students;
    }
}
