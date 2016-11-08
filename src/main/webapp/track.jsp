<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>Scorezoo tracker</title>
</head>
<body>
    <form action="/" method="post">
        <c:if test="${error != null}">
            <p style="color: red">${error}</p>
        </c:if>
        <label for="issueNumber">Укажите номер задания:</label>
        <input name="issueNumber" id="issueNumber">
        <br/><br/>
        <label for="repoName">Выберите репозиторий: </label>
        <select name="repoName" id="repoName">
            <option value="socialscore">socialscore</option>
            <option value="scorezoo_front">scorezoo_front</option>
        </select>
        <br/><br/>
        <button type="submit">Расчитать</button>
    </form>
    <c:if test="${track != null}">
            <h2>#${track.issueNumber} - ${track.issueTitle}</h2>

            <table>
                <tr>
                    <td><b>Задача:</b></td>
                </tr>
                <tr>
                    <td>#${track.issueNumber}</td>
                    <c:forEach var="entry" items="${track.trackingInIssue}">
                        <td style="padding-top: 15px">
                            <c:forEach var="row" items="${entry.value}">
                                ${row.key} - <fmt:formatNumber value="${row.value/60}" type="number" pattern="0" maxFractionDigits="2"/>h
                                (${row.value%60}m)
                                <br/>
                            </c:forEach>
                        </td>
                    </c:forEach>
                </tr>
                <tr>
                    <td style="padding-top: 15px"><b>PullRequests:</b></td>
                </tr>
                <c:forEach var="entry" items="${track.trackingInPullRequest}">
                    <tr>
                        <td>#${entry.key}</td>
                        <td style="padding-top: 15px">
                            <c:forEach var="row" items="${entry.value}">
                                ${row.key} - <fmt:formatNumber value="${row.value/60}" type="number" pattern="0" maxFractionDigits="2"/>h
                                (${row.value%60}m) <br/>
                            </c:forEach>
                        </td>
                    </tr>
                </c:forEach>
                <tr>
                    <td><b>Total:</b></td>
                    <td style="padding-top: 15px">
                        <c:forEach var="entry" items="${track.totalTrack}">
                            ${entry.key} - <fmt:formatNumber value="${entry.value/60}" type="number" pattern="0" maxFractionDigits="2"/>h
                            (${entry.value%60}m) <br/>
                        </c:forEach>
                    </td>

                </tr>


            </table>
    </c:if>

</body>
</html>