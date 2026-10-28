package kz.edu.web;

import jakarta.enterprise.context.Dependent;

/** @Dependent — жизненный цикл зависит от компонента, в который бин внедрён: своя копия в каждой точке внедрения. */
@Dependent
public class DependentInfo extends TrackedBean {
}
