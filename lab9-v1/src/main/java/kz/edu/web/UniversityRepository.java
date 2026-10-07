package kz.edu.web;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Работа с университетом через EntityManager. Все связи LAZY, поэтому данные для страниц
 * загружаются одним запросом через JOIN FETCH (без N+1 и LazyInitializationException).
 */
public class UniversityRepository {

    /** Данные формы студента. */
    public record StudentInput(Long facultyId, String fullName, String groupName, Integer score,
                               String cardNumber, Integer cardYear, Set<Long> courseIds) {
    }

    private final EntityManagerFactory emf;

    public UniversityRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    // ---------- чтение ----------

    public University university() {
        return read(em -> em.createQuery("SELECT u FROM University u ORDER BY u.id", University.class)
                .setMaxResults(1).getResultStream().findFirst().orElse(null));
    }

    /** Факультеты со студентами, их билетами и курсами. */
    public List<Faculty> faculties() {
        return read(em -> em.createQuery(
                "SELECT DISTINCT f FROM Faculty f "
                        + "LEFT JOIN FETCH f.students s "
                        + "LEFT JOIN FETCH s.card "
                        + "LEFT JOIN FETCH s.courses "
                        + "ORDER BY f.name", Faculty.class).getResultList());
    }

    /** Студенты с фильтрами (любой может быть null): часть имени, факультет, курс. */
    public List<Student> students(String name, Long facultyId, Long courseId) {
        return read(em -> {
            StringBuilder jpql = new StringBuilder("SELECT DISTINCT s FROM Student s "
                    + "JOIN FETCH s.faculty f LEFT JOIN FETCH s.card LEFT JOIN FETCH s.courses WHERE 1 = 1");
            if (name != null) {
                jpql.append(" AND LOWER(s.fullName) LIKE :name ESCAPE '\\'");
            }
            if (facultyId != null) {
                jpql.append(" AND f.id = :facultyId");
            }
            if (courseId != null) {
                jpql.append(" AND s.id IN (SELECT s2.id FROM Student s2 JOIN s2.courses c2 WHERE c2.id = :courseId)");
            }
            jpql.append(" ORDER BY s.fullName");

            TypedQuery<Student> query = em.createQuery(jpql.toString(), Student.class);
            if (name != null) {
                query.setParameter("name", "%" + escapeLike(name.toLowerCase()) + "%");
            }
            if (facultyId != null) {
                query.setParameter("facultyId", facultyId);
            }
            if (courseId != null) {
                query.setParameter("courseId", courseId);
            }
            return query.getResultList();
        });
    }

    public Student student(long id) {
        return read(em -> em.createQuery(
                        "SELECT DISTINCT s FROM Student s JOIN FETCH s.faculty "
                                + "LEFT JOIN FETCH s.card LEFT JOIN FETCH s.courses WHERE s.id = :id", Student.class)
                .setParameter("id", id).getResultStream().findFirst().orElse(null));
    }

    public List<Course> courses() {
        return read(em -> em.createQuery("SELECT c FROM Course c ORDER BY c.title", Course.class).getResultList());
    }

    /** Студенты каждого курса: courseId -> студенты (один запрос). */
    public Map<Long, List<Student>> courseStudents() {
        return read(em -> {
            Map<Long, List<Student>> map = new HashMap<>();
            for (Object[] row : em.createQuery(
                    "SELECT c.id, s FROM Student s JOIN s.courses c ORDER BY s.fullName", Object[].class).getResultList()) {
                map.computeIfAbsent((Long) row[0], k -> new java.util.ArrayList<>()).add((Student) row[1]);
            }
            return map;
        });
    }

    public long countFaculties() {
        return read(em -> em.createQuery("SELECT COUNT(f) FROM Faculty f", Long.class).getSingleResult());
    }

    // ---------- университет / факультеты ----------

    public University ensureUniversity(String name, String city) {
        return inTransaction(em -> {
            List<University> list = em.createQuery("SELECT u FROM University u", University.class)
                    .setMaxResults(1).getResultList();
            if (!list.isEmpty()) {
                return list.get(0);
            }
            University u = new University(name, city);
            em.persist(u);
            return u;
        });
    }

