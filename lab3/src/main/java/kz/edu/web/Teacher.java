package kz.edu.web;

/**
 * Индивидуальное задание, вариант 4: карточка преподавателя.
 */
public class Teacher {

    private final String fullName;
    private final String department;
    private final String position;
    private final int experienceYears;

    public Teacher(String fullName, String department, String position, int experienceYears) {
        this.fullName = fullName;
        this.department = department;
        this.position = position;
        this.experienceYears = experienceYears;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }

    public int getExperienceYears() {
        return experienceYears;
    }
}
