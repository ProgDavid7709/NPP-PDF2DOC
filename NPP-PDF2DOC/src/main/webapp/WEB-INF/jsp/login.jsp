<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - Async Project</title>

    <!-- 
      CSS TÙY CHỈNH (DỰA TRÊN CÁC FILE BẠN CUNG CẤP)
      Trích xuất từ filecss2.css và filecss3.css
    -->
    <style type="text/css">
        /* Reset mặc định */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: Arial, sans-serif;
            background: #f4f6f9; /* Lấy từ filecss3.css */
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }
        
        /* Tiêu đề (Lấy từ h2 trong filecss3.css) */
        h2 {
            color: #2c3e50;
            text-align: center;
            margin-bottom: 25px;
        }

        /* Form (Lấy từ .form-container trong filecss3.css) */
        .form-container {
            width: 90%;
            max-width: 400px; /* Thu hẹp cho form đăng nhập */
            margin: 20px auto;
            padding: 30px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.1);
        }
        
        /* CSS cho các dòng input */
        .form-group {
            margin-bottom: 20px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #34495e;
        }

        /* Input (Lấy từ filecss3.css) */
        input[type="text"],
        input[type="password"] {
            width: 100%;
            padding: 12px; /* Tăng padding cho đẹp hơn */
            margin-top: 5px;
            border: 1px solid #ccc;
            border-radius: 4px;
            font-size: 14px;
        }

        input[type="text"]:focus,
        input[type="password"]:focus {
            border-color: #1abc9c; /* Viền xanh khi focus */
            outline: none;
            box-shadow: 0 0 5px rgba(26, 188, 156, 0.3);
        }

        /* Nút bấm (Lấy từ filecss3.css) */
        input[type="submit"] {
            width: 100%; /* Nút bấm full-width */
            padding: 12px 20px;
            border: none;
            background-color: #1abc9c;
            color: white;
            cursor: pointer;
            border-radius: 4px;
            font-weight: bold;
            font-size: 16px; /* Chữ to rõ ràng */
            transition: background-color 0.3s ease;
        }

        input[type="submit"]:hover {
            background-color: #16a085;
        }
        
        /* Link Đăng ký (ở dưới cùng) */
        .form-footer {
            text-align: center;
            margin-top: 20px;
        }
        
        .form-footer a {
            color: #1abc9c;
            text-decoration: none;
            font-weight: bold;
        }
        .form-footer a:hover {
            text-decoration: underline;
        }
        
    </style>
</head>
<body>

    <!-- Áp dụng class .form-container -->
    <div class="form-container">
        
        <h2>Đăng nhập hệ thống</h2>

        <!-- 
          Form trỏ action đến LoginController
        -->
        <form action="${pageContext.request.contextPath}/login" method="POST">
            
            <div class="form-group">
                <label for="username">Tên đăng nhập</label>
                <input id="username" name="username" type="text" required>
            </div>
            
            <div class="form-group">
                <label for="password">Mật khẩu</label>
                <input id="password" name="password" type="password" required>
            </div>

            <div>
                <input type="submit" value="Đăng nhập">
            </div>
            
            <div class="form-footer">
                <a href="${pageContext.request.contextPath}/register">
                    Chưa có tài khoản? Đăng ký
                </a>
            </div>
        </form>
    </div>
</body>
</html>

