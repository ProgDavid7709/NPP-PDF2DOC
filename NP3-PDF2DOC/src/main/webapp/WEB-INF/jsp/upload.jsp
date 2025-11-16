<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Upload File - Async Project</title>

    <!-- 
      CSS TÙY CHỈNH (DỰA TRÊN CÁC FILE BẠN CUNG CẤP)
      Sử dụng chung style với trang login.jsp/register.jsp
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
            padding: 20px 0;
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
            max-width: 500px; /* Rộng hơn form login một chút */
            margin: 20px auto;
            padding: 30px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.1);
        }
        
        /* CSS cho input[type=file] */
        input[type="file"] {
            width: 100%;
            padding: 10px;
            margin-top: 5px;
            margin-bottom: 20px;
            border: 1px solid #ccc;
            border-radius: 4px;
            font-size: 14px;
            background: white;
        }
        
        /* Tùy chỉnh giao diện cho nút "Choose File" */
        input[type="file"]::file-selector-button {
            padding: 8px 15px;
            border: none;
            background-color: #34495e; /* Màu nền từ header bảng */
            color: white;
            cursor: pointer;
            border-radius: 4px;
            font-weight: bold;
            transition: background-color 0.3s ease;
            margin-right: 10px;
        }
        
        input[type="file"]::file-selector-button:hover {
             background-color: #2c3e50;
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
        
        /* Link xem Trạng thái (ở dưới cùng) */
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
        
        <h2>Tải lên File PDF</h2>

        <!-- 
          QUAN TRỌNG: Form upload file BẮT BUỘC phải có:
          1. method="POST"
          2. enctype="multipart/form-data" (Rất quan trọng)
          3. Action trỏ đến UploadFileServlet của bạn
        -->
        <form action="${pageContext.request.contextPath}/upload" method="POST" enctype="multipart/form-data">
            
            <div class="form-group">
                <label for="pdf-file">Chọn file PDF để xử lý:</label>
                <!-- 
                  SỬA LỖI:
                  Đổi name="file" thành name="pdfFile" 
                  để khớp với request.getPart("pdfFile") 
                  trong UploadFileServlet.java
                -->
                <input id="pdf-file" name="pdfFile" type="file" accept=".pdf" required>
            </div>

            <div>
                <input type="submit" value="Tải lên và Xử lý">
            </div>
            
            <div class="form-footer">
                <a href="${pageContext.request.contextPath}/status">
                    Xem danh sách tác vụ
                </a>
            </div>
        </form>
    </div>
</body>
</html>

