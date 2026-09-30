<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>

<head>
<meta charset="UTF-8">
<title>Список студентов</title>
<link rel="stylesheet" href="style.css">
</head>

<body>

<h1>Список студентов</h1>

<table>
<tr><th>ФИО</th><th>Группа</th><th>Баллы</th><th>Статус</th></tr>

<c:forEach var="student" items="${students}">
<tr>
    <td>${student.fullName}</td>
    <td>${student.group}</td>
    <td>${student.score}</td>
    <td>
        <c:if test="${student.score >= 90}">
            <strong>Отличник</strong>
        </c:if>
        <c:if test="${student.score < 90}">
            &mdash;
        </c:if>
    </td>
</tr>
</c:forEach>

</table>

</body>
</html>
