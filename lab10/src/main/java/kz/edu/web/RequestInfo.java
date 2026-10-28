package kz.edu.web;

import jakarta.enterprise.context.RequestScoped;

/** @RequestScoped — новый экземпляр на каждый HTTP-запрос. */
@RequestScoped
public class RequestInfo extends TrackedBean {
}
