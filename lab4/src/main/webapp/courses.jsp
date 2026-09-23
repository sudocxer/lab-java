<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>

<head>
<meta charset="UTF-8">
<title>Учебные курсы</title>
</head>

<body>

<h1>Учебные курсы</h1>

<table border="1" cellpadding="6" cellspacing="0">
<tr><th>Название</th><th>Часов</th><th>Тип</th></tr>

<c:forEach var="course" items="${courses}">
<tr>
    <td>${course.title}</td>
    <td>${course.hours}</td>
    <td>
        <c:if test="${course.hours > 50}">
            <strong>Расширенный курс</strong>
        </c:if>
        <c:if test="${course.hours <= 50}">
            Базовый курс
        </c:if>
    </td>
</tr>
</c:forEach>

</table>

</body>
</html>
