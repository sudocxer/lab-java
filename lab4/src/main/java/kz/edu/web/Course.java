package kz.edu.web;

public class Course {

    private final Long id;
    private final String title;
    private final int hours;

    public Course(Long id, String title, int hours) {
        this.id = id;
        this.title = title;
        this.hours = hours;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getHours() {
        return hours;
    }
}
