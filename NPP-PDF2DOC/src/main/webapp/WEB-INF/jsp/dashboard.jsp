<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Dashboard - NPP PDF2DOC</title>

    <!-- Centralized theme CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/theme.css" />
    <style>
        /* Small local adjustments to ensure the upload button aligns with the heading
           without requiring changes in the central theme. Remove or move this to theme.css
           if you prefer global styling. */
        .page .header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 1rem;
            flex-wrap: wrap;
            margin-bottom: 1rem;
        }

        .page .header h2 {
            margin: 0;
            font-size: 1.5rem;
            line-height: 1;
        }

        .page .header .actions {
            margin-left: auto;
        }

        /* Ensure buttons have reasonable spacing when wrapped on small viewports */
        .page .header .btn {
            margin: 0;
        }

        /* Keep the rest of the table layout intact as in the theme */
        .table-wrap table {
            width: 100%;
            border-collapse: collapse;
        }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/partials/header.jsp" />

    <div class="page" role="main" aria-labelledby="dashboard-heading">
        <header class="header" role="region" aria-label="Dashboard header">
            <h2 id="dashboard-heading">Bảng điều khiển của bạn</h2>

            <!-- Upload button moved to be on the same horizontal row as the heading.
                 Logout button removed per request. -->
            <div class="actions">
                <a href="${pageContext.request.contextPath}/process" class="btn" title="Tải lên file mới">Tải lên mới</a>
            </div>
        </header>

        <section class="table-wrap" aria-label="Danh sách tác vụ">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Tên file</th>
                        <th>Ngày</th>
                        <th>Trạng thái</th>
                        <th>Kết quả</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty taskList}">
                            <c:forEach items="${taskList}" var="task">
                                <tr>
                                    <td>${task.id}</td>
                                    <td>${task.fileName}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty task.date}">
                                                <fmt:formatDate value="${task.date}" pattern="yyyy-MM-dd HH:mm:ss" />
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${task.status == 'PENDING'}">
                                                <span class="status-badge status-pending">Đang chờ</span>
                                            </c:when>
                                            <c:when test="${task.status == 'PROCESSING'}">
                                                <span class="status-badge status-processing">Đang xử lý</span>
                                            </c:when>
                                            <c:when test="${task.status == 'COMPLETED'}">
                                                <span class="status-badge status-completed">Hoàn thành</span>
                                            </c:when>
                                            <c:when test="${task.status == 'FAILED'}">
                                                <span class="status-badge status-failed">Thất bại</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge">${task.status}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><c:out value="${task.resultSummary}" /></td>
                                    <td>
                                        <c:if test="${task.status == 'COMPLETED'}">
                                            <a class="btn secondary" href="${pageContext.request.contextPath}/download?taskId=${task.id}">Tải xuống</a>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>

                        <c:otherwise>
                            <tr>
                                <td colspan="6" class="empty-state">Chưa có tác vụ nào.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </section>
    </div>

    <!-- Optional small client debug (non-blocking) -->
    <script type="text/javascript">
        (function () {
            try {
                console.log("[CLIENT-DEBUG] Dashboard loaded:", window.location.href);
            } catch (e) {
                // ignore
            }
        })();
    </script>
</body>
</html>
