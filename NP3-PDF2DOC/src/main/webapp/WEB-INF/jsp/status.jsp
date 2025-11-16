<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- 
  SỬA LỖI 500:
  Chúng ta CHỈ CẦN MỘT dòng khai báo taglib cho prefix "c".
  Tất cả các dòng trùng lặp đã bị xóa.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trạng thái Tác vụ - Async Project</title>

    <!-- 
      CSS TÙY CHỈNH (DỰA TRÊN CÁC FILE BẠN CUNG CẤP)
      Trích xuất từ filecss3.css (cho bảng)
    -->
    <style type"text/css">
        /* Reset mặc định */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: Arial, sans-serif;
            background: #f4f6f9; /* Lấy từ filecss3.css */
            padding-bottom: 50px; /* Thêm padding để không bị che */
        }
        
        /* Container chung cho trang */
        .page-container {
            width: 95%;
            max-width: 1100px;
            margin: 20px auto;
        }
        
        /* Tiêu đề (Lấy từ h2 trong filecss3.css) */
        .header {
             display: flex;
             justify-content: space-between;
             align-items: center;
             margin: 20px 0 25px 0;
        }
        
        .header h2 {
            color: #2c3e50;
            margin: 0;
        }
        
        /* Nút bấm (Lấy từ filecss3.css) */
        .btn-primary {
            padding: 10px 20px;
            border: none;
            background-color: #1abc9c;
            color: white;
            cursor: pointer;
            border-radius: 4px;
            font-weight: bold;
            font-size: 14px;
            text-decoration: none;
            transition: background-color 0.3s ease;
        }
        .btn-primary:hover {
            background-color: #16a085;
        }
        
        .btn-secondary {
            padding: 5px 10px;
            border: none;
            background-color: #3498db;
            color: white;
            cursor: pointer;
            border-radius: 4px;
            font-size: 12px;
            text-decoration: none;
            transition: background-color 0.3s ease;
        }
         .btn-secondary:hover {
            background-color: #2980b9;
        }

        /* Bảng (Lấy từ table trong filecss3.css) */
        .table-container {
            width: 100%;
            margin: 20px auto;
            border-collapse: collapse;
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
            background-color: white;
            border-radius: 8px;
            overflow: hidden;
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
        }

        /* Tiêu đề cột */
        th {
            background-color: #34495e;
            color: white;
            font-weight: bold;
            padding: 12px 15px;
            text-align: left;
            font-size: 13px;
            text-transform: uppercase;
        }

        /* Các ô dữ liệu */
        td {
            padding: 12px 15px;
            border-bottom: 1px solid #ddd;
        }

        tr:nth-child(even) {
            background-color: #f8f9fa; /* Màu xen kẽ */
        }

        tr:hover {
            background-color: #e9ecef; /* Màu khi di chuột qua */
        }

        tr:last-of-type td {
            border-bottom: none; /* Bỏ viền cho dòng cuối */
        }
        
        /* Badge Trạng thái */
        .status-badge {
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: bold;
            text-transform: uppercase;
        }
        .status-pending {
            background-color: #f39c12; /* Vàng */
            color: white;
        }
        .status-processing {
            background-color: #3498db; /* Xanh dương */
            color: white;
        }
        .status-completed {
            background-color: #1abc9c; /* Xanh lá */
            color: white;
        }
         .status-failed {
            background-color: #e74c3c; /* Đỏ */
            color: white;
        }
        
        /* Trạng thái Rỗng */
        .empty-state {
            text-align: center;
            padding: 40px;
            color: #777;
        }
    </style>
</head>
<body>

    <div class="page-container">
    
        <div class="header">
            <h2>Bảng điều khiển Tác vụ</h2>
            <div>
                <%-- Link này trỏ đến trang Upload (nếu bạn tạo) --%>
                <a href="${pageContext.request.contextPath}/upload" class="btn-primary">Tải lên File mới</a>
                <%-- Link này trỏ đến LogoutController --%>
                <a href="${pageContext.request.contextPath}/logout" class="btn-secondary">Đăng xuất</a>
            </div>
        </div>

        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>ID Tác vụ</th>
                        <th>Tên File</th>
                        <th>Trạng thái</th>
                        <th>Kết quả</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <%-- 
                      DÙNG JSTL (prefix 'c') ĐỂ KIỂM TRA VÀ LẶP
                      Biến 'taskList' này được gửi từ StatusServlet.java
                    --%>
                    <c:choose>
                        <%-- 1. Nếu danh sách KHÔNG rỗng --%>
                        <c:when test="${not empty taskList}">
                            <c:forEach items="${taskList}" var="task">
                                <tr>
                                    <td>${task.id}</td>
                                    <td>${task.fileName}</td>
                                    <td>
                                        <%-- Dùng c:choose để đổi màu trạng thái --%>
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
                                    <td>${task.resultSummary}</td>
                                    <td>
                                        <%-- 
                                          Chỉ hiển thị nút Download nếu đã "COMPLETED"
                                          Link này trỏ đến DownloadController
                                        --%>
                                        <c:if test="${task.status == 'COMPLETED'}">
                                            <a href="${pageContext.request.contextPath}/download?taskId=${task.id}" class="btn-secondary">
                                                Tải xuống
                                            </a>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        
                        <%-- 2. Nếu danh sách RỖNG --%>
                        <c:otherwise>
                            <tr>
                                <td colspan="5" class="empty-state">
                                    Chưa có tác vụ nào được upload.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>

</body>
</html>

