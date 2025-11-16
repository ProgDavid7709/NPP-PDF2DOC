<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
  File: index.jsp
  Mục đích: Tự động chuyển hướng từ trang gốc (/) đến trang đăng nhập (/login).
--%>
<%
    // Lấy đường dẫn gốc của ứng dụng (ví dụ: /Async-JSP-PDF-Project)
    String contextPath = request.getContextPath();
    
    // Thực hiện chuyển hướng (redirect)
    response.sendRedirect(contextPath + "/login");
%>