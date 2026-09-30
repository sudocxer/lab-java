package kz.edu.web;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/** Хранилище записей аудита в памяти (последние {@value #LIMIT} действий). */
final class AuditLog {

    static final int LIMIT = 50;

    record Entry(LocalDateTime time, String user, String ip, String method,
                 String uri, String params, int status) {
    }

    private static final ConcurrentLinkedDeque<Entry> ENTRIES = new ConcurrentLinkedDeque<>();

    private AuditLog() {
    }

    static void add(Entry entry) {
        ENTRIES.addFirst(entry);
        while (ENTRIES.size() > LIMIT) {
            ENTRIES.pollLast();
        }
    }

    /** Записи, новые первыми. */
    static List<Entry> all() {
        return new ArrayList<>(ENTRIES);
    }
}
