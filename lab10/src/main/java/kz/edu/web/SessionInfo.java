package kz.edu.web;

import jakarta.enterprise.context.SessionScoped;

/** @SessionScoped — свой экземпляр у каждой HTTP-сессии (бин обязан быть Serializable). */
@SessionScoped
public class SessionInfo extends TrackedBean {

    private int views;

    public synchronized int hit() {
        return ++views;
    }
}
