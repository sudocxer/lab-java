package kz.edu.web;

/**
 * Model для индивидуального задания, вариант 4: список студентов.
 */
public class Student {

    private final Long id;
    private final String fullName;
    private final String group;
    private final int score;

    public Student(Long id, String fullName, String group, int score) {
        this.id = id;
        this.fullName = fullName;
        this.group = group;
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getGroup() {
        return group;
    }

    public int getScore() {
        return score;
    }
}
