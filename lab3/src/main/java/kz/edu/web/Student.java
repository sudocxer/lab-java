package kz.edu.web;

public class Student {

    private final String name;
    private final String group;
    private final int score;

    public Student(String name, String group, int score) {
        this.name = name;
        this.group = group;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public int getScore() {
        return score;
    }
}
