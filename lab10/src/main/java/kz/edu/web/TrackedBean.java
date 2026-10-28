package kz.edu.web;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Основа для бинов-демонстраций областей видимости: запоминает, когда экземпляр создан,
 * и умеет назвать себя (по identityHashCode) — так видно, один и тот же это объект или новый.
 * Сам по себе бином не является (нет аннотации области).
 */
public abstract class TrackedBean implements Serializable {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final LocalTime created = LocalTime.now();

    public String instanceId() {
        return Integer.toHexString(System.identityHashCode(this));
    }

    public String created() {
        return TIME.format(created);
    }
}
