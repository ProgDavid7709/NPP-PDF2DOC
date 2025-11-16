<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký - Dự án Async</title>
    
    <!-- CSS được trích xuất từ file "style.css" (PHP MVC) của bạn -->
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f9;
            color: #333;
            margin: 0;
            padding: 20px;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 90vh;
        }
        h2 {
            color: #2c3e50;
            text-align: center;
            margin-bottom: 25px;
        }
        a {
            color: #1abc9c;
            text-decoration: none;
            font-weight: bold;
        }
        a:hover { text-decoration: underline; color: #16a085; }
        
        .container {
             width: 100%;
             max-width: 450px;
        }

        form {
            padding: 30px;
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        
        form table {
             width: 100%;
             border-spacing: 0 15px; /* Khoảng cách giữa các dòng */
             border-collapse: separate;
        }
        form td {
             border: none;
             padding: 0 5px;
             vertical-align: middle;
        }
        form tr td:first-child {
             font-weight: bold;
             width: 130px; /* Cố định chiều rộng label (rộng hơn login) */
        }
        
        input[type="text"],
        input[type="password"],
        input[type="email"] {
            width: 100%; /* Chiếm hết 100% thẻ <td> */
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 4px;
            box-sizing: border-box; /* Quan trọng để padding không làm vỡ layout */
        }
        
        input[type="text"]:focus,
        input[type="password"]:focus,
        input[type="email"]:focus {
             border-color: #1abc9c; /* Viền xanh khi focus */
             outline: none;
        }

        input[type="submit"] {
            padding: 12px 25px;
            width: 100%;
            border: none;
            background-color: #1abc9c;
            color: white;
            cursor: pointer;
            border-radius: 4px;
            font-weight: bold;
            font-size: 16px;
            transition: background-color 0.3s ease;
        }
        input[type="submit"]:hover { background-color: #16a085; }
        
        .link-container {
            text-align: center;
            margin-top: 20px;
        }
        
        /* Hiển thị lỗi (nếu có) */
        .error-message {
            color: #e74c3c;
            font-weight: bold;
            text-align: center;
            margin-bottom: 15px;
        }
    </style>
</head>
<body>

    <div class="container">
        <!-- 
          Form này (action="/register") sẽ được xử lý bởi RegisterController.java
          Nó phải dùng method="post".
        -->
        <form action="${pageContext.request.contextPath}/register" method="post">
            <h2>Tạo Tài Khoản Mới</h2>
            
            <!-- Hiển thị lỗi (ví dụ: trùng username, sai pass) -->
            <c:if test="${not empty errorMessage}">
                <p class="error-message">${errorMessage}</p>
            </c:if>

            <table>
                <tr>
                    <td>Username:</td>
                    <td><input type="text" name="username" required></td>
                </tr>
                 <tr>
                    <td>Email:</td>
                    <td><input type="email" name="email" required></td>
                </tr>
                <tr>
                    <td>Password:</td>
                    <td><input type="password" name="password" required></td>
                </tr>
                 <tr>
                    <td>Xác nhận Pass:</td>
                    <td><input type="password" name="confirmPassword" required></td>
                </tr>
                <tr>
                    <td colspan="2">
                        <input type="submit" value="Tạo tài khoản">
                    </td>
                </tr>
            </table>
            
            <div class="link-container">
                Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
            </div>
        </form>
    </div>

</body>
</html>

