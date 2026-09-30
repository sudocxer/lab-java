package kz.edu.web;

/**
 * Экранирование пользовательского ввода перед вставкой в HTML,
 * чтобы данные из формы (например, логин) не могли быть интерпретированы как разметка (XSS).
 */
final class Html {

    private Html() {
    }

    static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
