package kz.edu.web;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Хранилище результатов измерений TimingFilter (последние {@value #LIMIT} запросов).
 */
final class RequestStats {

    static final int LIMIT = 15;

    record Entry(LocalTime time, String method, String uri, int status, double millis) {
    }

    private static final ConcurrentLinkedDeque<Entry> RECENT = new ConcurrentLinkedDeque<>();
    private static final AtomicLong TOTAL = new AtomicLong();

    private RequestStats() {
    }

    static void add(Entry entry) {
        RECENT.addFirst(entry);
        while (RECENT.size() > LIMIT) {
            RECENT.pollLast();
        }
        TOTAL.incrementAndGet();
    }

    /** Последние запросы, новые первыми. */
    static List<Entry> recent() {
        return new ArrayList<>(RECENT);
    }

    static long total() {
        return TOTAL.get();
    }
}
