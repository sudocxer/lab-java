package kz.edu.web;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicInteger;

/** @ApplicationScoped — один экземпляр на приложение, общий для всех пользователей. */
@ApplicationScoped
public class ApplicationInfo extends TrackedBean {

    private final AtomicInteger views = new AtomicInteger();

    public int hit() {
        return views.incrementAndGet();
    }
}