    /** false — если факультет с таким названием уже есть. */
    public boolean addFaculty(String name, String dean) {
        return inTransaction(em -> {
            boolean exists = !em.createQuery("SELECT f.id FROM Faculty f WHERE LOWER(f.name) = :n", Long.class)
                    .setParameter("n", name.toLowerCase()).getResultList().isEmpty();
            University university = em.createQuery("SELECT u FROM University u", University.class)
                    .setMaxResults(1).getResultList().stream().findFirst().orElse(null);
            if (exists || university == null) {
                return false;
            }
            university.addFaculty(new Faculty(name, dean));
            return true;
        });
    }

    /** Каскадно удаляет факультет вместе со студентами (cascade = ALL). */
    public boolean deleteFaculty(long id) {
        return inTransaction(em -> {
            Faculty faculty = em.find(Faculty.class, id);
            if (faculty == null) {
                return false;
            }
            faculty.getUniversity().getFaculties().remove(faculty); // orphanRemoval + cascade REMOVE
            return true;
        });
    }

    // ---------- студенты ----------

    /** Создаёт (studentId == null) или обновляет студента. null — не найден факультет/студент. */
    public Student saveStudent(Long studentId, StudentInput in) {
        return inTransaction(em -> {
            Faculty faculty = em.find(Faculty.class, in.facultyId());
            if (faculty == null) {
                return null;
            }
            Student student;
            if (studentId == null) {
                student = new Student();
                faculty.addStudent(student);
            } else {
                student = em.find(Student.class, studentId);
                if (student == null) {
                    return null;
                }
                if (!student.getFaculty().getId().equals(faculty.getId())) {
                    // Перенос меняет только owning side (faculty_id). Нельзя убирать студента из старой
                    // коллекции faculty.students — orphanRemoval сочтёт его «сиротой» и удалит из БД.
                    student.setFaculty(faculty);
                }
            }
            student.setFullName(in.fullName());
            student.setGroupName(in.groupName());
            student.setScore(in.score());

            if (!in.cardNumber().isEmpty() || in.cardYear() != null) {
                StudentCard card = student.getCard() == null ? new StudentCard() : student.getCard();
                card.setNumber(in.cardNumber());
                card.setEnrollmentYear(in.cardYear());
                student.setCard(card);
            } else {
                student.setCard(null); // orphanRemoval удалит строку student_cards
            }

            student.getCourses().clear();
            for (Long courseId : in.courseIds()) {
                Course course = em.find(Course.class, courseId);
                if (course != null) {
                    student.getCourses().add(course);
                }
            }
            if (studentId == null) {
                em.persist(student);
            }
            return student;
        });
    }

    /** Удаление через коллекцию факультета: срабатывает orphanRemoval. */
    public boolean deleteStudent(long id) {
        return inTransaction(em -> {
            Student student = em.find(Student.class, id);
            if (student == null) {
                return false;
            }
            student.getFaculty().removeStudent(student);
            return true;
        });
    }

    // ---------- курсы ----------

    public boolean addCourse(String title, int credits) {
        return inTransaction(em -> {
            boolean exists = !em.createQuery("SELECT c.id FROM Course c WHERE LOWER(c.title) = :n", Long.class)
                    .setParameter("n", title.toLowerCase()).getResultList().isEmpty();
            if (exists) {
                return false;
            }
            em.persist(new Course(title, credits));
            return true;
        });
    }

    /** Курс снимается со всех студентов (Student — владелец связи), затем удаляется. */
    public boolean deleteCourse(long id) {
        return inTransaction(em -> {
            Course course = em.find(Course.class, id);
            if (course == null) {
                return false;
            }
            for (Student s : em.createQuery("SELECT s FROM Student s JOIN s.courses c WHERE c.id = :id", Student.class)
                    .setParameter("id", id).getResultList()) {
                s.getCourses().remove(course);
            }
            em.remove(course);
            return true;
        });
    }

    // ---------- инфраструктура ----------

    private <T> T read(Function<EntityManager, T> action) {
        try (EntityManager em = emf.createEntityManager()) {
            return action.apply(em);
        }
    }

    private <T> T inTransaction(Function<EntityManager, T> action) {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                T result = action.apply(em);
                tx.commit();
                return result;
            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
